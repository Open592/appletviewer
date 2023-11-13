package com.open592.appletviewer.modal

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.config.javconfig.JavConfig
import com.open592.appletviewer.config.javconfig.ServerConfiguration
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.events.GlobalEventBus
import com.open592.appletviewer.modal.view.ApplicationModalView
import com.open592.appletviewer.progress.ProgressEvent
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationModalTest {
    @Test
    fun `Should properly handle a MESSAGE event type`() =
        runTest {
            val config = ApplicationConfiguration(SupportedLanguage.ENGLISH)
            val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
            val view = mockk<ApplicationModalView>()
            val modal = ApplicationModal(config, eventBus, view)

            // For MESSAGE events we have to initialize the configuration with
            // a JavConfig instance that includes the needed content strings
            // since they are not packaged with the applet viewer.
            val expectedModalTitle = "Message"
            val expectedButtonText = "OK"

            val serverConfig = ServerConfiguration()

            serverConfig.setContent("message", expectedModalTitle)
            serverConfig.setContent("ok", expectedButtonText)

            val javConfig = JavConfig(root = serverConfig, overrides = linkedMapOf(), languageNames = sortedMapOf())

            config.initialize(javConfig)

            val expectedMessage = "Hello world"

            justRun { view.display(any()) }

            modal.displayMessageModal(expectedMessage)

            verify(exactly = 1, timeout = 50) {
                view.display(
                    withArg {
                        assertEquals(it.content.size, 1)
                        assertEquals(it.content.first(), expectedMessage)
                        assertEquals(it.title, expectedModalTitle)
                        assertEquals(it.buttonText, expectedButtonText)
                        assertEquals(it.closeAction, view::close)
                    },
                )
            }
        }

    @Test
    fun `Should properly handle a FATAL_ERROR event type in a locale other than ENGLISH`() =
        runTest {
            val config = ApplicationConfiguration(SupportedLanguage.GERMAN)
            val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
            val view = mockk<ApplicationModalView>()
            val modal = ApplicationModal(config, eventBus, view)

            val expectedModalTitle = "Fehler"
            val expectedButtonText = "Beenden"

            val expectedMessage = "This is a serious error"

            justRun { view.display(any()) }

            // A FATAL_ERROR should close the progress indicator
            var progressEvent: ProgressEvent.ChangeVisibility? = null

            eventBus.listen<ProgressEvent> {
                when (it) {
                    is ProgressEvent.ChangeVisibility -> progressEvent = it
                    else -> fail("Invalid ProgressEvent encountered")
                }
            }

            // Our fatal error models should never return. Due to mocking we fall through
            // and hit a `error()` call.
            assertThrows<IllegalStateException> { modal.displayFatalErrorModal(expectedMessage) }

            verify(exactly = 1, timeout = 50) {
                view.display(
                    withArg {
                        assertEquals(it.content.size, 1)
                        assertEquals(it.content.first(), expectedMessage)
                        assertEquals(it.title, expectedModalTitle)
                        assertEquals(it.buttonText, expectedButtonText)
                        assertEquals(it.closeAction, view::quit)
                    },
                )
            }

            assertEquals(false, progressEvent?.visible)
        }

    @Test
    fun `Should properly handle a FATAL_ERROR event with a multi line message`() =
        runTest {
            val config = ApplicationConfiguration(SupportedLanguage.ENGLISH)
            val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
            val view = mockk<ApplicationModalView>()
            val modal = ApplicationModal(config, eventBus, view)

            val expectedModalTitle = "Error"
            val expectedButtonText = "Quit"

            val expectedModalContentStrings =
                listOf(
                    "This is a really serious error.",
                    "You should definitely fix it.",
                    "You have until tomorrow.",
                )
            val expectedMessage = expectedModalContentStrings.joinToString("\\n")

            justRun { view.display(any()) }

            // A FATAL_ERROR should close the progress indicator
            var progressEvent: ProgressEvent.ChangeVisibility? = null

            eventBus.listen<ProgressEvent> {
                when (it) {
                    is ProgressEvent.ChangeVisibility -> progressEvent = it
                    else -> fail("Invalid ProgressEvent encountered")
                }
            }

            assertThrows<IllegalStateException> { modal.displayFatalErrorModal(expectedMessage) }

            verify(exactly = 1, timeout = 50) {
                view.display(
                    withArg {
                        assertEquals(it.content, expectedModalContentStrings)
                        assertEquals(it.title, expectedModalTitle)
                        assertEquals(it.buttonText, expectedButtonText)
                        assertEquals(it.closeAction, view::quit)
                    },
                )
            }

            assertEquals(false, progressEvent?.visible)
        }
}
