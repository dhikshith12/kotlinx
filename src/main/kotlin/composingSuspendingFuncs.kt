package composingSuspending;
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// https://kotlinlang.org/docs/composing-suspending-functions.html

//Sequential by default
/*
    Assume that we have two suspending functions defined elsewhere that do something useful like some kind of
    remote service call or computation. We just pretend they are useful, but actually each one just delays for a second
    for the purposes of this example.
 */

suspend fun doSomethingUsefulOne(): Int{
    delay(1000L);
    return 13;
}

suspend fun doSomethingUsefulTwo(): Int{
    delay(1000L);
    return 29;
}

//if you want to invoke both functions concurrently create new two concurrent co-routines, if you want to invoke them parallely create two new threads one for each
/*
What do we do if we need them to be invoked sequentially — first doSomethingUsefulOne
and then doSomethingUsefulTwo, and compute the sum of their results? In practice,
we do this if we use the result of the first function to make
a decision on whether we need to invoke the second one or to decide on how to invoke it.

We use a normal sequential invocation, because the code in the coroutine,
just like in the regular code, is sequential by default.
The following example demonstrates it by measuring the total
time it takes to execute both suspending functions:
 */
fun main() = runBlocking{
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms");
    compute();
    lazyCompute();
}

suspend fun compute() = coroutineScope{
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("async Completed in $time ms")
}

// Lazily started async
/*
Optionally, async can be made lazy by setting its start parameter to CoroutineStart.LAZY
In this mode it only starts the coroutine when its result is required by await, or if its job's start
function is invoked.
 */

suspend fun lazyCompute() = coroutineScope {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        // some computation
        one.start() // start the first one
        two.start() // start the second one
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}