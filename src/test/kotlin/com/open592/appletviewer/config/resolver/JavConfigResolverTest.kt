package com.open592.appletviewer.config.resolver

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.paths.ApplicationPathsMocks
import com.open592.appletviewer.paths.WindowsApplicationPaths
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
        val applicationPaths = mockk<ApplicationPaths>()
        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

        every { settings.getString("com.jagex.config") } returns ""
        every { settings.getString("com.jagex.configfile") } returns ""

        assertThrows<JavConfigResolveException.MissingConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to load a file from the fs`() {
        val nonexistentFile = "i-dont-exist.ws"
        val config = mockk<ApplicationConfiguration>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val preferences = mockk<AppletViewerPreferences>()

        every { settings.getString("com.jagex.config") } returns ""
        every { settings.getString("com.jagex.configfile") } returns nonexistentFile
        every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
        every { settings.getString("user.dir") } returns "not-a-dir"
        every { settings.getString("user.home") } returns "not-a-dir"

        val applicationPaths = WindowsApplicationPaths(config, FileSystems.getDefault(), settings)
        val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        verify(exactly = 1) { settings.getString("user.dir") }
        verify(exactly = 1) { applicationPaths.resolveGameFileDirectoryPath(nonexistentFile) }
    }

    @Test
    fun `Should throw a LoadConfigurationException when no remote connection could be made`() {
        val applicationPaths = mockk<WindowsApplicationPaths>()
        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

        // Simulates when we attempt to make a connection to an invalid host
        val configUrlTemplate = "https://invalid.connection/k=3/l=\$(Language:0)/jav_config.ws"

        every { settings.getString("com.jagex.config") } returns configUrlTemplate
        every { settings.getString("com.jagex.configfile") } returns ""
        // Verify we can resolve a template param other than the default value
        every { preferences.get("Language") } returns SupportedLanguage.GERMAN.getLanguageId().toString()

        assertThrows<JavConfigResolveException.LoadConfigurationException> { resolver.resolve() }

        verify(exactly = 1) { settings.getString("com.jagex.config") }
        verify(exactly = 1) { settings.getString("com.jagex.configfile") }
        // Need to resolve the URL template
        verify(exactly = 1) { preferences.get("Language") }
    }

    @Test
    fun `Should throw a LoadConfigurationException when unable to fetch remote JavConfig file`() {
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val applicationPaths = mockk<WindowsApplicationPaths>()
        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

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

        val applicationPaths = mockk<ApplicationPaths>()
        val preferences = mockk<AppletViewerPreferences>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

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

        useLocalJavConfigFile(configFile) { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns configFile
            every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
            every { settings.getString("user.dir") } returns (
                fs.getPath(ApplicationPathsMocks.ROOT_DIR, "bin").toAbsolutePath().toString()
                )
            every { settings.getString("user.home") } returns fs
                .getPath("C:\\Users\\test")
                .toAbsolutePath()
                .toString()

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settings)
                val preferences = mockk<AppletViewerPreferences>()
                val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)
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

        useLocalJavConfigFile(invalidJavConfig) { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            every { settings.getString("com.jagex.config") } returns ""
            every { settings.getString("com.jagex.configfile") } returns invalidJavConfig
            every { settings.getString("com.open592.launcherDirectoryOverride") } returns ""
            every { settings.getString("user.home") } returns fs
                .getPath("C:\\Users\\test")
                .toAbsolutePath()
                .toString()
            every { settings.getString("user.dir") } returns (
                fs.getPath(ApplicationPathsMocks.ROOT_DIR, "bin").toAbsolutePath().toString()
                )

            val applicationPaths = WindowsApplicationPaths(config, fs, settings)
            val preferences = mockk<AppletViewerPreferences>()
            val resolver = JavConfigResolver(preferences, applicationPaths, client, settings)

            assertThrows<JavConfigResolveException.DecodeConfigurationException> { resolver.resolve() }

            verify(exactly = 1) { settings.getString("com.jagex.config") }
            verify(exactly = 1) { settings.getString("com.jagex.configfile") }
            verify(exactly = 1) { settings.getString("user.dir") }
            verify(exactly = 1) { applicationPaths.resolveGameFileDirectoryPath(invalidJavConfig) }
        }
    }

    private fun cloneFileBuffer(source: BufferedSource): Buffer {
        source.request(Long.MAX_VALUE)

        val config = source.buffer.clone()

        source.close()

        return config
    }

    private fun useLocalJavConfigFile(filename: String, action: (FileSystem) -> Unit) {
        ApplicationPathsMocks.createLauncherDirectoryStructure().use { fs ->
            val dir = fs.getPath(ApplicationPathsMocks.ROOT_DIR, Constants.GAME_NAME)

            val javConfigStream = JavConfigResolver::class.java.getResourceAsStream(filename)
                ?: throw FileNotFoundException("Failed to find $filename during JavConfigResolverTest")

            val path = dir.resolve(filename).toAbsolutePath()

            Files.copy(javConfigStream, path, StandardCopyOption.REPLACE_EXISTING)

            action(fs)
        }
    }
}
