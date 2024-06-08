package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.progress.ProgressEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
public class ViewerDependencies @Inject constructor(
    private val applicationModal: ApplicationModal,
    private val configuration: ApplicationConfiguration,
    private val browserControlResolver: BrowserControlResolver,
    private val eventBus: GlobalEventBus,
    private val loaderResolver: LoaderResolver,
) {
    public fun resolve() {
        // Signify that we are about to start downloading the browsercontrol library.
        eventBus.dispatch(ProgressEvent.ChangeText(configuration.getContent("loading_app_resources")))

        try {
            browserControlResolver.resolve()
        } catch (e: RemoteDependencyResolver.ResolveException) {
            applicationModal.displayFatalErrorModal(e.message)
        }

        // Signify that we are about to start downloading the loader jar.
        eventBus.dispatch(ProgressEvent.ChangeText(configuration.getContent("loading_app")))

        try {
            loaderResolver.resolve()
        } catch (e: RemoteDependencyResolver.ResolveException) {
            applicationModal.displayFatalErrorModal(e.message)
        }
    }
}
