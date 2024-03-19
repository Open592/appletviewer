package com.open592.appletviewer.jar

import com.google.inject.AbstractModule
import com.open592.appletviewer.settings.SettingStoreModule

public object CertificateValidatorModule : AbstractModule() {
    public override fun configure() {
        install(SettingStoreModule)

        bind(CertificateValidator::class.java).toProvider(CertificateValidatorProvider::class.java)
    }
}
