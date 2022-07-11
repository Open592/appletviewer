package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.OutputCapture
import com.open592.appletviewer.debug.capture.OutputCaptureEvent
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.event.EventBus
import com.open592.appletviewer.settings.SettingsStore
import java.awt.Frame
import java.awt.TextArea
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DebugConsole @Inject constructor(
    eventBus: EventBus<OutputCaptureEvent>,
    private val outputCapture: OutputCapture,
    private val settings: SettingsStore
): ApplicationEventListener<OutputCaptureEvent>(eventBus) {
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

    public override fun processEvent(event: OutputCaptureEvent) {
        when(event) {
            is OutputCaptureEvent.MessageReceived -> handleMessageReceived(event)
        }
    }

    private fun getFrame() : Frame {
        if (!this::frame.isInitialized) {
            textArea = TextArea()
            textArea.isEditable = false

            frame = Frame()
            frame.add(textArea, "Center")
            frame.title = TITLE
            frame.setLocation(320, 240)
            frame.setSize(720, 260)
            frame.addWindowListener(WindowListener())
        }

        return frame
    }

    private fun handleMessageReceived(event: OutputCaptureEvent.MessageReceived) {
        textArea.append(event.capture.message)
    }

    private inner class WindowListener : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            getFrame().isVisible = false
        }
    }

    private companion object {
        private const val TITLE = "Jagex host console"
    }
}
