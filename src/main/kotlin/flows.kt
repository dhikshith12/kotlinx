package flows
import channels.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking<Unit>{
   val sum = (1..5).asFlow()
       .map { it*it }
       .reduce { a,b -> a+b  }
    println(sum)
    seq()
    simple().collect{ value -> log("Collected $value")}
    simple2().collect{ value -> log("Collected $value")}
    val time = measureTimeMillis {
        simpleSlow()
            .collect{
                delay(300)
                println(it)
            }
    }
    println("Collected in $time ms")
    val nums = (1..3).asFlow()
    val strs = flowOf("one","two","three")
    zipper(nums, strs)

    val numsSlow = (1..3).asFlow().onEach { delay(300L) }
    val strsSlow = flowOf("one","two","three").onEach { delay(400) }
    var startTime = System.currentTimeMillis()
    numsSlow.combine(strsSlow){a,b->"$a -> $b"}
        .collect{
            println("$it at ${System.currentTimeMillis()-startTime}")
        }
    startTime = System.currentTimeMillis()
    //has same effect as .map{transform(T)}.flatConcat{} where map transforms a Flow into Flow<Flow<T>> and flacConcat flattens flow of flows.
    nums.onEach { delay(100L) }
        .flatMapConcat { requestFlow(it) }
        .collect{
            println("$it at ${System.currentTimeMillis()-startTime} ms from start")
        }
    println("===============flatMapMerge=============")
    startTime = System.currentTimeMillis()
    nums.onEach { delay(100) }
        .flatMapMerge { requestFlow(it) }
        .collect{
            println("$it at ${System.currentTimeMillis()-startTime} ms from start")
        }
}

suspend fun zipper(nums: Flow<Int>, strs: Flow<String>) {
    nums.zip(strs) { a, b -> "$a -> $b" }
        .collect { println(it) }
}

// Flows are sequential each item is sequentially going through all intermediate operators from upstream to downstream and then delivered
// to the terminal operator after.
suspend fun seq() = coroutineScope {
    (1..5).asFlow()
        .filter {
            println("Filter $it")
            it%2==0
        }
        .map{
            println("Map $it")
            "string $it"
        }.collect{
            println("Collect $it")
        }
}

fun simple(): Flow<Int> = flow{
    log("Started simple flow")
    for(i in 1..3){
        log("emmiting $i")
        emit(i)
    }
}
/*
    For the long-running CPU-consuming code might need to be executed in the context of
    Dispatchers.Deafult and UI-updating code might need to be executed in the context of
    Dispatchers.Main. Usually, withContext is used to change the context in the code using Kotlin co-routines,
    but code in flow{...} builder has to honor the context preservation property and is not allowed to emit from a
    different context.
 */
// throws an Exception flow invariant is violated
suspend fun simpleErr(): Flow<Int> = flow{
    withContext(Dispatchers.Default){
        for(i in 1..3){
            Thread.sleep(100)
            emit(i)
        }
    }
}
// use flowOn(context) to switch context for long running flow items so that ui-thread is performant
suspend fun simple2():Flow<Int> = flow{
    for(i in 1..5){
        delay(100)
        log("emmiting $i");
        emit(i);
    }
}.flowOn(Dispatchers.Default)
/*
Notice how flow{...} works in the background thread, while collection happens in the main thread:
    Another thing to observe here is that the flowOn operator has changed the default sequential nature
    of the flow. Now collection happens in one coroutine("coroutine#1) and emission happens in another
    coroutine("Coroutine#2) that is running in another thread concurrently with the collecting coroutine.
    The flowOn operator creates another coroutine for an upstream flow when it has to change the CoroutineDispatcher
    in its context
 */

/*  =============Buffering================
    Running different parts of a flow in different coroutines can be helpful from the standpoint of
    the overall time it takes to collect the flow, especially when long-running asynchronous operations
    are involved.

    we can use a buffer operator on a flow to run emitting code of the simpleSlow flow
    concurrently with collecting code, as opposed to running them sequentialy.
 */

fun simpleSlow():Flow<Int> = flow{
   for(i in 1..3) {
       delay(100)
       emit(i)
   }
}

/* ===============Zipping=====================
    takes two flows and zips thems,
    items in the new flow after zipping are as slow as the slower flow between both flows.

    combine can be helpful when computing a value where both flows represents updating values
    and by combining them if at least one flow emits new value it computes new result by combining
 */

/*  =============Flattening Flows================
    Flows represent asynchronously received sequences of values, so it is quite easy to get in a
    situation where each value triggers a request for another sequence of values. For example, We can
    have the following function that returns a flow of two strings 500 ms apart:
*/
fun requestFlow(i: Int):Flow<String> = flow{
    emit("$i: First")
    delay(200)
    emit("$i: Second")
}
/*
    Now if we have a flow of three integers and call requestFlow for each of them like this:
        (1..3).asFlow().map{ requestFlow(it) }
    Then we end up with a flow of Flows(Flow<Flow<String>>) that needs to be flattened into a single flow
    for further processing. Collections and sequences have flatten and flatMap modes of flattening, as such,
    there is a family of flattening operators on flows.
 */