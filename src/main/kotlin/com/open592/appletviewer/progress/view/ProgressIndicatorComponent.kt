package com.open592.appletviewer.progress.view

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.root.Root
import com.open592.appletviewer.viewer.event.ViewerEventBus
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Font
import java.awt.Frame
import java.awt.Graphics
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicatorComponent @Inject constructor(
    config: ApplicationConfiguration,
    @Root private val rootFrame: Frame,
    private val viewerEventBus: ViewerEventBus
) : Component(), ProgressIndicatorView {
    private val dialog: Dialog = Dialog(rootFrame, WINDOW_TITLE, false)
    private val fontMetrics = this.getFontMetrics(TEXT_FONT)

    private var currentProgress = 0
    private var currentContent = config.getContent("loaderbox_initial")

    init {
        dialog.add(this)
        dialog.isResizable = false
        dialog.setSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        dialog.setLocationRelativeTo(rootFrame)

        dialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                viewerEventBus.dispatchQuitEvent()
            }
        })
    }

    public override fun paint(g: Graphics?) {
        if (g == null) {
            this.repaint()

            return
        }

        try {
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
        this.dialog.isVisible = visible
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
