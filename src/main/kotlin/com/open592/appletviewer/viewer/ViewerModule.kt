package com.open592.appletviewer.viewer

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.ApplicationConfigurationModule
import com.open592.appletviewer.config.resolver.JavConfigResolverModule
import com.open592.appletviewer.debug.DebugConsoleModule
import com.open592.appletviewer.dependencies.ViewerDependenciesModule
import com.open592.appletviewer.frame.RootFrameModule
import com.open592.appletviewer.modal.ApplicationModalModule
import com.open592.appletviewer.progress.ProgressIndicatorModule
import com.open592.appletviewer.settings.SettingStoreModule

public object ViewerModule : AbstractModule() {
    public override fun configure() {
        install(RootFrameModule)
        install(SettingStoreModule)
        /**
         * At the time of loading Viewer the application configuration
         * will not have a backing JavConfig - this is lazy loaded by
         * the resolver during application start-up.
         */
        install(ApplicationConfigurationModule)
        install(ApplicationModalModule)
        install(JavConfigResolverModule)
        install(DebugConsoleModule)
        // Bound as an eager singleton which initializes event listener.
        install(ProgressIndicatorModule)
        install(ViewerDependenciesModule)
    }
}
