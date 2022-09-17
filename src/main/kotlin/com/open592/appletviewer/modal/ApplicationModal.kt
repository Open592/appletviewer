package com.open592.appletviewer.modal

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.modal.event.ApplicationModalEvent
import com.open592.appletviewer.modal.event.ApplicationModalEventBus
import com.open592.appletviewer.modal.view.ApplicationModalProperties
import com.open592.appletviewer.modal.view.ApplicationModalView
import com.open592.appletviewer.viewer.event.ViewerEventBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationModal @Inject constructor(
    public val eventBus: ApplicationModalEventBus,
    private val configuration: ApplicationConfiguration,
    private val view: ApplicationModalView,
    private val viewerEventBus: ViewerEventBus
) : ApplicationEventListener<ApplicationModalEvent>(eventBus) {
    protected override fun processEvent(event: ApplicationModalEvent) {
        when (event) {
            is ApplicationModalEvent.Display -> handleDisplayEvent(event)
        }
    }

    private fun handleDisplayEvent(event: ApplicationModalEvent.Display) {
        when (event.type) {
            ApplicationModalType.FATAL_ERROR -> displayFatalErrorModal(event.message)
            ApplicationModalType.MESSAGE -> displayMessageModal(event.message)
        }
    }

    private fun displayFatalErrorModal(message: String) {
        view.display(
            ApplicationModalProperties(
                type = ApplicationModalType.FATAL_ERROR,
                content = parseMessage(message),
                title = configuration.getContent("error"),
                buttonText = configuration.getContent("quit"),
                closeAction = viewerEventBus::dispatchQuitEvent
            )
        )
    }

    private fun displayMessageModal(message: String) {
        view.display(
            ApplicationModalProperties(
                type = ApplicationModalType.MESSAGE,
                content = parseMessage(message),
                title = configuration.getContent("message"),
                buttonText = configuration.getContent("ok"),
                closeAction = view::close
            )
        )
    }

    private fun parseMessage(message: String): List<String> {
        return message.split("\n", ignoreCase = true)
    }
}
