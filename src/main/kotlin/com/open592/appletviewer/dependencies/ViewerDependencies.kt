package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.dependencies.resolver.DependencyResolver
import com.open592.appletviewer.dependencies.resolver.DependencyResolverException
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.progress.ProgressEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
public class ViewerDependencies
@Inject
constructor(
    private val applicationModal: ApplicationModal,
    private val config: ApplicationConfiguration,
    private val environment: Environment,
    private val resolver: DependencyResolver,
    private val eventBus: GlobalEventBus,
) {
    public fun resolve() {
        // Signify that we are about to start downloading the browsercontrol library.
        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_app_resources")))

        try {
            resolver.resolveBrowserControl()
        } catch (e: DependencyResolverException.FetchDependencyException) {
            val errorMessage = config.getContent("err_downloading")

            applicationModal.displayFatalErrorModal("$errorMessage: ${e.filename}")
        } catch (e: DependencyResolverException.VerifyDependencyException) {
            applicationModal.displayFatalErrorModal(getBrowserControlValidationErrorKey())
        }

        // Signify that we are about to start downloading the loader jar.
        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_app")))
    }

    /**
     * The content key for the error message returned when an issue occurs during
     * validation of the browsercontrol jar includes the bit size of the architecture,
     * either 64, or 32.
     *
     * Example: bc64
     */
    private fun getBrowserControlValidationErrorKey(): String {
        return when (environment.getArchitecture()) {
            Architecture.X86 -> "err_verify_bc"
            Architecture.X86_64 -> "err_verify_bc64"
        }
    }
}
