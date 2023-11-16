package com.open592.appletviewer.viewer

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.config.resolver.JavConfigResolveException
import com.open592.appletviewer.config.resolver.JavConfigResolver
import com.open592.appletviewer.debug.DebugConsole
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.progress.ProgressEvent
import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

@Singleton
public class Viewer
@Inject
constructor(
    private val eventBus: GlobalEventBus,
    private val applicationModal: ApplicationModal,
    private val config: ApplicationConfiguration,
    private val debugConsole: DebugConsole,
    private val settingsStore: SettingsStore,
    private val javConfigResolver: JavConfigResolver,
) {
    init {
        eventBus.listen<ViewerEvent> {
            when (it) {
                is ViewerEvent.Quit -> handleQuitEvent()
            }
        }
    }

    public fun initialize() {
        // Initialize the debug console in case we are in debug mode
        debugConsole.initialize()

        printDebugInfo()

        // Inform the user that we are loading the configuration
        eventBus.dispatch(ProgressEvent.ChangeVisibility(visible = true))
        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_config")))

        initializeConfiguration()

        checkForNewViewerVersion()

        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_app_resources")))
    }

    private fun checkForNewViewerVersion() {
        val requiredVersion = config.getConfigAsInt("viewerversion") ?: Int.MAX_VALUE

        if (requiredVersion > VIEWER_VERSION) {
            applicationModal.displayMessageModal(config.getContent("new_version"))
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
            applicationModal.displayFatalErrorModal(config.getContent(e.contentKey))
        }
    }

    private fun printDebugInfo() {
        if (settingsStore.getBoolean(SettingsStore.IS_DEBUG_KEY)) {
            println("release #7")
            println("java.version = ${System.getProperty("java.version")}")
            println("os.name = ${System.getProperty("os.name")}")
            println("os.arch = ${System.getProperty("os.arch")}")
        }
    }

    private companion object {
        private const val VIEWER_VERSION = 100
    }
}
