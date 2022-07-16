package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.CapturedMessage
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DebugConsoleEventEmitter @Inject constructor(
    private val eventBus: EventBus<DebugConsoleEvent>
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    public fun emit(message: CapturedMessage) {
        val handler = CoroutineExceptionHandler { _, _ -> } // Ignored
        val event = DebugConsoleEvent.MessageReceived(message)

        scope.launch(handler) {
            eventBus.emitEvent(event)
        }
    }
}
