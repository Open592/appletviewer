package com.open592.appletviewer.dependencies

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.http.HttpTestConstants
import com.open592.appletviewer.jar.CertificateValidator
import com.open592.appletviewer.jar.SignedJarFileResolver
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.paths.WindowsApplicationPaths
import com.open592.appletviewer.progress.ProgressEvent
import com.open592.appletviewer.settings.SettingsStore
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.source
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.FileNotFoundException
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserControlResolverTest {
    @Test
    fun `Should return a ResolveException if we fail to download the browsercontrol library`() {
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val applicationPaths = mockk<ApplicationPaths>()
        val configuration = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val settingsStore = mockk<SettingsStore>()

        val remoteDependencyFetcher = RemoteDependencyFetcher(HttpTestConstants.client, eventBus)

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns FAKE_THAWTE_PUBLIC_KEY
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns FAKE_JAGEX_PUBLIC_KEY
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val signedJarFileResolver = SignedJarFileResolver(certificateValidator)

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        val browserControlResolver = BrowserControlResolver(
            applicationPaths,
            configuration,
            remoteDependencyFetcher,
            signedJarFileResolver,
            environment,
        )

        every { configuration.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { configuration.getConfig("codebase") } returns server.url("/").toString()
        every { configuration.getContent("err_load_bc") } returns EXPECTED_LOAD_BROWSERCONTROL_ERROR_CONTENT

        val exception = assertThrows<RemoteDependencyResolver.ResolveException> {
            browserControlResolver.resolve()
        }

        assertEquals(exception.message, EXPECTED_LOAD_BROWSERCONTROL_ERROR_CONTENT)
    }

    @Test
    fun `Should return a ResolveException if we encounter an invalid browsercontrol file`() {
        val mockServer = serveBrowsercontrolTestFile("invalid-browsercontrol-entry-file-type.jar")

        val applicationPaths = mockk<ApplicationPaths>()
        val configuration = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val settingsStore = mockk<SettingsStore>()

        val remoteDependencyFetcher = RemoteDependencyFetcher(HttpTestConstants.client, eventBus)

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns FAKE_THAWTE_PUBLIC_KEY
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns FAKE_JAGEX_PUBLIC_KEY
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val signedJarFileResolver = SignedJarFileResolver(certificateValidator)

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        val browserControlResolver = BrowserControlResolver(
            applicationPaths,
            configuration,
            remoteDependencyFetcher,
            signedJarFileResolver,
            environment,
        )

        every { configuration.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { configuration.getConfig("codebase") } returns mockServer.url("/").toString()
        every { configuration.getContent("err_verify_bc64") } returns EXPECTED_VERIFY_BROWSERCONTROL64_ERROR_CONTENT
        justRun { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }

        val exception = assertThrows<RemoteDependencyResolver.ResolveException> {
            browserControlResolver.resolve()
        }

        assertEquals(exception.message, EXPECTED_VERIFY_BROWSERCONTROL64_ERROR_CONTENT)
        verify(atLeast = 1) { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }
    }

    /**
     * The following test cases verify that we handle both Jagex and Open592 browsercontrol libary
     * jars.
     *
     * The only difference is in the underlying library file naming conventions. Jagex included
     * architecture information in the filename, while we do not.
     *
     * NOTE: The reason I'm not able to provide an original Jagex browsercontrol jar is because
     * the jar file itself never persisted to disk and at the time of the 592 era nobody
     * (that I've found) was archiving the file itself.
     */
    @ParameterizedTest
    @ValueSource(
        strings = [
            "valid-simulated-jagex-windows-browsercontrol64.jar",
            "valid-open592-windows-browsercontrol.jar",
        ],
    )
    fun `Should properly resolve valid browsercontrol Windows test jars`(filename: String) {
        val mockServer = serveBrowsercontrolTestFile(filename)

        val fs = MemoryFileSystemBuilder.newWindows().addUser("test").build()

        val configuration = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val settingsStore = mockk<SettingsStore>()

        val applicationPaths = WindowsApplicationPaths(configuration, fs, settingsStore)
        val remoteDependencyFetcher = RemoteDependencyFetcher(HttpTestConstants.client, eventBus)

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns FAKE_THAWTE_PUBLIC_KEY
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns FAKE_JAGEX_PUBLIC_KEY
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val signedJarFileResolver = SignedJarFileResolver(certificateValidator)

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        val browserControlResolver = BrowserControlResolver(
            applicationPaths,
            configuration,
            remoteDependencyFetcher,
            signedJarFileResolver,
            environment,
        )

        every { configuration.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { configuration.getConfig("codebase") } returns mockServer.url("/").toString()
        every { configuration.getConfig("cachesubdir") } returns Constants.GAME_NAME
        every { configuration.getConfigAsInt("modewhat") } returns 0
        every { settingsStore.getString("user.home") } returns fs.getPath("C:\\Users\\test").toAbsolutePath().toString()
        justRun { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }

        assertDoesNotThrow { browserControlResolver.resolve() }

        val libraryFileContents = fs
            .getPath("C:\\rscache\\.jagex_cache_32\\runescape\\browsercontrol64.dll")
            .readText()
            .trim()

        assertEquals("open592-test", libraryFileContents)

        // Verify just the calls executed by DependencyResolver
        verify(exactly = 1) { configuration.getConfig("browsercontrol_win_amd64_jar") }
        verify(exactly = 1) { configuration.getConfig("codebase") }
        verify(atLeast = 1) { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }
    }

    private fun serveBrowsercontrolTestFile(filename: String): MockWebServer {
        val file = this::class.java.getResourceAsStream(filename)
            ?.source()?.buffer()
            ?: throw FileNotFoundException("Failed to find $filename in DependencyResolverTest")
        val buffer = cloneFile(file)

        val server = MockWebServer()

        server.enqueue(MockResponse().setBody(buffer).setResponseCode(200))

        server.start()

        return server
    }

    private fun cloneFile(file: BufferedSource): Buffer {
        val sink = Buffer()

        file.use {
            it.readAll(sink)
        }

        return sink
    }

    private companion object {
        private const val DEFAULT_BROWSERCONTROL_FILENAME = "browsercontrol_0_-1928975093.jar"

        /**
         * Fake Thawte public key used in our test jars.
         */
        private const val FAKE_THAWTE_PUBLIC_KEY: String =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCg1kw0oUcTFY5OTCluVXHSCOA33ePAePh9lMCRewLpX9XtVVsqDzhopFi4WD7ih19" +
                "zpdXvMcK61HI7/99TnoD8upiBIeH3bP3h/30i1rQ6S4kDYLBDF58ErzWqp71NurE35sa1bFF1SFtBy17AuSkJLszcK+Z+0Auxvd3" +
                "0sFcXQIDAQAB"

        /**
         * Fake Jagex public key used in our test jars.
         */
        private const val FAKE_JAGEX_PUBLIC_KEY: String =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu5fJjw2jQ5wmDqjeEf7kYtwmVhk8Fdy0y1Vg+G6azsCiW68pYRaLW7kr/VHf" +
                "pl6eYyupfpfDnyWqTxGvKoHT28dJHETjN+PLubOGhiwL0KYMx6CUIoTBKXMRRBIa6P07RLYJu9fJyFtKmhb+ept0os+hUDUYquOg" +
                "CgNF42C2rpmNe3cxm1BO1EGDFXwZHBzwVX06F1v+xcnwkxBqCOFg1zuNpqlK/2THZX3iaMnnjl8B7ad77D+7vzAQThdMPIOj4MmW" +
                "5CGX70fQgyCRVXRYeRXvvbCpPNPUiZ2jtWCIib6G4pUPp1uAGQXouILp/wMQPhW4EoGABc21B8LVpboM8QIDAQAB"

        private const val EXPECTED_LOAD_BROWSERCONTROL_ERROR_CONTENT = "Unable to load browsercontrol"
        private const val EXPECTED_VERIFY_BROWSERCONTROL64_ERROR_CONTENT = "Unable to verify browsercontrol64"
    }
}
