package com.open592.appletviewer.fetch

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SettingsStore
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.FileSystem
import java.nio.file.Path
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.notExists

@Singleton
public class AssetFetch @Inject constructor(
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val settingsStore: SettingsStore,
){
    public fun fetchLocaleFile(filename: String): ExternalAsset? {
        val path = getGameFileDirectory(filename)

        if (path.notExists()) {
            return null
        }

        return ExternalAsset.fromPath(path)
    }

    public fun fetchRemoteFile(url: String): ExternalAsset? {
        try {
            val request = HttpRequest.newBuilder(URI(url))
                .timeout(Duration.ofSeconds(30L))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

            if (response.statusCode() != EXPECTED_STATUS_CODE) {
                return null
            }

            return ExternalAsset(response.body())
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
