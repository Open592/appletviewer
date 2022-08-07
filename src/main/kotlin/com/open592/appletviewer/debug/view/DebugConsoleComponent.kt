package com.open592.appletviewer.debug.view

import java.awt.Frame
import java.awt.TextArea
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

public class DebugConsoleComponent : DebugConsoleView {
    private val frame: Frame = Frame()
    private val textArea: TextArea = TextArea()

    init {
        textArea.isEditable = false
        frame.add(textArea, "Center")

        frame.title = WINDOW_TITLE
        frame.setLocation(320, 240)
        frame.setSize(720, 260)
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                if (frame.isVisible) {
                    frame.isVisible = false
                }
            }
        })
    }

    public override fun display() {
        frame.isVisible = true
    }

    public override fun isDisplayed(): Boolean {
        return frame.isVisible
    }

    public override fun appendMessage(message: String) {
        // It is the responsibility of the controller to initialize before appending a message
        assert(frame.isVisible) {
            "Attempted to append message to DebugConsole before initialization"
        }

        textArea.append(message)
    }

    private companion object {
        private const val WINDOW_TITLE = "Jagex host console"
    }
}
