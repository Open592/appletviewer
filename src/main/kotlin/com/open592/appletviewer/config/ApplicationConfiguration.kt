package com.open592.appletviewer.config

import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.modal.ApplicationModalType
import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationConfiguration @Inject constructor(
    private val applicationModal: ApplicationModal,
    private val localization: Localization,
    private val settingsStore: SettingsStore
) {
    public fun initialize() {
        val configURL = settingsStore.getString(CONFIG_URL_PROPERTY_NAME)
        val configFilePath = settingsStore.getString(CONFIG_FILE_PROPERTY_NAME)

        if (configURL.isEmpty() && configFilePath.isEmpty()) {
            return applicationModal.eventBus.dispatchDisplayEvent(
                ApplicationModalType.FATAL_ERROR,
                localization.getContent("err_missing_config")
            )
        }
    }

    private companion object {
        const val CONFIG_URL_PROPERTY_NAME = "com.jagex.config"
        const val CONFIG_FILE_PROPERTY_NAME = "com.jagex.configfile"
    }
}
