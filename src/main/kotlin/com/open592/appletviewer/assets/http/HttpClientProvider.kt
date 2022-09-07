package com.open592.appletviewer.assets.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.net.http.HttpClient
import java.time.Duration
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
public class HttpClientProvider : Provider<HttpClient> {
    public override fun get(): HttpClient {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(Dispatchers.IO.asExecutor())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }
}
