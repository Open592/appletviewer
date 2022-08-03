package com.open592.appletviewer.debug.event

import com.open592.appletviewer.debug.capture.CapturedMessage
import com.open592.appletviewer.event.ApplicationEvent

/**
 * Represents events emitted from DebugConsole.
 */
public sealed interface DebugConsoleEvent : ApplicationEvent {
    /**
     * This is an event fired when we have received a message from the output capture.
     *
     * All types of messages will fire this event. It is up to the consumer to filter if applicable.
     */
    public data class MessageReceived(val capture: CapturedMessage) : DebugConsoleEvent
}
