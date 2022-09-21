package com.open592.appletviewer.config

import com.open592.appletviewer.config.language.SupportedLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationConfigurationTest {
    @Test
    fun `Should successfully return packaged content without a backing JavConfig`() {
        val config = ApplicationConfiguration(SupportedLanguage.ENGLISH)

        assertEquals("Loading configuration", config.getContent("loading_config"))
    }
}
