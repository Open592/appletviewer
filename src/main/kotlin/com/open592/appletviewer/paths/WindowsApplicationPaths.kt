package com.open592.appletviewer.paths

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SettingsStore
import jakarta.inject.Inject
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.isWritable

public class WindowsApplicationPaths
@Inject
constructor(
    config: ApplicationConfiguration,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore,
) : ApplicationPaths(config, fileSystem, settingsStore) {
    /**
     * To locate the eventual location where the file will be stored
     * we need to perform two pieces of logic which were present in the
     * original applet viewer.
     *
     * 1. First, we need to search for an existing file. We must exhaust
     * all possible file locations.
     *
     * 2. If no existing files are found, we then search for the first
     * possible potential cache directory where we can save the file.
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
     *      in. Examples include live, rc, wip - and are represents by a zero
     *      indexed number appended to the cache directory name. This value
     *      is stored within the application's configuration.
     *
     * 3. `cachesubdir`
     *      - This usually the game name, and if present, is a subdirectory
     *      of the cache directory. This value is also stored within the
     *      application configuration.
     *
     * @param filename The filename must be passed to allow us to check for
     * existing files within the potential cache directories.
     *
     * @return The resolved cache file path.
     *
     * @throws RuntimeException If we are unable to resolve the cache file path.
     */
    public override fun resolveCacheDirectoryPath(filename: String): Path {
        val existingCachePath = findExistingCacheFile(filename)

        if (existingCachePath?.isWritable() == true) {
            return existingCachePath
        }

        return findWritableCachePath(filename) ?: handleCacheDirectoryResolutionFailure(filename)
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
    private fun findWritableCachePath(filename: String): Path? {
        return processAllPossibleCachePaths {
            try {
                Files.createDirectories(it)

                return@processAllPossibleCachePaths it.resolve(filename).createFile().toAbsolutePath()
            } catch (_: Exception) {
                // Ignored
            }

            return@processAllPossibleCachePaths null
        }
    }

    /**
     * Iterate over all possible cache directories, performing an operation on each path.
     * The result of the operation is the full path to the file, or `null` if the path
     * wasn't valid.
     *
     * @return A valid `Path` returned from the operation if found, otherwise `null`
     */
    private fun processAllPossibleCachePaths(operation: (path: Path) -> Path?): Path? {
        // Updated list to remove some duplicates and keep consistent drive letter placement
        val potentialParentDirectories =
            listOf(
                "C:/rscache/",
                "C:/windows/",
                "C:/winnt/",
                "C:/",
                getUserHomeDirectory(),
                "C:/tmp/",
                "", // Will result in the working directory being prepended
            )
        // Represents the top level cache directory names to be used when searching
        // for or creating the cache file.
        val modewhat = getModewhat()
        val potentialCacheDirectoryNames =
            listOf(
                ".jagex_cache_$modewhat",
                ".file_store_$modewhat",
            )

        potentialParentDirectories.forEach { potentialParentDirectory ->
            potentialCacheDirectoryNames.forEach { potentialCacheDirectoryName ->
                val cacheDirectory =
                    fileSystem.getPath(
                        potentialParentDirectory,
                        potentialCacheDirectoryName,
                        getCacheSubDirectory(),
                    ).toAbsolutePath()
                val cacheFilePath = operation(cacheDirectory)

                if (cacheFilePath != null) {
                    return cacheFilePath
                }
            }
        }

        return null
    }
}
