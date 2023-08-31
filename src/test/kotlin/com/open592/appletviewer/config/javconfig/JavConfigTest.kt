package com.open592.appletviewer.config.javconfig

import com.open592.appletviewer.config.language.SupportedLanguage
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals

class JavConfigTest {
    @Test
    fun `Should throw exception when encountering an invalid configuration item`() {
        useJavConfigFile("invalid-config-javconfig.ws") {
            assertThrows<Exception> { JavConfig.parse(it) }
        }
    }

    @Test
    fun `Should throw exception when encountering an invalid content declaration`() {
        useJavConfigFile("invalid-content-javconfig.ws") {
            assertThrows<Exception> { JavConfig.parse(it) }
        }
    }

    @Test
    fun `Should throw exception when encountering an invalid parameter declaration`() {
        useJavConfigFile("invalid-parameter-javconfig.ws") {
            assertThrows<Exception> { JavConfig.parse(it) }
        }
    }

    @Test
    fun `Should throw exception when encountering an invalid server-block declaration`() {
        useJavConfigFile("invalid-server-block-javconfig.ws") {
            assertThrows<Exception> { JavConfig.parse(it) }
        }
    }

    @Test
    fun `Should correctly parse and store language name declarations`() {
        useJavConfigFile("valid-language-names-javconfig.ws") {
            assertDoesNotThrow {
                val config = JavConfig.parse(it)
                val expectedLanguageCount = 4

                assertEquals(expectedLanguageCount, config.languageNames.size)

                // Make sure that not only are we receiving all expected
                // languages, but that they are store in the correct order.
                config.languageNames.keys.forEachIndexed { index, language ->
                    assertEquals(
                        language,
                        SupportedLanguage.resolveFromLanguageId(index),
                    )
                }
            }
        }
    }

    @Test
    fun `Should correctly parse and store configurations with multiple server blocks`() {
        useJavConfigFile("valid-multiple-server-blocks-javconfig.ws") {
            assertDoesNotThrow {
                val config = JavConfig.parse(it)
                val serverOneKey = "server_one"
                val serverTwoKey = "server_two"
                val expectedOrder = arrayListOf(serverOneKey, serverTwoKey)

                // Validate root has correct keys
                assertEquals("RuneScape", config.root.getConfig("title"))
                assertEquals("100", config.root.getConfig("viewerversion"))
                assertEquals("Welcome to RuneScape", config.root.getContent("welcome"))
                assertEquals("0", config.root.getParameter("colourid"))

                // Validate order of configuration file is preserved
                config.overrides.asIterable().forEachIndexed { index, server ->
                    assertEquals(
                        expectedOrder[index],
                        server.key,
                    )
                }

                // Validate ServerOne
                assertEquals("ServerOne", config.overrides[serverOneKey]?.getConfig("servername"))
                assertEquals("Welcome to ServerOne", config.overrides[serverOneKey]?.getContent("welcome"))
                assertEquals("1", config.overrides[serverOneKey]?.getParameter("colourid"))

                // Validate ServerTwo
                assertEquals("ServerTwo", config.overrides[serverTwoKey]?.getConfig("servername"))
                assertEquals("Welcome to ServerTwo", config.overrides[serverTwoKey]?.getContent("welcome"))
                assertEquals("2", config.overrides[serverTwoKey]?.getParameter("colourid"))
            }
        }
    }

    private fun useJavConfigFile(
        filename: String,
        action: (String) -> Unit,
    ) {
        val javConfig =
            JavConfigTest::class.java.getResource(filename)?.readText()
                ?: throw FileNotFoundException("Failed to find $filename during JavConfigTest")

        action(javConfig)
    }
}
