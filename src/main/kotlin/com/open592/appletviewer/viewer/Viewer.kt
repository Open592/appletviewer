package com.open592.appletviewer.viewer

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.config.resolver.JavConfigResolveException
import com.open592.appletviewer.config.resolver.JavConfigResolver
import com.open592.appletviewer.debug.DebugConsole
import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.modal.ApplicationModalType
import com.open592.appletviewer.progress.ProgressIndicator
import com.open592.appletviewer.settings.SettingsStore
import com.open592.appletviewer.viewer.event.ViewerEvent
import com.open592.appletviewer.viewer.event.ViewerEventBus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

@Singleton
public class Viewer @Inject constructor(
    eventBus: ViewerEventBus,
    private val applicationModal: ApplicationModal,
    private val config: ApplicationConfiguration,
    private val debugConsole: DebugConsole,
    private val progressIndicator: ProgressIndicator,
    private val settingsStore: SettingsStore,
    private val javConfigResolver: JavConfigResolver
) : ApplicationEventListener<ViewerEvent>(eventBus) {
    public fun initialize() {
        // Initialize the debug console in case we are in debug mode
        debugConsole.initialize()

        printDebugInfo()

        progressIndicator.eventBus.dispatchChangeVisibilityEvent(visible = true)

        initializeConfiguration()
    }

    protected override fun processEvent(event: ViewerEvent) {
        when (event) {
            is ViewerEvent.Quit -> handleQuitEvent()
        }
    }

    private fun handleQuitEvent() {
        exitProcess(0)
    }

    private fun initializeConfiguration() {
        try {
            val javConfig = javConfigResolver.resolve()

            config.initialize(javConfig)
        } catch (e: JavConfigResolveException) {
            applicationModal.eventBus.dispatchDisplayEvent(
                ApplicationModalType.FATAL_ERROR,
                config.getContent(e.contentKey)
            )

            return
        }
    }

    private fun printDebugInfo() {
        if (settingsStore.getBoolean("com.jagex.debug")) {
            println("release #7")
            println("java.version = ${System.getProperty("java.version")}")
            println("os.name = ${System.getProperty("os.name")}")
            println("os.arch = ${System.getProperty("os.arch")}")
        }
    }
}
