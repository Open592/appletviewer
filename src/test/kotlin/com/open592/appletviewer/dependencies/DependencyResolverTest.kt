package com.open592.appletviewer.dependencies

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.http.HttpTestConstants
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.paths.WindowsApplicationPaths
import com.open592.appletviewer.progress.ProgressEvent
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.BufferedSource
import okio.FileNotFoundException
import okio.buffer
import okio.source
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class DependencyResolverTest {
    @Test
    fun `Should throw a FetchDependencyException if we fail to download the browsercontrol library`() {
        // Start up mock HTTP server which will fail to resolve the browsercontrol library.
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val config = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val applicationPaths = mockk<ApplicationPaths>()
        val dependencyResolver = DependencyResolver(
            config,
            environment,
            eventBus,
            HttpTestConstants.client,
            applicationPaths,
        )

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { config.getConfig("codebase") } returns server.url("/").toString()

        assertThrows<DependencyResolverException.FetchDependencyException> {
            dependencyResolver.resolveBrowserControl()
        }

        verify(exactly = 1) { config.getConfig("browsercontrol_win_amd64_jar") }
        verify(exactly = 1) { config.getConfig("codebase") }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid-browsercontrol-file-type.txt",
            "invalid-browsercontrol-entry-file-type.jar",
            "invalid-browsercontrol-not-signed.jar",
        ],
    )
    fun `Should throw VerifyDependencyException when encountering an invalid browsercontrol file`(filename: String) {
        val server = MockWebServer()

        val invalidBrowsercontrolFile = this::class.java.getResourceAsStream(filename)
            ?.source()?.buffer()
            ?: throw FileNotFoundException("Failed to find $filename in DependencyResolverTest")
        val invalidBrowsercontrolBuffer = cloneFile(invalidBrowsercontrolFile)

        server.enqueue(MockResponse().setBody(invalidBrowsercontrolBuffer).setResponseCode(200))

        server.start()

        val config = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val applicationPaths = mockk<ApplicationPaths>()
        val dependencyResolver = DependencyResolver(
            config,
            environment,
            eventBus,
            HttpTestConstants.client,
            applicationPaths,
        )

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { config.getConfig("codebase") } returns server.url("/").toString()
        justRun { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }

        assertThrows<DependencyResolverException.VerifyDependencyException> {
            dependencyResolver.resolveBrowserControl()
        }

        verify(exactly = 1) { config.getConfig("browsercontrol_win_amd64_jar") }
        verify(exactly = 1) { config.getConfig("codebase") }
        verify(atLeast = 1) { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }
    }

    /**
     * NOTE: I have not been able to find an original browsercontrol jar from the 592 era. This is due
     * to only the library files, not the jar file itself, being downloaded to the user's computer.
     * Because of this I am unable to write a test for the browsercontrol routines which verify original
     * Jagex jar handling. I _will_ however be able to verify these routines in the loader handling.
     */
    @Test
    fun `Should properly resolve a browsercontrol library file from a Open592 browsercontrol jar`() {
        val server = MockWebServer()

        val filename = "valid-open592-browsercontrol.jar"

        val validBrowsercontrolFile = this::class.java.getResourceAsStream(filename)
            ?.source()?.buffer()
            ?: throw FileNotFoundException("Failed to find $filename in DependencyResolverTest")
        val validBrowsercontrolBuffer = cloneFile(validBrowsercontrolFile)

        server.enqueue(MockResponse().setBody(validBrowsercontrolBuffer).setResponseCode(200))

        server.start()

        val fs = MemoryFileSystemBuilder.newWindows().addUser("test").build()

        val config = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = mockk<GlobalEventBus>()
        val settings = mockk<SystemPropertiesSettingsStore>()
        val applicationPaths = WindowsApplicationPaths(config, fs, settings)
        val dependencyResolver = DependencyResolver(
            config,
            environment,
            eventBus,
            HttpTestConstants.client,
            applicationPaths,
        )

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns DEFAULT_BROWSERCONTROL_FILENAME
        every { config.getConfig("codebase") } returns server.url("/").toString()
        every { config.getConfig("cachesubdir") } returns Constants.GAME_NAME
        every { config.getConfigAsInt("modewhat") } returns 0

        every { settings.getString("user.home") } returns fs.getPath("C:\\Users\\test").toAbsolutePath().toString()

        justRun { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }

        assertDoesNotThrow { dependencyResolver.resolveBrowserControl() }

        val libraryFileContents = fs
            .getPath("C:\\rscache\\.jagex_cache_32\\runescape\\browsercontrol64.dll")
            .readText()
            .trim()

        assertEquals("remote browsercontrol64.dll", libraryFileContents)

        // Verify just the calls executed by DependencyResolver
        verify(exactly = 1) { config.getConfig("browsercontrol_win_amd64_jar") }
        verify(exactly = 1) { config.getConfig("codebase") }
        verify(atLeast = 1) { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }
    }

    private fun cloneFile(file: BufferedSource): Buffer {
        val sink = Buffer()

        file.use {
            it.readAll(sink)
        }

        return sink
    }

    private companion object {
        const val DEFAULT_BROWSERCONTROL_FILENAME = "browsercontrol_0_-1928975093.jar"
    }
}
