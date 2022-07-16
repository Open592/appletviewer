package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.OutputCapture
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.settings.SettingsStore
import java.awt.Frame
import java.awt.TextArea
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DebugConsole @Inject constructor(
    eventBus: DebugConsoleEventBus,
    private val outputCapture: OutputCapture,
    private val settings: SettingsStore
) : ApplicationEventListener<DebugConsoleEvent>(eventBus) {
    /**
     * In the original implementation the debug console is not visible until
     * the first message has been received.
     *
     * We lazy initialize the debug console to keep with the original behavior
     */
    private lateinit var frame: Frame
    private lateinit var textArea: TextArea

    // The entry point of the debug console.
    public fun start() {
        val shouldStart = settings.getBoolean("com.jagex.debug")

        // Nothing to do if we aren't running in debug mode
        if (!shouldStart) {
            return
        }

        val shouldLogToSystemStream = settings.getBoolean("com.open592.debugConsoleLogToSystemStream")

        // Start capturing logs sent to:
        // - System.out
        // - System.err
        outputCapture.capture(shouldLogToSystemStream)
    }

    protected override fun processEvent(event: DebugConsoleEvent) {
        when (event) {
            is DebugConsoleEvent.MessageReceived -> handleMessageReceived(event)
        }
    }

    private fun initialize() {
        textArea = TextArea()
        textArea.isEditable = false

        frame = Frame()
        frame.add(textArea, "Center")
        frame.title = TITLE
        frame.setLocation(320, 240)
        frame.setSize(720, 260)
        frame.addWindowListener(WindowListener())
    }

    private fun handleMessageReceived(event: DebugConsoleEvent.MessageReceived) {
        // When receiving the first message we initialize the debug console
        if (!this::frame.isInitialized) {
            initialize()
        }

        textArea.append(event.capture.message)
    }

    private inner class WindowListener : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
            if (::frame.isInitialized) {
                frame.isVisible = false
            }
        }
    }

    private companion object {
        private const val TITLE = "Jagex host console"
    }
}
