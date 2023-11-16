package com.open592.appletviewer.http

import okhttp3.OkHttpClient
import java.time.Duration

object HttpTestConstants {
    private val timeout = Duration.ofMillis(10)

    val client = OkHttpClient.Builder()
        .connectTimeout(timeout)
        .writeTimeout(timeout)
        .readTimeout(timeout)
        .build()
}
