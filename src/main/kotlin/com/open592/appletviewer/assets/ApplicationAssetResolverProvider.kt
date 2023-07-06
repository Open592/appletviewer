package com.open592.appletviewer.assets

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import java.nio.file.FileSystems
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

public class ApplicationAssetResolverProvider @Inject constructor(
    private val config: ApplicationConfiguration,
    private val environment: Environment,
    private val settingsStore: SettingsStore
) : Provider<ApplicationAssetResolver> {
    override fun get(): ApplicationAssetResolver {
        when(environment.os) {
            OperatingSystem.WINDOWS -> {
                return WindowsApplicationAssetResolver(config, FileSystems.getDefault(), settingsStore)
            }
            else -> {
                error("Unsupported operating system: ${environment.os}")
            }
        }
    }
}
