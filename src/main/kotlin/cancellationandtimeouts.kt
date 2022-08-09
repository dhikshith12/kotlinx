package blocking;
import kotlinx.coroutines.*
/*fun main() = runBlocking{
    val job = launch{
        repeat(1000){
            log("job: I'm sleeping $it...")
            delay(500L)
        }
    }
    delay(1300L)
    log("I'm Tired of waiting")
    job.cancel()
    log("Now I can quit.")

    /*=======   cancellation of co-routines    =====*/
    cancelleble()
}
fun main() = runBlocking{
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default){
        var nextPrintTime = startTime
        var i = 0
        while(isActive){
            if(System.currentTimeMillis()>=nextPrintTime){
                log("I'm sleeping ${i++}...")
                nextPrintTime+=500L
            }
        }
    }
    delay(1300)
    log("I'm tired of waiting!")
    job.cancel()
    log("Now I can quit")
}*/

var acquired = 0

class Resource {
    init { acquired++ } // Acquire the resource
    fun close() { acquired-- } // Release the resource
}

fun main() {
    runBlocking {
        repeat(100_000) { // Launch 100K coroutines
            launch {
                val resource = withTimeout(60) { // Timeout of 60 ms
                    delay(50) // Delay for 50 ms
                    Resource() // Acquire a resource and return it from withTimeout block
                }
                resource.close() // Release the resource
            }
        }
    }
    // Outside of runBlocking all coroutines have completed
    println(acquired) // Print the number of resources still acquired
}