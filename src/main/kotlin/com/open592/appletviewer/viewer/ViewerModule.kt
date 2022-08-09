package com.open592.appletviewer.viewer

import com.google.inject.AbstractModule
import com.open592.appletviewer.debug.DebugConsoleModule
import com.open592.appletviewer.root.RootFrameModule
import com.open592.appletviewer.localization.LocalizationModule
import com.open592.appletviewer.progress.ProgressIndicatorModule

public object ViewerModule : AbstractModule() {
    public override fun configure() {
        install(RootFrameModule)
        install(DebugConsoleModule)
        install(LocalizationModule)
        install(ProgressIndicatorModule)
    }
}
