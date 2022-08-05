package com.open592.appletviewer.viewer

import com.open592.appletviewer.debug.DebugConsole
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.progress.ProgressIndicator
import com.open592.appletviewer.viewer.event.ViewerEvent
import com.open592.appletviewer.viewer.event.ViewerEventBus
import java.awt.Frame
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

@Singleton
public class Viewer @Inject constructor(
    private val eventBus: ViewerEventBus,
    private val debugConsole: DebugConsole,
    private val localization: Localization,
    private val progressIndicator: ProgressIndicator
) : ApplicationEventListener<ViewerEvent>(eventBus) {
    private val frame = Frame()

    public fun initialize() {
        // Initialize the debug console in case we are in debug mode
        debugConsole.initialize()

        // Initialize the progress indicator
        progressIndicator.initialize(frame)

        println(localization.getContent("err_missing_config"))
    }

    protected override fun processEvent(event: ViewerEvent) {
        when (event) {
            is ViewerEvent.Quit -> handleQuitEvent()
        }
    }

    private fun handleQuitEvent() {
        exitProcess(0)
    }
}
