package com.open592.appletviewer.viewer

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.ApplicationConfigurationModule
import com.open592.appletviewer.debug.DebugConsoleModule
import com.open592.appletviewer.progress.ProgressIndicatorModule
import com.open592.appletviewer.root.RootFrameModule

public object ViewerModule : AbstractModule() {
    public override fun configure() {
        install(RootFrameModule)
        install(ApplicationConfigurationModule)
        install(DebugConsoleModule)
        install(ProgressIndicatorModule)
    }
}
