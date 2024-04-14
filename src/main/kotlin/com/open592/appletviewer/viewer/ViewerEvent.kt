package com.open592.appletviewer.viewer

import com.open592.appletviewer.events.ApplicationEvent

/**
 * Represents events sent to the root viewer frame.
 */
public sealed interface ViewerEvent : ApplicationEvent {
    /**
     * This event is fired when the caller wishes to inform the applet viewer
     * that it should terminate.
     */
    public data object Quit : ViewerEvent
}
