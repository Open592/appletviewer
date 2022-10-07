package com.open592.appletviewer.config.resolver

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.fetch.AssetFetch
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.source
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class JavConfigResolverTest {
    private val timeout = Duration.ofMinutes(1)
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeout)
        .writeTimeout(timeout)
        .readTimeout(timeout)
        .followRedirects(true)
        .build()

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
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to load a file from the fs`() {
        val nonexistentFile = "i-dont-exist.ws"
        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        every { settings.getString("com.jagex.config") } returns ""
        every { settings.getString("com.jagex.configfile") } returns nonexistentFile
        every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
        every { settings.getString("user.dir") } returns "not-a-dir"

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        verify(exactly = 1) { settings.getString("user.dir") }
        verify(exactly = 1) { fetch.fetchLocaleFile(nonexistentFile) }
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to fetch remote JavConfig file`() {
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        // Get mocked URL
        val baseUrl = server.url("/")
        val expectedUrl = server.url("/k=3/l=1/jav_config.ws")

        // Verify we are resolving URLS templates properly
        val configUrlTemplate = "${baseUrl}k=3/l=\$(Language:0)/jav_config.ws"

        every { settings.getString("com.jagex.config") } returns configUrlTemplate
        every { settings.getString("com.jagex.configfile") } returns ""
        // Verify we can resolve a template param other than the default value
        every { preferences.get("Language") } returns SupportedLanguage.GERMAN.getLanguageId().toString()

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        // Need to resolve the URL template
        verify(exactly = 1) { preferences.get("Language") }

        // Verify we are making the correct calls
        val request = server.takeRequest()

        assertEquals(request.method, "GET")
        assertEquals(request.requestUrl, expectedUrl)

        server.close()
    }

    @Test
    fun `Should correctly resolve a remote jav config file`() {
        val configFile = "simple-javconfig.ws"
        val configBuffer = JavConfigResolverTest::class.java.getResourceAsStream(configFile)?.source()?.buffer()
            ?: throw FileNotFoundException("Failed to find $configFile within JavConfigResolverTest")
        val config = cloneFileBuffer(configBuffer)
        val server = MockWebServer()

        server.enqueue(MockResponse().setBody(config).setResponseCode(200))

        server.start()

        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, fetch, settings)

        // Verify we are resolving URLS templates properly
        val baseUrl = server.url("/")
        val configUrlTemplate = "${baseUrl}k=3/l=\$(Language:0)/jav_config.ws"
        val expectedUrl = server.url("/k=3/l=0/jav_config.ws")

        every { settings.getString("com.jagex.config") } returns configUrlTemplate
        every { settings.getString("com.jagex.configfile") } returns ""
        every { preferences.get("Language") } returns SupportedLanguage.ENGLISH.getLanguageId().toString()

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

        // Verify we are making the correct calls
        val request = server.takeRequest()

        assertEquals(request.method, "GET")
        assertEquals(request.requestUrl, expectedUrl)

        server.close()
    }

    @Test
    fun `Should correctly read a jav config file from the filesystem`() {
        val configFile = "simple-javconfig.ws"

        useLocalJavConfigFile(configFile) {
            val preferences = mockk<AppletViewerPreferences>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, it, settings)
            val resolver = JavConfigResolver(preferences, fetch, settings)

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns configFile
            every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
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
        }
    }

    @Test
    fun `Should throw DecodeConfigurationException when an invalid JavConfig file is encountered`() {
        val invalidJavConfig = "invalid-javconfig.ws"

        useLocalJavConfigFile(invalidJavConfig) {
            val preferences = mockk<AppletViewerPreferences>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, it, settings)
            val resolver = JavConfigResolver(preferences, fetch, settings)

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns invalidJavConfig
            every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
            every { settings.getString("user.dir") } returns it.getPath(ROOT).toAbsolutePath().toString()

            assertThrows<JavConfigResolveException.DecodeConfigurationException> { resolver.resolve() }

            verify(exactly = 1) { settings.getString("com.jagex.config") }
            verify(exactly = 1) { settings.getString("com.jagex.configfile") }
            verify(exactly = 1) { settings.getString("user.dir") }
            verify(exactly = 1) { fetch.fetchLocaleFile(invalidJavConfig) }
        }
    }

    private fun cloneFileBuffer(source: BufferedSource): Buffer {
        source.request(Long.MAX_VALUE)

        val config = source.buffer.clone()

        source.close()

        return config
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

        fs.close()
    }

    private companion object {
        private const val ROOT = "user-dir"
    }
}
