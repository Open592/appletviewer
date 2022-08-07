package com.open592.appletviewer.progress.view

import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.viewer.event.ViewerEventBus
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Font
import java.awt.Frame
import java.awt.Graphics
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicatorComponent @Inject constructor(
    private val viewerEventBus: ViewerEventBus,
    localization: Localization
) : Component(), ProgressIndicatorView {
    private lateinit var dialog: Dialog
    private var currentProgress = 0
    private var currentContent = localization.getContent("loaderbox_initial")
    private val fontMetrics = this.getFontMetrics(TEXT_FONT)

    public override fun initialize(parentFrame: Frame) {
        dialog = Dialog(parentFrame, WINDOW_TITLE, false)

        dialog.add(this)
        dialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                viewerEventBus.dispatchQuitEvent()
            }
        })
        dialog.isResizable = false
        dialog.setSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        dialog.setLocationRelativeTo(parentFrame)
    }

    public override fun paint(g: Graphics?) {
        try {
            if (g == null) {
                this.repaint()

                return
            }

            val content = "${this.currentContent} - ${this.currentProgress}%"

            g.color = Color.BLACK
            g.fillRect(0, 0, this.width, this.height)
            g.color = RED_COLOR
            g.drawRect((this.width / 2) - 152, (this.height / 2) - 18, 303, 33)
            g.fillRect((this.width / 2) - 150, (this.height / 2) - 16, this.currentProgress * 300 / 100, 30)
            g.font = TEXT_FONT
            g.color = Color.WHITE
            g.drawString(content, (this.width - this.fontMetrics.stringWidth(content)) / 2, 4 + this.height / 2)
        } catch (_: Exception) {
            // Ignored
        }
    }

    public override fun changeVisibility(visible: Boolean) {
        dialog.isVisible = visible
    }

    public override fun changeText(text: String) {
        this.currentContent = text

        this.repaint()
    }

    public override fun setProgress(percentage: Int) {
        this.currentProgress = percentage

        this.repaint()
    }

    private companion object {
        private val RED_COLOR = Color(140, 11, 1)
        private const val WINDOW_TITLE = "Jagex Ltd."
        private const val WINDOW_WIDTH = 320
        private const val WINDOW_HEIGHT = 100
        private val TEXT_FONT = Font("Helvetica", Font.BOLD, 13)
    }
}