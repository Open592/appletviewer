package com.open592.appletviewer.config

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.open592.appletviewer.config.language.SupportedLanguageModule
import com.open592.appletviewer.http.HttpFetchModule

public object ApplicationConfigurationModule : AbstractModule() {
    public override fun configure() {
        install(SupportedLanguageModule)
        install(HttpFetchModule)

        bind(ApplicationConfiguration::class.java)
            .toProvider(ApplicationConfigurationProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
