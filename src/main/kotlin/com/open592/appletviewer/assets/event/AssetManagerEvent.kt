package com.open592.appletviewer.assets.event

import com.open592.appletviewer.assets.RemoteAsset
import com.open592.appletviewer.event.ApplicationEvent
import java.io.BufferedReader
import java.net.URI

/**
 * Asset manager provides two events - one for requesting a remote asset
 * to be downloaded, a second for when that request as been completed.
 *
 * We know all assets which will be requested by the applet viewer, and
 * use an enum representing them as the key to which request as been
 * completed.
 *
 * When a request is unsuccessful our `AssetRequestCompleted` event will
 * have a `null` `BufferedReader` value.
 */
public sealed interface AssetManagerEvent : ApplicationEvent {
    public data class TriggerAssetRequest(public val asset: RemoteAsset, public val url: URI) : AssetManagerEvent
    public data class AssetRequestCompleted(public val asset: RemoteAsset, public val reader: BufferedReader?) : AssetManagerEvent
}
