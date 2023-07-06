package com.open592.appletviewer.assets.resolver

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.buffer
import okio.source
import java.awt.Image
import java.awt.Toolkit
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.notExists

public class AssetResolver(
    private val fileSystem: FileSystem,
    private val httpClient: OkHttpClient,
    private val settingsStore: SettingsStore
){
    public fun fetchLocaleFile(filename: String): BufferedSource? {
        val path = getGameFileDirectory(filename)

        if (path.notExists()) {
            return null
        }

        return path.source().buffer()
    }

    public fun fetchLocaleImage(filename: String): Image? {
        val path = getGameFileDirectory(filename)

        if (path.notExists()) {
            return null
        }

        return Toolkit.getDefaultToolkit().getImage(path.toUri().toURL())
    }

    public fun fetchRemoteFile(url: String): BufferedSource? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                val result = response.peekBody(Long.MAX_VALUE)

                return result.source()
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
