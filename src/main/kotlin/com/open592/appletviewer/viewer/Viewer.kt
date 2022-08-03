package com.open592.appletviewer.viewer

import com.open592.appletviewer.debug.DebugConsole
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.viewer.event.ViewerEvent
import com.open592.appletviewer.viewer.event.ViewerEventBus
import javax.inject.Singleton
import javax.inject.Inject
import kotlin.system.exitProcess

@Singleton
public class Viewer @Inject constructor(
    private val eventBus: ViewerEventBus,
    private val debugConsole: DebugConsole,
    private val localization: Localization
) : ApplicationEventListener<ViewerEvent>(eventBus) {
    public fun initialize() {
        // Initialize the debug console in case we are in debug mode
        debugConsole.initialize()

        println(localization.getContent("err_missing_config"))

        eventBus.dispatchQuitEvent()
    }

    protected override fun processEvent(event: ViewerEvent) {
        when (event) {
            ViewerEvent.Quit -> handleQuitEvent()
        }
    }

    private fun handleQuitEvent() {
        exitProcess(0)
    }
}
