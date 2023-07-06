package com.open592.appletviewer.environment

import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Provider

public class EnvironmentProvider @Inject constructor(
    private val settingsStore: SettingsStore
) : Provider<Environment> {
    override fun get(): Environment {
        return Environment.detect(settingsStore)
    }
}
