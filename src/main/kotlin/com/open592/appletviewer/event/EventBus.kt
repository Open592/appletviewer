package com.open592.appletviewer.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Provides a simple event bus which can emit events that can be consumed
 * by clients which extend `ApplicationEventListener`
 */
public abstract class EventBus<T : ApplicationEvent> {
    private val _events = MutableSharedFlow<T>()
    protected val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    public val events: SharedFlow<T> = _events.asSharedFlow()

    public suspend fun emitEvent(event: T): Unit = _events.emit(event)
}
