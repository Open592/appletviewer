package com.open592.appletviewer.paths

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import java.nio.file.FileSystem
import java.nio.file.Path

public abstract class ApplicationPaths(
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
) {
    /**
     * Each platform must provide their own implementation for cache file path resolution.
     */
    public abstract fun resolveCacheFilePath(filename: String): Path

    /**
     * It is expected that the appletviewer is invoked by the launcher which exists within
     * a sibling directory to the "game directory" which includes a number of assets and
     * configuration files.
     *
     * NOTE: We differ from the original implementation by allowing for the overriding of the
     * root launcher directory.
     *
     * > jagexlauncher :: Root launcher directory where all files required by the launcher are stored.
     * ----> bin (`user.dir`) :: Location where the launcher will be invoked and where the jvm will be initialized.
     * ----> lib :: Software libraries and properties/configuration files.
     * ----> runescape :: This is the game directory and should include the file returned from this function.
     *
     * @param filename It is expected that this file exists within the game directory.
     */
    public fun resolveGameFileDirectoryPath(filename: String): Path? {
        val overridePath = settingsStore.getString(LAUNCHER_DIRECTORY_OVERRIDE_PROPERTY_NAME)
        val launcherDirectory = if (overridePath.isNotEmpty()) fileSystem.getPath(overridePath) else fileSystem.getPath(
            settingsStore.getString("user.dir")
        )

        return launcherDirectory.parent?.resolve(Constants.GAME_NAME)?.resolve(filename)
    }

    protected fun handleCacheDirectoryResolutionFailure(filename: String): Nothing {
        // Retain same behavior as original applet viewer
        if (settingsStore.getBoolean(SettingsStore.IS_DEBUG_KEY)) {
            throw RuntimeException("Fatal - could not find ANY location for file: $filename")
        } else {
            throw RuntimeException()
        }
    }

    private companion object {
        private const val LAUNCHER_DIRECTORY_OVERRIDE_PROPERTY_NAME = "com.open592.launcherDirectoryOverride"
    }
}
