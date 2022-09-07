package com.open592.appletviewer.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.EventListener

/**
 * The base class of all event listeners for use with EventBus
 *
 * You must provide a function which can accept all events you wish
 * to process.
 */
public abstract class ApplicationEventListener<T : ApplicationEvent>(
    private val eventBus: EventBus<T>
) : EventListener {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    init {
        scope.launch {
            eventBus.events.collect {
                processEvent(it)
            }
        }
    }

    /**
     * Processes one of the events which can be emitted by the associated
     * EventBus
     */
    protected abstract suspend fun processEvent(event: T)
}
