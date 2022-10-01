package com.open592.appletviewer.fetch

import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import java.nio.file.FileSystems
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

public class AssetFetchProvider @Inject constructor(
    private val settingsStore: SettingsStore
) : Provider<AssetFetch> {
    public override fun get(): AssetFetch {
        val timeout = Duration.ofMinutes(1)
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout)
            .writeTimeout(timeout)
            .readTimeout(timeout)
            .followRedirects(true)
            .build()

        return AssetFetch(client, FileSystems.getDefault(), settingsStore)
    }
}
