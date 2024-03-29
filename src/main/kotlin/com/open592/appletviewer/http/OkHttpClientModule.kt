package com.open592.appletviewer.http

import com.google.inject.AbstractModule
import okhttp3.OkHttpClient

public object OkHttpClientModule : AbstractModule() {
    override fun configure() {
        bind(OkHttpClient::class.java).toInstance(OkHttpClient.Builder().build())
    }
}
