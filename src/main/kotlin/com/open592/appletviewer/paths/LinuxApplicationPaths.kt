package com.open592.appletviewer.paths

import com.open592.appletviewer.settings.SettingsStore
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.inject.Inject

public class LinuxApplicationPaths @Inject constructor(
    fileSystem: FileSystem,
    settingsStore: SettingsStore
) : ApplicationPaths(fileSystem, settingsStore) {
    override fun resolveCacheFilePath(filename: String): Path {
        handleCacheDirectoryResolutionFailure(filename)
    }
}
