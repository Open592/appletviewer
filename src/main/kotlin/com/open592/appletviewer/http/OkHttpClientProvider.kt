package com.open592.appletviewer.http

import okhttp3.OkHttpClient
import java.time.Duration
import javax.inject.Provider

public class OkHttpClientProvider : Provider<OkHttpClient> {
    override fun get(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT)
            .writeTimeout(TIMEOUT)
            .readTimeout(TIMEOUT)
            .followRedirects(true)
            .build()
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofMinutes(1)
    }
}
