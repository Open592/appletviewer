package com.open592.appletviewer.dependencies.resolver

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.http.HttpTestConstants
import com.open592.appletviewer.paths.ApplicationPaths
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DependencyResolverTest {
    @Test
    fun `Should properly handle download errors and display the correct fatal error message`() = runTest {
        // Start up mock HTTP server which will fail to resolve the browsercontrol library.
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val config = mockk<ApplicationConfiguration>()
        val environment = mockk<Environment>()
        val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
        val applicationPaths = mockk<ApplicationPaths>()
        val dependencyResolver = DependencyResolver(
            config,
            environment,
            eventBus,
            HttpTestConstants.client,
            applicationPaths,
        )

        val browserControlFilename = "browsercontrol_0_-1928975093.jar"

        // Get mocked browsercontrol URL
        val baseUrl = server.url("/")

        every { environment.getOperatingSystem() } returns OperatingSystem.WINDOWS
        every { environment.getArchitecture() } returns Architecture.X86_64

        every { config.getConfig("browsercontrol_win_amd64_jar") } returns browserControlFilename
        every { config.getConfig("codebase") } returns baseUrl.toString()

        every { config.getContent("err_downloading") } returns "Error Downloading"

        assertThrows<DependencyResolverException.FetchDependencyException> {
            dependencyResolver.resolveBrowserControl()
        }

        verify(exactly = 1) { config.getConfig("browsercontrol_win_amd64_jar") }
        verify(exactly = 1) { config.getConfig("codebase") }
    }
}
