package com.open592.appletviewer.config.resolver

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.language.SupportedLanguageModule
import com.open592.appletviewer.http.OkHttpClientModule
import com.open592.appletviewer.paths.ApplicationPathsModule

public object JavConfigResolverModule : AbstractModule() {
    public override fun configure() {
        install(ApplicationPathsModule)
        install(OkHttpClientModule)
        install(SupportedLanguageModule)
    }
}
