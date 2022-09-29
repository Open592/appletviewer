package com.open592.appletviewer.fetch

import com.open592.appletviewer.settings.SettingsStore
import java.net.http.HttpClient
import java.nio.file.FileSystems
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

public class AssetFetchProvider @Inject constructor(
    private val settingsStore: SettingsStore
) : Provider<AssetFetch> {
    public override fun get(): AssetFetch {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30L))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        return AssetFetch(client, FileSystems.getDefault(), settingsStore)
    }
}
