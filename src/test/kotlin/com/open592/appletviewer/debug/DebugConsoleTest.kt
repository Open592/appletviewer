package com.open592.appletviewer.debug

import com.open592.appletviewer.debug.capture.OutputCapture
import com.open592.appletviewer.debug.event.DebugConsoleEventBus
import com.open592.appletviewer.debug.view.DebugConsoleView
import com.open592.appletviewer.settings.SettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class DebugConsoleTest {
    @Test
    fun `Should not capture messages when debug mode is not true`() {
        val eventBus = DebugConsoleEventBus()
        val debugConsoleView = mockk<DebugConsoleView>()
        // Mocking this purely to make sure we aren't invoking it
        val outputCapture = mockk<OutputCapture>()
        val settings = mockk<SettingsStore>()

        // Mock that we aren't running in debug mode
        every { settings.getBoolean(DEBUG_PROPERTY) } returns false

        val debugConsole = DebugConsole(eventBus, debugConsoleView, outputCapture, settings)

        debugConsole.initialize()

        // Make sure we only checked for if we are running in debug mode and then short-circuited
        verify(exactly = 1) { settings.getBoolean(DEBUG_PROPERTY) }
        verify(exactly = 0) { settings.getBoolean(LOG_TO_SYSTEM_STREAM_PROPERTY) }

        // Verify we are not invoking the outputCapture
        verify(exactly = 0) {
            outputCapture.capture(false)
        }
    }

    companion object {
        const val DEBUG_PROPERTY = "com.jagex.debug"
        const val LOG_TO_SYSTEM_STREAM_PROPERTY = "com.open592.shouldLogToSystemStream"
    }
}
