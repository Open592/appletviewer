package com.open592.appletviewer.fetch

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.buffer
import okio.source
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.notExists

@Singleton
public class AssetFetch @Inject constructor(
    private val httpClient: OkHttpClient,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore
) {
    public fun fetchLocaleFile(filename: String): BufferedSource? {
        val path = getGameFileDirectory(filename)

        if (path.notExists()) {
            return null
        }

        return path.source().buffer()
    }

    public fun fetchRemoteFile(url: String): BufferedSource? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                // Read response
                val body = response.body!!.source()

                body.request(Long.MAX_VALUE)

                val ret = body.buffer.clone()

                body.close()

                return ret
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
     * > "jagexlauncher" (* Root directory for the installer *)
     * ----> "bin" > `user.dir` (* Location where the launcher will be invoked and where the jvm will be initialized *)
     * ----> "runescape" > (* Directory where we look for "com.jagex.configfile" *)
     */
    private fun getGameFileDirectory(filename: String): Path {
        return fileSystem.getPath(settingsStore.getString("user.dir"), Constants.GAME_NAME, filename)
    }

    private companion object {
        private const val EXPECTED_STATUS_CODE = 200
    }
}