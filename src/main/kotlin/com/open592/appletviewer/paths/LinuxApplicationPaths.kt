package com.open592.appletviewer.paths

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SettingsStore
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

public class LinuxApplicationPaths
    @Inject
    constructor(
        config: ApplicationConfiguration,
        private val fileSystem: FileSystem,
        settingsStore: SettingsStore,
    ) : ApplicationPaths(config, fileSystem, settingsStore) {
        /**
         * Resolves the cache directory path for the provided filename.
         *
         * For Linux this is very simple as we only have a single potential
         * location where the cache files can exist. On other platforms,
         * especially Linux, this logic is much more complex.
         *
         * We do, need to account for a couple dynamic pieces of data:
         *
         * - `modewhat`: This value specifies the environment we are
         *    executing in (live, rc, wip) and is a 0 indexed integer
         *    that we append to the top level cache directory name.
         *
         * - `cachesubdir`: If this value is populated within the
         *    application configuration, then we need to append it
         *    to the cache directory hierarchy. Usually it corresponds
         *    to the game name (`runescape` for example).
         *
         * Examples:
         * - `/home/user/.cache/.jagex_cache_32/runescape/`
         * - `/home/user/.cache/.jagex_cache_33/runescape/`
         * - `/home/user/.cache/.jagex_cache_33/`
         *
         * @param filename On Linux this parameter is ignored.
         *
         * @return The resolved cache file path.
         *
         * @throws RuntimeException If we are unable to resolve the cache file path.
         */
        override fun resolveCacheDirectoryPath(filename: String): Path {
            val systemCacheDirectory = getSystemCacheDirectory()
            val modewhat = getModewhat()
            val parentDirectoryName = ".jagex_cache_$modewhat"

            return Files.createDirectories(
                systemCacheDirectory.resolve(parentDirectoryName).resolve(getCacheSubDirectory()),
            )
        }

        /**
         * Returns the path to the system's cache directory, creating
         * it if needed.
         *
         * Example: `/home/user/.cache`
         */
        private fun getSystemCacheDirectory(): Path {
            val homeDirectoryPath = getUserHomeDirectory()
            val systemCacheDirectoryPath = fileSystem.getPath(homeDirectoryPath, ".cache")

            return Files.createDirectories(systemCacheDirectoryPath)
        }
    }
