package com.open592.appletviewer.modal.view

import com.open592.appletviewer.frame.RootFrame
import java.awt.BorderLayout
import java.awt.Button
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Label
import java.awt.Panel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationModalComponent @Inject constructor(
    rootFrame: RootFrame
) : ApplicationModalView {
    private val modal: Dialog = Dialog(rootFrame.getFrame(), Dialog.DEFAULT_MODALITY_TYPE)

    init {
        modal.size = Dimension(MODAL_WIDTH, MODAL_HEIGHT)
        modal.isResizable = false
        modal.setLocationRelativeTo(rootFrame.getFrame())
    }

    public override fun close() {
        // Remove all child components
        modal.removeAll()

        // Remove existing window listeners
        modal.windowListeners.forEach {
            modal.removeWindowListener(it)
        }

        modal.isVisible = false
    }

    public override fun display(properties: ApplicationModalProperties) {
        modal.title = properties.title

        modal.add(createMessagePanel(properties.content), BorderLayout.CENTER)
        modal.add(createButtonPanel(properties.buttonText, properties.closeAction), BorderLayout.SOUTH)

        modal.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                properties.closeAction()
            }
        })

        modal.isVisible = true
    }

    private fun createButtonPanel(buttonText: String, closeAction: () -> Unit): Panel {
        val panel = Panel()

        panel.layout = FlowLayout(FlowLayout.CENTER)

        val button = Button(buttonText)

        button.addActionListener {
            closeAction()
        }

        panel.add(button)

        return panel
    }

    private fun createMessagePanel(content: List<String>): Panel {
        val panel = Panel()

        panel.layout = GridLayout(content.size, 1)

        content.forEach {
            val label = Label(it, Label.CENTER)

            panel.add(label)
        }

        return panel
    }

    private companion object {
        private const val MODAL_WIDTH = 500
        private const val MODAL_HEIGHT = 100
    }
}
