package dispatchersAndThreads;
import kotlinx.coroutines.*

fun main() = runBlocking<Unit>{
    launch {
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}
/*
    When launch{...} is used without parameters, it inherits the context(and thus dispatcher)
    from the CoroutineScope it is being launched from. In this case, it inherits the context of
    the main runBlocking coroutine which runs in the main thread.

    Dispatchers.Unconfined is a special dispatcher that also appears to run in the main thread,
    but it is, in fact, a different mechanism that is explained later.

    The default dispatcher is used when no other dispatcher is explicitly specified in the scope.
    It is represented by Dispatchers.Default and uses a shared background pool of threads.

    newSingleThreadContext creates a thread for the coroutine to run. A dedicated thread is a very
    expensive resource. In a real application it must be either released, when no longer needed,
    using the close function, or stored in a top-level variable and resued throughout the application.
 */
