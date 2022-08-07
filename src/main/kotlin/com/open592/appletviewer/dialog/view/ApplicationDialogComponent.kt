package com.open592.appletviewer.dialog.view

import java.awt.Button
import java.awt.Dialog
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Frame
import java.awt.FlowLayout
import java.awt.Label
import java.awt.Panel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

public class ApplicationDialogComponent constructor(
    parentFrame: Frame,
    private val properties: DialogProperties
) : Dialog(parentFrame, true), ApplicationDialogView {
    init {
        this.size = Dimension(DIALOG_WIDTH, DIALOG_HEIGHT)
        this.isResizable = false
        this.setLocationRelativeTo(parentFrame)
        this.title = properties.title

        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                properties.closeAction()
            }
        })
    }

    public override fun display(properties: DialogProperties) {
        // Add message to dialog
        this.add(createMessagePanel(), "Center")

        // Add button to dialog
        this.add(createButtonPanel())

        this.isVisible = true
    }

    public override fun close() {
        this.dispose()
    }

    private fun createButtonPanel(): Panel {
        val panel = Panel()

        panel.layout = FlowLayout(FlowLayout.CENTER)

        val button = Button(properties.buttonText)

        button.addActionListener {
            properties.closeAction()
        }

        panel.add(button, "South")

        return panel
    }

    private fun createMessagePanel(): Panel {
        val panel = Panel()

        panel.layout = GridLayout(properties.lines.size, 1)

        properties.lines.forEach {
            val label = Label(it, Label.CENTER)

            panel.add(label)
        }

        return panel
    }

    private companion object {
        private const val DIALOG_WIDTH = 500
        private const val DIALOG_HEIGHT = 100
    }
}
