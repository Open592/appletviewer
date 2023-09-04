package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.OutputCapture
import com.open592.appletviewer.debug.view.DebugConsoleView
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DebugConsole
@Inject
constructor(
    private val eventBus: GlobalEventBus,
    private val view: DebugConsoleView,
    private val outputCapture: OutputCapture,
    private val settings: SettingsStore,
) {
    /**
     * Entry point of the DebugConsole
     *
     * This function is responsible for determining if we should load the debug console,
     * as well as starting the output capture.
     *
     * The output capture will be responsible for capturing all relevant output and emitting
     * DebugConsoleEvents when output is received.
     *
     * On receiving output we need to display the view and append the message.
     */
    public fun initialize() {
        val isDebug = settings.getBoolean(SettingsStore.IS_DEBUG_KEY)
        val isDisabled = settings.getBoolean("com.open592.disableDebugConsole")

        if (!isDebug || isDisabled) {
            return
        }

        startEventListener()

        // Should we continue to log to the console?
        val shouldLogToSystemStream = settings.getBoolean("com.open592.debugConsoleLogToSystemStream")

        // Start capturing logs sent to:
        // - System.out
        // - System.err
        outputCapture.capture(shouldLogToSystemStream)
    }

    private fun startEventListener() {
        eventBus.listen<DebugConsoleEvent> {
            when (it) {
                is DebugConsoleEvent.MessageReceived -> handleMessageReceived(it)
            }
        }
    }

    private fun handleMessageReceived(event: DebugConsoleEvent.MessageReceived) {
        if (!view.isDisplayed()) {
            view.display()
        }

        view.appendMessage(event.capture.message)
    }
}
