package com.open592.appletviewer.dependencies

import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.progress.ProgressEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.source

/**
 * Handles resolving the remote dependencies as well as updating the
 * progress bar with the status of the download.
 */
@Singleton
public class RemoteDependencyFetcher @Inject constructor(
    private val httpClient: OkHttpClient,
    private val eventBus: GlobalEventBus,
) {
    /**
     * Fetch the remote dependency from the provided url.
     */
    public fun fetchRemoteDependency(type: DependencyType, url: String): Buffer? {
        try {
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                response.body.use { body ->
                    val bodySource = body?.byteStream()?.source() ?: return null

                    bodySource.use { source ->
                        val contentLength = body.contentLength().takeIf { it != -1L }
                        val buffer = Buffer()

                        // Before starting the download, update the total download size value if we receive an
                        // explicit size from the server
                        if (contentLength != null) {
                            setFileSize(type, contentLength.toInt())
                        }

                        val totalDownloadSize = getTotalDownloadSize()
                        do {
                            // In case we can't resolve the content length from the server use the default
                            // specified within the original applet viewer
                            val bytesRead = source.read(buffer, contentLength ?: 300_000)

                            totalDownloadedBytes += bytesRead.toInt()

                            val progress = ((100.0 * totalDownloadedBytes) / totalDownloadSize).toInt()

                            eventBus.dispatch(ProgressEvent.UpdateProgress(progress))
                        } while (bytesRead != -1L)

                        return buffer
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Keeps track of the total number of bytes we have downloaded across
     * all dependency types.
     */
    private var totalDownloadedBytes: Int = 0

    /**
     * Get the total size of all dependencies.
     *
     * This is used to calculate the percent completion of the overall
     * dependency download.
     */
    private fun getTotalDownloadSize(): Int {
        return fileSizes.values.sumOf { it }
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
        DependencyType.BROWSERCONTROL to DEFAULT_BROWSERCONTROL_SIZE,
        DependencyType.LOADER to DEFAULT_LOADER_SIZE,
    )

    private companion object {
        /**
         * These defaults were found within the original Jagex applet viewer.
         */
        private const val DEFAULT_BROWSERCONTROL_SIZE = 27_049
        private const val DEFAULT_LOADER_SIZE = 32_901
    }
}
