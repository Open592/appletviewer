package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.OutputCapture
import com.open592.appletviewer.debug.capture.SystemOutInterceptor
import com.open592.appletviewer.debug.view.DebugConsoleView
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.settings.SettingsStore
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebugConsoleTest {
    @Test
    fun `Should not capture messages when debug mode is not true`() = runTest {
        val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
        val debugConsoleView = mockk<DebugConsoleView>()
        // Mocking this purely to make sure we aren't invoking it
        val outputCapture = mockk<OutputCapture>()
        val settings = mockk<SettingsStore>()

        // Mock that we aren't running in debug mode
        every { settings.getBoolean(DEBUG_PROPERTY) } returns false
        every { settings.getBoolean(DISABLE_PROPERTY) } returns false

        val debugConsole = DebugConsole(eventBus, debugConsoleView, outputCapture, settings)

        debugConsole.initialize()

        // Make sure we if we are running in debug mode and then short-circuited
        verify(exactly = 1) { settings.getBoolean(DEBUG_PROPERTY) }
        verify(exactly = 1) { settings.getBoolean(DISABLE_PROPERTY) }
        verify(exactly = 0) { settings.getBoolean(LOG_TO_SYSTEM_STREAM_PROPERTY) }
        // Verify we are not invoking the outputCapture
        verify(exactly = 0) { outputCapture.capture(false) }
    }

    @Test
    fun `Should not capture messages even when in debug mode if the disableDebugConsole flag is set`() = runTest {
        val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
        val debugConsoleView = mockk<DebugConsoleView>()
        // Mocking this purely to make sure we aren't invoking it
        val outputCapture = mockk<OutputCapture>()
        val settings = mockk<SettingsStore>()

        // Mock that we aren't running in debug mode
        every { settings.getBoolean(DEBUG_PROPERTY) } returns true
        every { settings.getBoolean(DISABLE_PROPERTY) } returns true

        val debugConsole = DebugConsole(eventBus, debugConsoleView, outputCapture, settings)

        debugConsole.initialize()

        // Make sure, that even if debug mode is turned on, we still short circuit due to disable property being set
        verify(exactly = 1) { settings.getBoolean(DEBUG_PROPERTY) }
        verify(exactly = 1) { settings.getBoolean(DISABLE_PROPERTY) }
        verify(exactly = 0) { settings.getBoolean(LOG_TO_SYSTEM_STREAM_PROPERTY) }
        // Verify we are not invoking the outputCapture
        verify(exactly = 0) { outputCapture.capture(false) }
    }

    @Test
    fun `Should capture messages and initialize the component`() = runTest {
        val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
        val debugConsoleView = mockk<DebugConsoleView>()
        // Mocking this purely to make sure we aren't invoking it
        val outputCapture = OutputCapture(setOf(SystemOutInterceptor(eventBus)))
        val settings = mockk<SettingsStore>()

        every { settings.getBoolean(DEBUG_PROPERTY) } returns true
        every { settings.getBoolean(DISABLE_PROPERTY) } returns false
        every { settings.getBoolean(LOG_TO_SYSTEM_STREAM_PROPERTY) } returns false

        val debugConsole = DebugConsole(eventBus, debugConsoleView, outputCapture, settings)

        debugConsole.initialize()

        verify(exactly = 1) { settings.getBoolean(DEBUG_PROPERTY) }
        verify(exactly = 1) { settings.getBoolean(DISABLE_PROPERTY) }
        verify(exactly = 1) { settings.getBoolean(LOG_TO_SYSTEM_STREAM_PROPERTY) }

        // Log an event to trigger the output capture to emit a message which will be received by the
        // debug console. This will then trigger the view to be initialized and a message to be rendered
        val message = "hello world"

        every { debugConsoleView.isDisplayed() } returns false
        every { debugConsoleView.display() } just Runs
        every { debugConsoleView.appendMessage("$message\n") } just Runs

        println(message)

        verify(exactly = 1, timeout = 50) { debugConsoleView.isDisplayed() }
        verify(exactly = 1) { debugConsoleView.display() }
        verify(exactly = 1) { debugConsoleView.appendMessage("$message\n") }
    }

    companion object {
        const val DEBUG_PROPERTY = "com.jagex.debug"
        const val DISABLE_PROPERTY = "com.open592.disableDebugConsole"
        const val LOG_TO_SYSTEM_STREAM_PROPERTY = "com.open592.debugConsoleLogToSystemStream"
    }
}
