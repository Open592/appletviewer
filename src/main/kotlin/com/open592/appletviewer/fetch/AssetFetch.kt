package com.open592.appletviewer.fetch

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.source
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

/**
 * Provides a simple wrapper around `OkHttpClient` and `FileSystem` to support
 * fetching external assets.
 *
 * We implement a couple methods for some of the most simple fetch actions. For more complex actions we could expose
 * the underlying `OkHttpClient` and `FileSystem`.
 */
@Singleton
public class AssetFetch @Inject constructor(
    private val httpClient: OkHttpClient,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
) {
    /**
     * Fetch a local file from the game file directory.
     *
     * We return the full contents of the file as a string, and as such this should only be used for small files.
     *
     * If we are unable to open the file for reading, we return `null` back to the caller.
     *
     * @param filename It is expected that this file exist within the game directory.
     */
    public fun fetchLocalGameFile(filename: String): String? {
        val path = resolveGameFileDirectoryPath(filename)

        if (path.notExists() || !path.isRegularFile()) {
            return null
        }

        try {
            path.source().buffer().use {
                return it.readUtf8()
            }
        } catch (_: IOException) {
            return null
        }
    }

    /**
     * Fetch a remote file from the game file directory.
     *
     * We return the full contents of the file as a string, and as such this should only be used for small files.
     *
     * If we are unable to fetch the file for any reason we return `null` back to the caller.
     *
     * @param url The full URL to the remote file we want to fetch.
     */
    public fun fetchRemoteFile(url: String): String? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) {
                    return null
                }

                return response.body!!.string()
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * It is assumed that the appletviewer will be invoked by the launcher which exists
     * within a sibling directory to the "game directory" which includes a number of
     * assets and configuration files.
     *
     * NOTE: We differ from the original implementation by allowing for the overriding of the
     * root launcher directory.
     *
     * > "jagexlauncher" (* Root launcher directory where all files required by the launcher are stored *)
     * ----> "bin" > `user.dir` (* Location where the launcher will be invoked and where the jvm will be initialized *)
     * ----> "lib" > (* Software libraries and properties/configuration files *)
     * ----> "runescape" > (* Directory where we look for "com.jagex.configfile" *)
     *
     * @param filename It is assumed that this file exists within the game directory.
     */
    public fun resolveGameFileDirectoryPath(filename: String): Path {
        val overridePath = settingsStore.getString(LAUNCHER_DIRECTORY_OVERRIDE_PROPERTY_NAME)

        if (overridePath.isNotEmpty()) {
            return fileSystem.getPath(overridePath, Constants.GAME_NAME, filename)
        }

        return fileSystem.getPath(settingsStore.getString("user.dir"), Constants.GAME_NAME, filename)
    }

    private companion object {
        private const val LAUNCHER_DIRECTORY_OVERRIDE_PROPERTY_NAME = "com.open592.launcherDirectoryOverride"
    }
}
