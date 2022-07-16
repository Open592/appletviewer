package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.CapturedMessage
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class DebugConsoleEventBus : EventBus<DebugConsoleEvent>() {
    private val scope = CoroutineScope(Dispatchers.Default)
    public fun dispatchMessageReceived(message: CapturedMessage) {
        scope.launch {
            emitEvent(DebugConsoleEvent.MessageReceived(message))
        }
    }
}
