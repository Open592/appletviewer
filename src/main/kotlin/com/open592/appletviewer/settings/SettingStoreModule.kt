package com.open592.appletviewer.settings

import com.google.inject.AbstractModule

public object SettingStoreModule : AbstractModule() {
    override fun configure() {
        bind(SettingsStore::class.java).to(SystemPropertiesSettingsStore::class.java)
    }
}
