package com.open592.appletviewer.progress.view

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.frame.RootFrame
import com.open592.appletviewer.viewer.ViewerEvent
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicatorComponent @Inject constructor(
    config: ApplicationConfiguration,
    rootFrame: RootFrame,
    private val eventBus: GlobalEventBus
) : Component(), ProgressIndicatorView {
    private val dialog: Dialog = Dialog(rootFrame.getFrame(), WINDOW_TITLE, false)
    private val fontMetrics = this.getFontMetrics(TEXT_FONT)

    private var currentProgress = 0
    private var currentContent = config.getContent("loaderbox_initial")

    init {
        dialog.add(this)
        dialog.background = Color.BLACK
        dialog.isResizable = false
        dialog.minimumSize = Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
        dialog.setLocationRelativeTo(rootFrame.getFrame())

        dialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                eventBus.dispatch(ViewerEvent.Quit)
            }
        })
    }

    public override fun paint(g: Graphics?) {
        if (g == null) {
            this.repaint()

            return
        }

        val content = "${this.currentContent} - ${this.currentProgress}%"

        try {
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
        if (visible) {
            // If we had previously called `dispose()` below, because we retained the reference,
            // `isVisible = true` will make the Dialog displayable again before showing the dialog.
            dialog.isVisible = true

            // We keep state within this class, so upon repainting the progress state will be retained
            // even if the underlying Dialog had to be recreated.
            this.repaint()
        } else {
            // There is only a small edge case (switching servers) where we will ever want to make
            // this displayable again. And on Linux calling `dispose()` instead of `isVisible = false`
            // solves an issue with a race condition during `displayFatalErrorModal`.
            dialog.dispose()
        }
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

        // Rendering changed quite substantially after Java 1.6 thus our dimensions differ
        // from the original implementation. These changes result in our indicator matching
        // the original as close as possible.
        private const val WINDOW_WIDTH = 330
        private const val WINDOW_HEIGHT = 110
        private val TEXT_FONT = Font("Helvetica", Font.BOLD, 13)
    }
}
