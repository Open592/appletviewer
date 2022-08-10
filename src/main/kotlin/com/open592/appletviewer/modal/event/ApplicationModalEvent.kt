package com.open592.appletviewer.modal.event

import com.open592.appletviewer.event.ApplicationEvent
import com.open592.appletviewer.modal.ApplicationModalType

/**
 * Represents an event handled by the ApplicationModal component.
 */
public sealed interface ApplicationModalEvent : ApplicationEvent {
    /**
     * Fired when we want to display a modal to the user.
     *
     * When this is invoked the application blocks until the user acknowledges
     * the event.
     *
     * In the case of a FATAL_ERROR message, acknowledgment of the modal will
     * terminate the application.
     */
    public data class Display(public val type: ApplicationModalType, public val message: String) : ApplicationModalEvent
}
