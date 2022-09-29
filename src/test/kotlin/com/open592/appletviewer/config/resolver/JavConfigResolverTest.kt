package com.open592.appletviewer.config.resolver

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.fetch.AssetFetch
import com.open592.appletviewer.http.MockHttpResponse
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class JavConfigResolverTest {
    @Test
    fun `Should return MissingConfigurationException when unable to find configuration`() {
        val preferences = mockk<AppletViewerPreferences>()
        val fetch = mockk<AssetFetch>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val resolver = JavConfigResolver(preferences, fetch, settings)

        every { settings.getString("com.jagex.config") } returns ""
        every { settings.getString("com.jagex.configfile") } returns ""

        assertThrows<JavConfigResolveException.MissingConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        verify(exactly = 0) { settings.getString("user.dir") }
        verify(exactly = 0) { fetch.fetchLocaleFile(any()) }
        verify(exactly = 0) { fetch.fetchRemoteFile(any()) }
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to load a file from the fs`() {
        val nonexistentFile = "i-dont-exist.ws"
        val preferences = mockk<AppletViewerPreferences>()
        val client = mockk<HttpClient>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        every { settings.getString("com.jagex.config") } returns ""
        every { settings.getString("com.jagex.configfile") } returns nonexistentFile
        every { settings.getString("user.dir") } returns "not-a-dir"

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        verify(exactly = 1) { settings.getString("user.dir") }
        verify(exactly = 1) { fetch.fetchLocaleFile(nonexistentFile) }
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to fetch remote JavConfig file`() {
        val preferences = mockk<AppletViewerPreferences>()
        val client = mockk<HttpClient>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        // Verify we are resolving URLS templates properly
        val configUrlTemplate = "http://www.runescape.com/k=3/l=\$(Language:0)/jav_config.ws"
        val expectedUrl = "http://www.runescape.com/k=3/l=1/jav_config.ws"

        every { settings.getString("com.jagex.config") } returns configUrlTemplate
        every { settings.getString("com.jagex.configfile") } returns ""
        // Verify we can resolve a template param other than the default value
        every { preferences.get("Language") } returns SupportedLanguage.GERMAN.getLanguageId().toString()

        // Construct expected HttpRequest
        //
        // We should fail when receiving non-200 status codes
        val expectedHttpRequest = HttpRequest.newBuilder(URI(expectedUrl))
            .timeout(Duration.ofSeconds(30L))
            .GET()
            .build()
        val expectedHttpStatusCode = 500
        val response = MockHttpResponse<InputStream?>(expectedHttpRequest, null, expectedHttpStatusCode)

        every {
            client.send(expectedHttpRequest, HttpResponse.BodyHandlers.ofInputStream())
        } returns response

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        // Need to resolve the URL template
        verify(exactly = 1) { preferences.get("Language") }
        // We should not be looking at the fs
        verify(exactly = 0) { settings.getString("user.dir") }
        verify(exactly = 1) { client.send(expectedHttpRequest, HttpResponse.BodyHandlers.ofInputStream()) }
    }

    @Test
    fun `Should correctly resolve a remote jav config file`() {
        val configFile = "simple-javconfig.ws"
        val javConfigStream = JavConfigResolverTest::class.java.getResourceAsStream(configFile)
            ?: throw FileNotFoundException("Failed to find $configFile within JavConfigResolverTest")
        val preferences = mockk<AppletViewerPreferences>()
        val client = mockk<HttpClient>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        // Verify we are resolving URLS templates properly
        val configUrlTemplate = "http://www.runescape.com/k=3/l=\$(Language:0)/jav_config.ws"
        val expectedUrl = "http://www.runescape.com/k=3/l=0/jav_config.ws"

        every { settings.getString("com.jagex.config") } returns configUrlTemplate
        every { settings.getString("com.jagex.configfile") } returns ""
        every { preferences.get("Language") } returns SupportedLanguage.ENGLISH.getLanguageId().toString()

        // Construct expected HttpRequest
        val expectedHttpRequest = HttpRequest.newBuilder(URI(expectedUrl)).GET().build()
        val expectedHttpStatusCode = 200

        every {
            client.send(expectedHttpRequest, HttpResponse.BodyHandlers.ofInputStream())
        } returns MockHttpResponse<InputStream>(expectedHttpRequest, javConfigStream, expectedHttpStatusCode)

        assertDoesNotThrow {
            val javConfig = resolver.resolve()

            assertEquals("RuneScape", javConfig.root.getConfig("title"))
            assertEquals("100", javConfig.root.getConfig("viewerversion"))
            assertEquals("English", javConfig.languageNames[SupportedLanguage.ENGLISH])
            assertEquals("0", javConfig.root.getParameter("colourid"))
        }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        // Need to resolve the URL template
        verify(exactly = 1) { preferences.get("Language") }
        // We should not be looking at the fs
        verify(exactly = 0) { settings.getString("user.dir") }
        verify(exactly = 1) { client.send(expectedHttpRequest, HttpResponse.BodyHandlers.ofInputStream()) }
    }

    @Test
    fun `Should correctly read a jav config file from the filesystem`() {
        val configFile = "simple-javconfig.ws"

        useLocalJavConfigFile(configFile) {
            val preferences = mockk<AppletViewerPreferences>()
            val client = mockk<HttpClient>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, it, settings)
            val resolver = JavConfigResolver(preferences, fetch, settings)

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns configFile
            every { settings.getString("user.dir") } returns it.getPath(ROOT).toAbsolutePath().toString()

            assertDoesNotThrow {
                val javConfig = resolver.resolve()

                assertEquals("RuneScape", javConfig.root.getConfig("title"))
                assertEquals("100", javConfig.root.getConfig("viewerversion"))
                assertEquals("English", javConfig.languageNames[SupportedLanguage.ENGLISH])
                assertEquals("0", javConfig.root.getParameter("colourid"))
            }

            verify(exactly = 1) { settings.getString("com.jagex.config") }
            verify(exactly = 1) { settings.getString("com.jagex.configfile") }
            verify(exactly = 1) { settings.getString("user.dir") }
            verify(exactly = 0) { preferences.get(any()) }
            verify(exactly = 1) { fetch.fetchLocaleFile(configFile) }
        }
    }

    @Test
    fun `Should throw DecodeConfigurationException when an invalid JavConfig file is encountered`() {
        val invalidJavConfig = "invalid-javconfig.ws"

        useLocalJavConfigFile(invalidJavConfig) {
            val preferences = mockk<AppletViewerPreferences>()
            val client = mockk<HttpClient>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, it, settings)
            val resolver = JavConfigResolver(preferences, fetch, settings)

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns invalidJavConfig
            every { settings.getString("user.dir") } returns it.getPath(ROOT).toAbsolutePath().toString()

            assertThrows<JavConfigResolveException.DecodeConfigurationException> { resolver.resolve() }

            verify(exactly = 1) { settings.getString("com.jagex.config") }
            verify(exactly = 1) { settings.getString("com.jagex.configfile") }
            verify(exactly = 1) { settings.getString("user.dir") }
            verify(exactly = 1) { fetch.fetchLocaleFile(invalidJavConfig) }
        }
    }

    private fun useLocalJavConfigFile(filename: String, action: (FileSystem) -> Unit) {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        val dir = fs.getPath(ROOT, Constants.GAME_NAME)

        Files.createDirectories(dir)

        val javConfigStream = JavConfigResolver::class.java.getResourceAsStream(filename)
            ?: throw FileNotFoundException("Failed to find $filename during JavConfigResolverTest")

        val path = dir.resolve(filename).toAbsolutePath()

        Files.copy(javConfigStream, path, StandardCopyOption.REPLACE_EXISTING)

        action(fs)
    }

    private companion object {
        private const val ROOT = "user-dir"
    }
}
