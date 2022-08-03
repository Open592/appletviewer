package com.open592.appletviewer.viewer

import com.google.inject.AbstractModule
import com.open592.appletviewer.debug.DebugConsoleModule
import com.open592.appletviewer.localization.LocalizationModule

public object ViewerModule : AbstractModule() {
    public override fun configure() {
        install(DebugConsoleModule)
        install(LocalizationModule)
    }
}
