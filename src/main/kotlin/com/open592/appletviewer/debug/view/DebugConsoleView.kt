package com.open592.appletviewer.debug.view

/**
 * Represents the API of the debug console.
 *
 * These functions expose the user visible functionality of the debug console
 */
public interface DebugConsoleView {
    /**
     * Display the debug console to the user.
     *
     * This should only be done after the first message is captured.
     *
     * In the original applet viewer the entire class wasn't initialized until the first message
     * was loaded, but it's fine to eager load the frame in our implementation as the user experience
     * isn't changed.
     */
    public fun display()

    /**
     * Represents if the view is displayed to the user.
     *
     * An interesting tidbit: In the original debug console implementation, closing the debug console
     * did not stop any of the underlying functionality (output capturing, etc.) and there wasn't a
     * separate flag specifying if the debug console was manually closed by the user. When the user
     * manually closed the debug console all that happened was the frame was hidden, and an "initialized"
     * flag was set to false. When a new message was received, the "initialized" flag was checked, and
     * if `false` the frame was re-created, and the debug console shown again.
     *
     * In order to keep the original behavior, we too re-open the debug console on receiving a new message,
     * but unlike the original we don't re-create the entire component.
     */
    public fun isDisplayed(): Boolean

    /**
     * Called when a new message is captured.
     *
     * Each message should be a separate line, and that is enforced within OutputCapture
     */
    public fun appendMessage(message: String)
}
