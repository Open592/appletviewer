package com.open592.appletviewer.preferences

import com.google.inject.AbstractModule
import com.google.inject.Scopes

public object AppletViewerPreferencesModule : AbstractModule() {
    override fun configure() {
        bind(AppletViewerPreferences::class.java)
            .toProvider(AppletViewerPreferencesProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
