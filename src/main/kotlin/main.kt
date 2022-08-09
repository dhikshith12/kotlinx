import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*

fun main() = runBlocking<Unit>{
    val channel = Channel<String>()
    launch {
        channel.send("A1")
        channel.send("A2")
        log("A done")
    }
    launch{
        channel.send("B1")
        log("B done")
    }
    launch{
        repeat(3){
            val x = channel.receive()
            log(x)
        }
    }
}
fun log(message: Any?) {
    println("[${Thread.currentThread().name}] $message")
}

/*
    Whole application is running on a single thread.
    producer A sends A1 to the channel, since the channel is rendezvous channel, and there is no receive waiting for it
    this producer gets suspended.
    main thread is free and producer B gets called
    producer B sends B1 to the channel, since the channel has no receiver waiting for it this producer also
    gets suspended.
    consumer gets called and rendezvous happens and A1 gets printed from consumer Co-routine and producer A gets awaken since the main thread is busy in this consumer it gets scheduled
    repeats and again rendezvous happens and B1 gets printed from consumer Co-routine and producer B gets awaken since the main thread is busy in this consumer it gets scheduled
    repeats and channel is empty so consumer gets suspended.

    producer A sends A2 to the channel, since there is one receiver waiting this producer does not get suspended and continues to prints A done in this co-routine 2 and consumer gets scheduled
    producer B prints "B done" and Consumer gets redezvous and prints A2.

    Unlimited channel:

        An unlimited channel is the closest analog to a queue: producers can send elements to this channel,
        and it will grow infinitely. The send() call will never be suspended. If there's no more memory, you'll get an OutOfMemoryException.
        The difference with a queue appears when a consumer tries to receive from an empty channel and gets suspended until some new elements are sent.

   
 */