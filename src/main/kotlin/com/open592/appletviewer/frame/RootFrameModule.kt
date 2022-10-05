package com.open592.appletviewer.frame

import com.google.inject.AbstractModule
import com.open592.appletviewer.fetch.AssetFetchModule

public object RootFrameModule : AbstractModule() {
    public override fun configure() {
        install(AssetFetchModule)
    }
}
