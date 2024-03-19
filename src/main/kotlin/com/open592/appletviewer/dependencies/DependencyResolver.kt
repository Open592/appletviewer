package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.jar.SignedJarFileResolver
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.progress.ProgressEvent
import jakarta.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.buffer
import okio.sink
import okio.source

public class DependencyResolver
@Inject
constructor(
    private val config: ApplicationConfiguration,
    private val environment: Environment,
    private val eventBus: GlobalEventBus,
    private val httpClient: OkHttpClient,
    private val paths: ApplicationPaths,
    private val signedJarFileResolver: SignedJarFileResolver,
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
    public fun resolveBrowserControl() {
        val browserControlJarBytes = fetchRemoteDependency(DependencyType.BROWSERCONTROL)
        val filename = getBrowserControlFilename()

        if (browserControlJarBytes == null) {
            throw DependencyResolverException.FetchDependencyException(filename)
        }

        val jarEntries = signedJarFileResolver.resolveEntries(browserControlJarBytes)
        val library = jarEntries[filename] ?: throw DependencyResolverException.VerifyDependencyException()

        // Now that we have verified the jar and extracted the library, write it to the cache directory.
        paths.resolveCacheDirectoryPath(filename).sink().buffer().use { destination ->
            destination.writeAll(library)
        }
    }

    /**
     * Represents the types of remote dependencies this resolver can resolve.
     */
    private enum class DependencyType { BROWSERCONTROL, LOADER }

    /**
     * Fetch a remote jar file from the server, resolving the filename from the
     * application config.
     */
    private fun fetchRemoteDependency(type: DependencyType): Buffer? {
        val url = when (type) {
            DependencyType.BROWSERCONTROL -> getBrowserControlUrl()
            DependencyType.LOADER -> TODO()
        }

        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                response.body.use { responseBody ->
                    val responseSource = responseBody?.byteStream()?.source() ?: return null

                    responseSource.use { source ->
                        val contentLength = responseBody.contentLength().takeIf { it != -1L }
                        val buffer = Buffer()

                        // Before starting the download, update the total download size value if we receive an
                        // explicit size from the server
                        if (contentLength != null) {
                            setFileSize(type, contentLength.toInt())
                        }

                        // In case we can't resolve the content length from the server use the default
                        // specified within the original applet viewer
                        while (source.read(buffer, contentLength ?: 300_000) != -1L) {
                            val progress = ((100 * buffer.size) / getTotalDownloadSize()).toInt()

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
     * Returns the root URL for assets we will be fetching.
     */
    private fun getCodebaseUrl(): String {
        return config.getConfig("codebase")
    }

    /**
     * Get the total size of all dependencies.
     *
     * This is used to calculate the percent completion of the overall
     * dependency download.
     */
    private fun getTotalDownloadSize(): Int {
        return fileSizes.values.reduce { acc, size -> acc + size }
    }

    /**
     * Upon resolving the actual size of the file we can use this function
     * to dynamically update the file size map making the progress indicator
     * more accurate.
     */
    private fun setFileSize(type: DependencyType, size: Int) {
        fileSizes[type] = size
    }

    /**
     * Used for calculating the progress as we are downloading dependencies.
     *
     * The defaults for each file type are the size of the original files
     * from Jagex. Within the runtime code for downloading the files, after
     * resolving the `Content-Length` header, we will dynamically update this
     * map to make the progress reporting more accurate.
     */
    private val fileSizes: MutableMap<DependencyType, Int> = mutableMapOf(
        DependencyType.BROWSERCONTROL to 27_049,
        DependencyType.LOADER to 32_901,
    )
}
