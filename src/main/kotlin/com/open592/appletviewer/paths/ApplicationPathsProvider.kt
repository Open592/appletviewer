package com.open592.appletviewer.paths

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.settings.SettingsStore
import jakarta.inject.Inject
import jakarta.inject.Provider
import java.nio.file.FileSystem

public class ApplicationPathsProvider
@Inject
constructor(
    private val config: ApplicationConfiguration,
    private val environment: Environment,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore,
) : Provider<ApplicationPaths> {
    override fun get(): ApplicationPaths {
        return when (environment.getOperatingSystem()) {
            OperatingSystem.WINDOWS -> WindowsApplicationPaths(config, fileSystem, settingsStore)
            OperatingSystem.LINUX -> LinuxApplicationPaths(config, fileSystem, settingsStore)
        }
    }
}
