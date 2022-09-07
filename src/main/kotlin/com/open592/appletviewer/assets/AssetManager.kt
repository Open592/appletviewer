package com.open592.appletviewer.assets

import com.open592.appletviewer.assets.event.AssetManagerEvent
import com.open592.appletviewer.assets.event.AssetManagerEventBus
import com.open592.appletviewer.event.ApplicationEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class AssetManager @Inject constructor(
    public val eventBus: AssetManagerEventBus,
    private val httpClient: HttpClient,
): ApplicationEventListener<AssetManagerEvent>(eventBus) {
    protected override suspend fun processEvent(event: AssetManagerEvent) {
        when(event) {
            is AssetManagerEvent.TriggerAssetRequest -> processAssetRequest(event.asset, event.url)
            else -> return // Callers should handle their own completion events
        }
    }

    private suspend fun processAssetRequest(asset: RemoteAsset, url: URI) {
        try {
            val request = HttpRequest.newBuilder(url)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()

            val response = withContext(Dispatchers.IO) {
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).join()
            }

            val responseCode = response.statusCode()

            if (responseCode != 200) {
                throw IOException("Unexpected status code: $responseCode - Expected 200")
            }

            withContext(Dispatchers.IO) {
                response.body().bufferedReader().use {
                    eventBus.dispatchAssetRequestCompletedEvent(asset, it)
                }
            }
        } catch (e: Exception) {
            handleRequestFailure(asset)
        }
    }

    private fun handleRequestFailure(asset: RemoteAsset) {
        eventBus.dispatchAssetRequestCompletedEvent(asset, null)
    }
}
