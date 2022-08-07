package com.open592.appletviewer.dialog.event

import com.open592.appletviewer.dialog.ApplicationDialogType
import com.open592.appletviewer.event.ApplicationEvent

/**
 * Represents events handled by the Dialog component
 */
public sealed interface ApplicationDialogEvent : ApplicationEvent {
    /**
     * Fired when we want to display a message to the user
     *
     * When a FATAL_ERROR message is received by the ApplicationDialog component
     * it will present the user a dialog which, upon interaction, will terminate
     * the application.
     */
    public data class Display(public val type: ApplicationDialogType, public val message: String) : ApplicationDialogEvent
}
