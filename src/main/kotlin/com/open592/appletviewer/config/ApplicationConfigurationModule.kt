package com.open592.appletviewer.config

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.language.SupportedLanguageModule
import com.open592.appletviewer.modal.ApplicationModalModule

public object ApplicationConfigurationModule : AbstractModule() {
    public override fun configure() {
        install(ApplicationModalModule)
        install(SupportedLanguageModule)
    }
}
