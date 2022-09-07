package com.open592.appletviewer.assets

import com.google.inject.AbstractModule
import com.open592.appletviewer.assets.http.HttpClientModule

public object AssetManagerModule : AbstractModule() {
    public override fun configure() {
        install(HttpClientModule)
    }
}
