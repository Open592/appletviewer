package com.open592.appletviewer.paths

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.inject.Inject

public class ApplicationPaths @Inject constructor(
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
) {
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

    private companion object {
        private const val LAUNCHER_DIRECTORY_OVERRIDE_PROPERTY_NAME = "com.open592.launcherDirectoryOverride"
    }
}
