package com.open592.appletviewer.config

import com.open592.appletviewer.config.resolver.ConfigurationResolver
import com.open592.appletviewer.http.HttpFetch
import io.mockk.mockk
import java.nio.file.Path

object ApplicationConfigurationMocks {
    fun generateValid592Configuration() {
        val fetch = mockk<HttpFetch>()
        val resolver = ConfigurationResolver()
    }

    private val VALID_DIR = Path.of(ApplicationConfigurationMocks::class.java.getResource("valid").toURI())
}
