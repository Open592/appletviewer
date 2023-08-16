package com.open592.appletviewer.frame

import com.google.inject.AbstractModule
import com.open592.appletviewer.paths.ApplicationPathsModule

public object RootFrameModule : AbstractModule() {
    public override fun configure() {
        install(ApplicationPathsModule)
    }
}
