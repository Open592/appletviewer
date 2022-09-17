package com.open592.appletviewer.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.net.http.HttpClient
import java.time.Duration
import javax.inject.Provider

public class HttpFetchProvider : Provider<HttpFetch> {
    public override fun get(): HttpFetch {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(Dispatchers.IO.asExecutor())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        return HttpFetch(httpClient)
    }
}
