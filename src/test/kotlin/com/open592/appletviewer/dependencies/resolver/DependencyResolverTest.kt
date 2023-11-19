package com.open592.appletviewer.dependencies.resolver

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.http.HttpTestConstants
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.progress.ProgressEvent
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

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

        val browserControlFilename = "browsercontrol_0_-1928975093.jar"

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns browserControlFilename
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
            "invalid-browsercontrol-not-signed.jar",
            "invalid-browsercontrol-entry-file-type.jar",
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

        val browserControlFilename = "browsercontrol_0_-1928975093.jar"

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns browserControlFilename
        every { config.getConfig("codebase") } returns server.url("/").toString()
        justRun { eventBus.dispatch(any(ProgressEvent.UpdateProgress::class)) }

        assertThrows<DependencyResolverException.VerifyDependencyException> {
            dependencyResolver.resolveBrowserControl()
        }

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
}
