package com.open592.appletviewer.http

import java.net.http.HttpClient
import java.time.Duration
import javax.inject.Provider

public class HttpFetchProvider : Provider<HttpFetch> {
    public override fun get(): HttpFetch {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30L))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        return HttpFetch(client)
    }
}
