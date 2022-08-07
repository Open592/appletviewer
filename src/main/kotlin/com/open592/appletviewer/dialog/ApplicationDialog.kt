package com.open592.appletviewer.dialog

import com.open592.appletviewer.dialog.event.ApplicationDialogEvent
import com.open592.appletviewer.dialog.event.ApplicationDialogEventBus
import com.open592.appletviewer.dialog.view.ApplicationDialogView
import com.open592.appletviewer.dialog.view.DialogProperties
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.viewer.event.ViewerEventBus
import java.awt.Frame
import java.security.PrivateKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationDialog @Inject constructor(
    eventBus: ApplicationDialogEventBus,
    private val localization: Localization,
    private val view: ApplicationDialogView,
    private val viewerEventBus: ViewerEventBus
) : ApplicationEventListener<ApplicationDialogEvent>(eventBus) {
    private lateinit var parentFrame: Frame

    public fun initialize(parent: Frame) {
        parentFrame = parent
    }

    protected override fun processEvent(event: ApplicationDialogEvent) {
        when(event) {
            is ApplicationDialogEvent.Display -> handleDisplayEvent(event)
        }
    }

    private fun handleDisplayEvent(event: ApplicationDialogEvent.Display) {
        when (event.type) {
            ApplicationDialogType.MESSAGE -> showMessage(event.message)
            ApplicationDialogType.FATAL_ERROR -> showFatalError(event.message)
        }
    }

    private fun parseMessage(message: String): List<String> {
        return message.split("\n", ignoreCase = false)
    }

    private fun showMessage(message: String) {
        (DialogProperties(
            type = ApplicationDialogType.MESSAGE,
            lines = parseMessage(message),
            title = localization.getContent("message"),
            buttonText = localization.getContent("ok"),
            closeAction = {
                view.close()
            }
        ))
    }

    private fun showFatalError(message: String) {
        view.display(DialogProperties(
            type = ApplicationDialogType.FATAL_ERROR,
            lines = parseMessage(message),
            title = localization.getContent("error"),
            buttonText = localization.getContent("quit"),
            closeAction = {
                // When closing a fatal error we should quit the applet viewer
                viewerEventBus.dispatchQuitEvent()
            }
        ))
    }
}
