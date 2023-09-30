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
import java.util.jar.JarInputStream
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
    public fun resolveBrowserControl(): Buffer {
        val url = getBrowserControlUrl()
        val fileBytes = fetchRemoteFileBytes(url)

        if (fileBytes == null) {
            val filename = getBrowserControlFilename()
            val errorMessage = config.getContent("err_downloading")

            applicationModal.displayFatalErrorModal("$errorMessage: $filename")
        }

        val jar = resolveRemoteJar(fileBytes)
            ?: applicationModal.displayFatalErrorModal(getBrowserControlValidationErrorKey())

        return SignedJarFileEntries.loadAndValidate(jar)?.getEntry(getBrowserControlFilename())
            ?: applicationModal.displayFatalErrorModal(getBrowserControlValidationErrorKey())
    }

    /**
     * Fetch the remote jar file from the server, resolving the filename from the
     * application config.
     */
    private fun fetchRemoteFileBytes(url: String): Buffer? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                response.body.use { responseBody ->
                    val responseSource = responseBody?.byteStream()?.source() ?: return null

                    responseSource.use { source ->
                        val bufferSize = responseBody.contentLength().takeIf { it != -1L } ?: 300_000L
                        val buffer = Buffer()

                        while (source.read(buffer, bufferSize) != -1L) {
                            val progress = (100.0F * (buffer.size / 58988F)).toInt()

                            eventBus.dispatch(ProgressEvent.UpdateProgress(progress))
                        }

                        return buffer
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
     * The content key for the error message returned when an issue occurs during
     * validation of the browsercontrol jar includes the bit size of the architecture,
     * either 64, or 32.
     *
     * Example: bc64
     */
    private fun getBrowserControlValidationErrorKey(): String {
        return when (environment.getArchitecture()) {
            Architecture.X86 -> "err_verify_bc"
            Architecture.X86_64 -> "err_verify_bc64"
        }
    }

    /**
     * Returns the root URL for assets we will be fetching.
     */
    private fun getCodebaseUrl(): String {
        return config.getConfig("codebase")
    }

    /**
     * When dealing with remote jar files we want to offload some validation
     * work to standard library classes like `JarInputStream`. Unfortunately
     * it makes a lot of assumptions about the structure of the jar file, and
     * the naming of its entries. Because of this we are unable to validate
     * the original Jagex jar files as-is since they use a non-standard entry
     * order.
     *
     * To mitigate the above we check if `JarInputStream` was unable to load the
     * jar manifest, and if so, we attempt to recreate the jar using the standard
     * naming and structure.
     *
     * The resulting `JarInputStream` will then be passed to our verification
     * helper to perform additional checks.
     */
    private fun resolveRemoteJar(jarBuffer: Buffer): JarInputStream? {
        /**
         * `JarInputStream` is going to read a segment off the top of the `Buffer`.
         * To allow us to later read from the beginning of the buffer if we encounter
         * a non-standard jar we need to clone the Buffer.
         */
        val bufferClone = jarBuffer.clone()
        val jarStream = JarInputStream(jarBuffer.inputStream())

        /**
         * Internally `JarInputStream` will attempt to read in the `MANIFEST.MF`
         * file from the jar. It first skips `META-INF/` then expects `MANIFEST.MF`
         * as the second file. If it finds a valid manifest file at that location
         * it will parse it and store it.
         *
         * If we are able to find it, we can assume that the jar we are
         * working with is structured properly. Further validation will confirm
         * it's signed with either our own, or Jagex's private keys.
         */
        if (jarStream.manifest != null) {
            /**
             * Since we don't need the duplicated `Buffer` let `okio` handle
             * recycling its resources.
             */
            bufferClone.clear()

            return jarStream
        }

        /**
         * At this point we have a jar file which is not structured according
         * to rules expected by `JarInputStream`. We need to restructure the
         * jar so that it can be properly read.
         *
         * This is an exception case, and most likely will only be encountered
         * then working with original Jagex jars from the 592 era.
         */
        return fixJagexJar(bufferClone)
    }
}
