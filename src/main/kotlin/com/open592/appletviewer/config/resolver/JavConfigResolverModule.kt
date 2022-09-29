package com.open592.appletviewer.config.resolver

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.language.SupportedLanguageModule
import com.open592.appletviewer.fetch.AssetFetchModule

public object JavConfigResolverModule : AbstractModule() {
    public override fun configure() {
        install(AssetFetchModule)
        install(SupportedLanguageModule)
    }
}
