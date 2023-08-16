package com.open592.appletviewer.http

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import okhttp3.OkHttpClient

public object OkHttpClientModule : AbstractModule() {
    override fun configure() {
        bind(OkHttpClient::class.java)
            .toProvider(OkHttpClientProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
