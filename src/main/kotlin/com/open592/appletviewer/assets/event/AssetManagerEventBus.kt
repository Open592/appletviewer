package com.open592.appletviewer.assets.event

import com.open592.appletviewer.assets.RemoteAsset
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.net.URI

public class AssetManagerEventBus : EventBus<AssetManagerEvent>(Dispatchers.IO) {
    public fun dispatchTriggerAssetRequestEvent(asset: RemoteAsset, url: URI) {
        scope.launch {
            emitEvent(AssetManagerEvent.TriggerAssetRequest(asset, url))
        }
    }

    public fun dispatchAssetRequestCompletedEvent(asset: RemoteAsset, reader: BufferedReader?) {
        scope.launch {
            emitEvent(AssetManagerEvent.AssetRequestCompleted(asset, reader))
        }
    }
}
