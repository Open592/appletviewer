package com.open592.appletviewer.assets

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SettingsStore
import okio.BufferedSource
import okio.sink
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

public class WindowsApplicationAssetResolver(
    private val config: ApplicationConfiguration,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
): ApplicationAssetResolver {
    // Updated list to remove some duplicates and keep consistent drive letter placement
    private val potentialParentDirectories = listOf(
        "C:/rscache/",
        "C:/windows/",
        "C:/winnt/",
        "C:/",
        getUserHomeDirectory(),
        "C:/tmp/",
        "" // Will result in the working directory being prepended
    )

    /**
     * To locate the eventual location where the file will be stored
     * we need to perform two pieces of logic which were present in the
     * original applet viewer.
     *
     * 1. First, we need to search for an existing file. We must exhaust
     * all possible file locations.
     *
     * 2. If no existing files are found, we then search for the first
     * possible folder where we can save the file.
     *
     * The cache file paths take into account the following:
     *
     * 1. Cache directory name
     *      - We have two options for top level cache
     *      directory names: `.jagex_cache_<MODEWHAT>` and
     *      `.file_store_<MODEWHAT>`
     *
     * 2. `modewhat`
     *      - This represents the environment which we are executing
     *      in. Examples include live, rc, wip - and are represents by a 0 indexed
     *      number appended to the cache directory name. This value is stored
     *      within the application configuration.
     *
     * 3. `cachesubdir`
     *      - This usually the game name, and if present, is a subdirectory
     *      of the cache directory. This value is also stored within the
     *      application configuration.
     *
     * @return `true` if we successfully saved the file, otherwise `false`
     */
    override fun saveCacheFile(name: String, file: BufferedSource): Boolean {
        val existingCachePath = findExistingCacheFile(name)

        if (existingCachePath != null && writeCacheFile(existingCachePath, file)) {
            return true
        }

        val cachePath = findWritableCachePath(name)

        return cachePath != null && writeCacheFile(cachePath, file)
    }

    /**
     * Iterate over all the potential cache paths looking for an existing file.
     *
     * @return The path to the existing file, otherwise `null`
     */
    private fun findExistingCacheFile(filename: String): Path? {
        return processAllPossibleCachePaths {
            val path = it.resolve(filename)

            if (path.exists()) {
                return@processAllPossibleCachePaths path
            } else {
                return@processAllPossibleCachePaths null
            }
        }
    }

    /**
     * Iterate over all the potential cache paths looking for a path which we can write
     * a new file to.
     *
     * @return The writable file path, otherwise `null`
     */
    private fun findWritableCachePath(name: String): Path? {
        return processAllPossibleCachePaths {
            try {
                Files.createDirectories(it)

                return@processAllPossibleCachePaths Files.createFile(it.resolve(name))
            } catch (_: Exception) {
                // Ignored
            }

            return@processAllPossibleCachePaths null
        }
    }

    /**
     * Iterate over all possible cache directories, performing an operation on each path.
     * The intention of the operation is to return the location where we can save the
     * new cache file.
     *
     * @return A valid `Path` returned from the operation if found, otherwise `null`
     */
    private fun processAllPossibleCachePaths(operation: (path: Path) -> Path?): Path? {
        // Represents the top level cache directory names to be used when searching
        // for or creating the cache file.
        val modewhat = getModewhat()
        val potentialCacheDirectoryNames = listOf(
            ".jagex_cache_$modewhat",
            ".file_store_$modewhat"
        )

        potentialParentDirectories.forEach { potentialParentDirectory ->
            potentialCacheDirectoryNames.forEach { potentialCacheDirectoryName ->
                val cacheDirectory = fileSystem.getPath(potentialParentDirectory, potentialCacheDirectoryName, getCacheSubDirectory())
                val cacheFilePath = operation(cacheDirectory)

                if (cacheFilePath != null) {
                    return cacheFilePath
                }
            }
        }

        return null
    }

    private fun getCacheSubDirectory(): String {
        return config.getConfig("cachesubdir")
    }

    private fun getModewhat(): Int {
        return config.getConfigAsInt("modewhat")?.plus(MODEWHAT_ADDEND) ?: MODEWHAT_ADDEND
    }

    private fun getUserHomeDirectory(): String {
        return settingsStore.getString("user.home").ifEmpty { "~" } + "/"
    }

    /**
     * Attempt to write the contents of `file` to the Path specified by `path`
     *
     * @return `true` if we successfully wrote the contents of the file - `false` otherwise.
     */
    private fun writeCacheFile(path: Path, file: BufferedSource): Boolean {
        return try {
            Files.newOutputStream(path).use { outputStream ->
                file.use {
                    it.readAll(outputStream.sink())
                }
            }

            true
        } catch (error: Exception) {
            false
        }
    }

    private companion object {
        // When added to the modewhat determines the value appended to the cache
        // directory name
        const val MODEWHAT_ADDEND = 32
    }
}
