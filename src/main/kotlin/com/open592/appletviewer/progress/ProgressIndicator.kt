package com.open592.appletviewer.progress

import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.progress.view.ProgressIndicatorView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ProgressIndicator
    @Inject
    constructor(
        private val eventBus: GlobalEventBus,
        private val view: ProgressIndicatorView,
    ) {
        /**
         * Initialize starts listening for the events which will be controlling the progress indicator view
         */
        public fun initialize() {
            eventBus.listen<ProgressEvent> {
                when (it) {
                    is ProgressEvent.ChangeVisibility -> view.changeVisibility(it.visible)
                    is ProgressEvent.ChangeText -> view.changeText(it.text)
                    is ProgressEvent.UpdateProgress -> handleUpdateProgressEvent(it)
                }
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
