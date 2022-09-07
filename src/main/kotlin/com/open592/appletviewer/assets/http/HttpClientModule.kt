package com.open592.appletviewer.assets.http

import com.google.inject.AbstractModule
import java.net.http.HttpClient

public object HttpClientModule : AbstractModule() {
    public override fun configure() {
        bind(HttpClient::class.java)
            .toProvider(HttpClientProvider::class.java)
    }
}
