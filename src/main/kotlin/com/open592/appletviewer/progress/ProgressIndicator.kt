package com.open592.appletviewer.progress

import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.progress.event.ProgressEvent
import com.open592.appletviewer.progress.event.ProgressEventBus
import com.open592.appletviewer.progress.view.ProgressIndicatorView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicator @Inject constructor(
    public val eventBus: ProgressEventBus,
    private val view: ProgressIndicatorView
) : ApplicationEventListener<ProgressEvent>(eventBus) {
    protected override suspend fun processEvent(event: ProgressEvent) {
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
