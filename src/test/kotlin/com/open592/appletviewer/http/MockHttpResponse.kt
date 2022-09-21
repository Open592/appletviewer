package com.open592.appletviewer.http

import java.net.URI
import java.net.http.HttpClient.Version
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Optional
import javax.net.ssl.SSLSession

/**
 * Small mock class for HttpResponse
 *
 * Does the bare minimum to simulate network responses
 *
 * TODO: Is there a better way?
 */
class MockHttpResponse<T> constructor(
    private val request: HttpRequest,
    private val body: T,
    private val statusCode: Int
) : HttpResponse<T> {
    override fun body(): T {
        return body
    }

    override fun headers(): HttpHeaders {
        return HttpHeaders.of(mapOf()) { _: String, _: String -> true }
    }

    override fun previousResponse(): Optional<HttpResponse<T>> {
        return Optional.empty()
    }

    override fun request(): HttpRequest {
        return request
    }

    override fun statusCode(): Int {
        return statusCode
    }

    override fun sslSession(): Optional<SSLSession> {
        return Optional.empty()
    }

    override fun uri(): URI {
        return request.uri()
    }

    override fun version(): Version {
        return Version.HTTP_1_1
    }
}
