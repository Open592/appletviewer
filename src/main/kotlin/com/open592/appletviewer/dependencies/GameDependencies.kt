package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.progress.ProgressEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.source
import javax.inject.Inject

public class GameDependencies
@Inject
constructor(
    private val applicationModal: ApplicationModal,
    private val config: ApplicationConfiguration,
    private val environment: Environment,
    private val eventBus: GlobalEventBus,
    private val httpClient: OkHttpClient,
) {
    /**
     * Resolving the browsercontrol library is performed in 3 parts:
     *
     * 1. The zip archive is downloaded from the server and stored in memory.
     *      - The file is platform dependent and is specified within the jav_config.
     *      - The progress loaded is updated with the progress of the download.
     * 2. The zip archive is validated and the library is extracted from the zip
     * 3. The library file bytes are written to disk. The location is platform dependent.
     */
    public fun resolveBrowserControl(): ByteArray {
        val url = getBrowserControlUrl()
        val fileBytes = fetchRemoteFileBytes(url)

        if (fileBytes == null) {
            val filename = getBrowserControlFilename()
            val errorMessage = config.getContent("err_downloading")

            applicationModal.displayFatalErrorModal("$errorMessage: $filename")
        }

        return fileBytes
    }

    /**
     * Fetch the remote jar file from the server, resolving the filename from the
     * application config.
     */
    private fun fetchRemoteFileBytes(url: String): ByteArray? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                response.body.use { responseBody ->
                    if (responseBody == null) {
                        return null
                    }

                    responseBody.byteStream().source().use { source ->
                        val bufferSize = responseBody.contentLength().takeIf { it != -1L } ?: 300_000L
                        val buffer = Buffer()

                        while (source.read(buffer, bufferSize) != -1L) {
                            val progress = (100.0F * (buffer.size / 58988F)).toInt()

                            eventBus.dispatch(ProgressEvent.UpdateProgress(progress))
                        }

                        return buffer.readByteArray()
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Get the resulting filename for the browsercontrol library. This will be what
     * the file is saved as on the user's filesystem.
     */
    private fun getBrowserControlFilename(): String {
        // For 64-bit architectures we include it within the resulting filename
        val filename = when (environment.getArchitecture()) {
            Architecture.X86 -> "browsercontrol"
            Architecture.X86_64 -> "browsercontrol64"
        }

        return when (environment.getOperatingSystem()) {
            OperatingSystem.LINUX -> "$filename.so"
            OperatingSystem.WINDOWS -> "$filename.dll"
        }
    }

    /**
     * Returns the full URL to fetch this platform's browsercontrol library jar.
     *
     * The jar only contains a single library file, but we have to perform validations
     * on it before we execute `System.load`. The jar file allows us to ensure it's
     * properly signed.
     */
    private fun getBrowserControlUrl(): String {
        val operatingSystemKey = when (environment.getOperatingSystem()) {
            OperatingSystem.WINDOWS -> "win"
            OperatingSystem.LINUX -> "linux"
        }
        val architectureKey = when (environment.getArchitecture()) {
            Architecture.X86 -> "x86"
            Architecture.X86_64 -> "amd64"
        }
        val configKey = "browsercontrol_${operatingSystemKey}_${architectureKey}_jar"
        val filename = config.getConfig(configKey)
        val codebaseUrl = getCodebaseUrl()

        return "$codebaseUrl$filename"
    }

    /**
     * Returns the root URL for assets we will be fetching.
     */
    private fun getCodebaseUrl(): String {
        return config.getConfig("codebase")
    }
}
