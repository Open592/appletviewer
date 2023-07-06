package com.open592.appletviewer.assets

import com.open592.appletviewer.events.ApplicationEvent

/**
 * Represents events handled by callers of the AssetHandler
 * classes.
 */
public sealed interface AssetEvent : ApplicationEvent {
    /**
     * Signals the start of a download
     */
    public data class DownloadStart(val fileName: String, val size: Int) : AssetEvent
    /**
     * Signals the progress of a download
     */
    public data class DownloadProgress(val fileName: String, val percentage: Int) : AssetEvent
    /**
     * Signals the completion of a download
     */
    public data class DownloadComplete(val fileName: String, val successful: Boolean) : AssetEvent
}
