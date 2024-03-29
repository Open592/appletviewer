package com.open592.appletviewer.events

import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

@Singleton
public class GlobalEventBus(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) {
    private val events = MutableSharedFlow<ApplicationEvent>(extraBufferCapacity = 1)

    public fun <T : ApplicationEvent> dispatch(event: T) {
        events.tryEmit(event)
    }

    @Throws(RuntimeException::class)
    public inline fun <reified T : ApplicationEvent> listen(noinline listener: suspend (T) -> Unit): Job {
        return listen(T::class, listener)
    }

    @Throws(RuntimeException::class)
    public fun <T : ApplicationEvent> listen(
        type: KClass<T>,
        listener: suspend (T) -> Unit,
    ): Job {
        return retrieveEvent(type)
            .onEach(listener)
            .launchIn(scope)
    }

    /**
     * Retrieves events of a particular type
     */
    private fun <T : ApplicationEvent> retrieveEvent(type: KClass<T>): Flow<T> {
        // Cast the abstract ApplicationEvent into it's concrete class
        @Suppress("UNCHECKED_CAST")
        return events.filter { type.isInstance(it) } as Flow<T>
    }
}
