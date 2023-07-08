package com.open592.appletviewer.fetch

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.source
import java.awt.Image
import java.awt.Toolkit
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

@Singleton
public class AssetFetch @Inject constructor(
    private val httpClient: OkHttpClient,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
) {
    /**
     * Read a locale file from the game file directory to a string.
     */
    public fun readLocaleGameFile(filename: String): String? {
        val path = getGameFileDirectory(filename)

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
     * Resolve a locale image file from the game file directory.
     */
    public fun fetchLocaleImage(filename: String): Image? {
        val path = getGameFileDirectory(filename)

        if (path.notExists()) {
            return null
        }

        return Toolkit.getDefaultToolkit().getImage(path.toUri().toURL())
    }

    /**
     * Read a remote file to a string
     */
    public fun readRemoteFile(url: String): String? {
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
     * It is assumed that the appletviewer will be invoked by the launcher which will be placed
     * in a directory a level above the "game directory" which includes a number of assets and
     * configuration files.
     *
     * NOTE: We differ from the original implementation by allowing for the overriding of the
     * root launcher directory.
     *
     * > "jagexlauncher" (* Root launcher directory where all files required by the launcher are stored *)
     * ----> "bin" > `user.dir` (* Location where the launcher will be invoked and where the jvm will be initialized *)
     * ----> "lib" > (* Software libraries and properties/configuration files *)
     * ----> "runescape" > (* Directory where we look for "com.jagex.configfile" *)
     */
    private fun getGameFileDirectory(filename: String): Path {
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
