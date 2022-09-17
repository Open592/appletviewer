package com.open592.appletviewer.http

import java.io.BufferedReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.inject.Inject

public class HttpFetch @Inject constructor(
    private val httpClient: HttpClient
) {
    public fun get(url: String): BufferedReader? {
        try {
            val request = HttpRequest.newBuilder(URI(url))
                .timeout(Duration.ofSeconds(30L))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

            if (response.statusCode() != EXPECTED_STATUS_CODE) {
                return null
            }

            return response.body().bufferedReader()
        } catch (e: Exception) {
            return null
        }
    }

    private companion object {
        const val EXPECTED_STATUS_CODE = 200
    }
}
