package com.open592.appletviewer.config.language

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.open592.appletviewer.preferences.AppletViewerPreferencesModule

public object SupportedLanguageModule : AbstractModule() {
    public override fun configure() {
        install(AppletViewerPreferencesModule)

        bind(SupportedLanguage::class.java)
            .toProvider(SupportedLanguageProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
