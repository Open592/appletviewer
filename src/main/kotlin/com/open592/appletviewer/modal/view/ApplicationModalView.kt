package com.open592.appletviewer.modal.view

/**
 * Represents the API of the user visible portion of the ApplicationModal
 */
public interface ApplicationModalView {
    /**
     * Closes the modal.
     *
     * This should only be invoked for modals of type MESSAGE, for
     * FATAL_ERROR modals we should be terminating the application.
     */
    public fun close()

    /**
     * Displays the modal with the provided parameters.
     */
    public fun display(properties: ApplicationModalProperties)
}
