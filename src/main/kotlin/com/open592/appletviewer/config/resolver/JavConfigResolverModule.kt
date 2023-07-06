package com.open592.appletviewer.config.resolver

import com.google.inject.AbstractModule
import com.open592.appletviewer.assets.AssetResolverModule
import com.open592.appletviewer.config.language.SupportedLanguageModule

public object JavConfigResolverModule : AbstractModule() {
    public override fun configure() {
        install(AssetResolverModule)
        install(SupportedLanguageModule)
    }
}
