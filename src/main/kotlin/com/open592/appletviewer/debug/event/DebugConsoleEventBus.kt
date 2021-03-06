package com.open592.appletviewer.debug.event

import com.open592.appletviewer.debug.capture.CapturedMessage
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
public class DebugConsoleEventBus : EventBus<DebugConsoleEvent>() {
    private val scope = CoroutineScope(Dispatchers.Default)

    public fun dispatchMessageReceivedEvent(message: CapturedMessage) {
        scope.launch {
            emitEvent(DebugConsoleEvent.MessageReceived(message))
        }
    }
}
