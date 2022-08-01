package com.open592.appletviewer.localization

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.open592.appletviewer.preferences.AppletViewerPreferencesModule

public object LocalizationModule : AbstractModule() {
    override fun configure() {
        install(AppletViewerPreferencesModule)

        bind(Localization::class.java)
            .toProvider(LocalizationProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
