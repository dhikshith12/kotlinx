package channels
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

            val unlimitedChannel = Channel<T>(UNLIMITED)

   Buffered channel:

    The size of a buffered channel is constrained by the specified number.
    Producers can send elements to this channel until the size limit is reached.
    All the elements are internally stored. When the channel is full, the next `send`
    call on it suspends until more free space appears.

           val bufferedChannel = Channel<T>(size)

    Rendezvous channel:

    The "Rendezvous" channel is a channel without a buffer.
    It's the same as creating a buffered channel with a zero size.
    One of the functions (send() or receive()) always gets suspended until the other is called.
    If the send() function is called and there's no suspended receive call ready to process the element,
    send() suspends. Similarly, if the receive function is called and the channel is empty or, in other words,
    there's no suspended send() call ready to send the element, the receive() call suspends.

    The "rendezvous" name ("a meeting at an agreed time and place") refers to the fact that send() and receive() should "meet on time".

           val rendezvousChannel = Channel<T>()

    Conflated channel:

    A new element sent to the conflated channel will overwrite the previously sent element,
    so the receiver will always get only the latest element. The send() call never suspends.

           val conflatedChannel = Channel<T>(CONFLATED)


    By default, a "Rendezvous" channel is created.
 */