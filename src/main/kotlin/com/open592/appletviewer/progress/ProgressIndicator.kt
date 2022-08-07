package com.open592.appletviewer.progress

import com.open592.appletviewer.dialog.ApplicationDialogType
import com.open592.appletviewer.dialog.event.ApplicationDialogEventBus
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.progress.event.ProgressEvent
import com.open592.appletviewer.progress.event.ProgressEventBus
import com.open592.appletviewer.progress.view.ProgressIndicatorView
import java.awt.Frame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicator @Inject constructor(
    eventBus: ProgressEventBus,
    private val applicationDialogEventBus: ApplicationDialogEventBus,
    private val view: ProgressIndicatorView
) : ApplicationEventListener<ProgressEvent>(eventBus) {
    public fun initialize(parentFrame: Frame) {
        view.initialize(parentFrame)

        applicationDialogEventBus.dispatchDisplayEvent(ApplicationDialogType.MESSAGE, "Hello world")
        applicationDialogEventBus.dispatchDisplayEvent(ApplicationDialogType.FATAL_ERROR, "Uh oh\nNot good")
    }

    protected override fun processEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.ChangeVisibility -> view.changeVisibility(event.visible)
            is ProgressEvent.ChangeText -> view.changeText(event.text)
            is ProgressEvent.UpdateProgress -> handleUpdateProgressEvent(event)
        }
    }

    private fun handleUpdateProgressEvent(event: ProgressEvent.UpdateProgress) {
        if (event.percentage > 100) {
            view.setProgress(100)

            return
        }

        view.setProgress(event.percentage)
    }
}
