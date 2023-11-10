package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.dependencies.resolver.DependencyResolver
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.progress.ProgressEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ViewerDependencies
@Inject
constructor(
    private val config: ApplicationConfiguration,
    private val resolver: DependencyResolver,
    private val eventBus: GlobalEventBus,
) {
    public fun resolve() {
        // Signify that we are about to start downloading the browsercontrol library.
        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_app_resources")))

        resolver.resolveBrowserControl()

        // Signify that we are about to start downloading the loader jar.
        eventBus.dispatch(ProgressEvent.ChangeText(config.getContent("loading_app")))
    }
}
