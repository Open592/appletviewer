package com.open592.appletviewer.environment

import com.open592.appletviewer.settings.SettingsStore

/**
 * A helper class for providing information about the environment.
 */
public data class Environment(
    public val os: OperatingSystem,
    public val arch: Architecture,
){
    public companion object {
        public fun detect(settingsStore: SettingsStore): Environment {
            val os: OperatingSystem = OperatingSystem.detect(settingsStore.getString("os.name"))
            val arch: Architecture = os.detectArchitecture(settingsStore.getString("os.arch"))

            return Environment(os, arch)
        }
    }
}
