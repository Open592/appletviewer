package com.open592.appletviewer.environment

import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject

public class Environment @Inject constructor(settingsStore: SettingsStore) {
    private val operatingSystem: OperatingSystem
    private val architecture: Architecture

    init {
        operatingSystem = OperatingSystem.detect(settingsStore.getString("os.name"))
        architecture = operatingSystem.detectArchitecture(settingsStore.getString("os.arch"))
    }

    public fun getOperatingSystem(): OperatingSystem {
        return operatingSystem
    }

    public fun getArchitecture(): Architecture {
        return architecture
    }
}
