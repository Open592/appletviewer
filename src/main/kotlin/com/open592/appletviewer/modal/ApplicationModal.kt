package com.open592.appletviewer.modal

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.view.ApplicationModalProperties
import com.open592.appletviewer.modal.view.ApplicationModalView
import com.open592.appletviewer.progress.ProgressEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationModal
@Inject
constructor(
    private val config: ApplicationConfiguration,
    private val eventBus: GlobalEventBus,
    private val view: ApplicationModalView,
) {
    public fun displayFatalErrorModal(message: String) {
        // When displaying a FATAL_ERROR we need to hide the progress indicator
        eventBus.dispatch(ProgressEvent.ChangeVisibility(false))

        view.display(
            ApplicationModalProperties(
                content = parseMessage(message),
                title = config.getContent("error"),
                buttonText = config.getContent("quit"),
                closeAction = view::quit,
            ),
        )
    }

    public fun displayMessageModal(message: String) {
        view.display(
            ApplicationModalProperties(
                content = parseMessage(message),
                title = config.getContent("message"),
                buttonText = config.getContent("ok"),
                closeAction = view::close,
            ),
        )
    }

    private fun parseMessage(message: String): List<String> {
        return message.split("\\n", ignoreCase = true)
    }
}
