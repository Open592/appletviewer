package com.open592.appletviewer.jar

import com.open592.appletviewer.settings.SettingsStore
import jakarta.inject.Inject
import jakarta.inject.Provider

public class CertificateValidatorProvider @Inject constructor(
    private val settingsStore: SettingsStore,
) : Provider<CertificateValidator> {
    public override fun get(): CertificateValidator {
        val fakeThawtePublicKey = settingsStore.getString("com.open592.fakeThawtePublicKey").ifEmpty { null }
        val fakeJagexPublicKey = settingsStore.getString("com.open592.fakeJagexPublicKey").ifEmpty { null }
        val disableJarValidation = settingsStore.getBoolean("com.open592.disableJarValidation")

        return CertificateValidator(fakeThawtePublicKey, fakeJagexPublicKey, disableJarValidation)
    }
}
