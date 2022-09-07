package com.open592.appletviewer.config

import com.google.inject.AbstractModule
import com.open592.appletviewer.assets.AssetManagerModule
import com.open592.appletviewer.localization.LocalizationModule
import com.open592.appletviewer.modal.ApplicationModalModule

public object ApplicationConfigurationModule : AbstractModule() {
    public override fun configure() {
        install(ApplicationModalModule)
        install(LocalizationModule)
        install(AssetManagerModule)
    }
}
