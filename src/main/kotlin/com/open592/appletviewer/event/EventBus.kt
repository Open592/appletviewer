package com.open592.appletviewer.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.CoroutineContext

/**
 * Provides a simple event bus which can emit events that can be consumed
 * by clients which extend `ApplicationEventListener`
 */
public abstract class EventBus<T : ApplicationEvent> constructor(
    coroutineContext: CoroutineContext = Dispatchers.Default
) {
    private val _events = MutableSharedFlow<T>()

    protected val scope: CoroutineScope = CoroutineScope(coroutineContext)

    public val events: SharedFlow<T> = _events.asSharedFlow()

    protected suspend fun emitEvent(event: T): Unit = _events.emit(event)
}
