package com.open592.appletviewer.modal

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.config.javconfig.JavConfig
import com.open592.appletviewer.config.javconfig.ServerConfiguration
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.modal.event.ApplicationModalEventBus
import com.open592.appletviewer.modal.view.ApplicationModalView
import com.open592.appletviewer.viewer.event.ViewerEventBus
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationModalTest {
    @Test
    fun `Should properly handle a MESSAGE event type`() {
        val config = ApplicationConfiguration(SupportedLanguage.ENGLISH)
        val eventBus = ApplicationModalEventBus()
        val viewerEventBus = ViewerEventBus()
        val view = mockk<ApplicationModalView>()
        val modal = ApplicationModal(eventBus, config, view, viewerEventBus)

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

        modal.eventBus.dispatchDisplayEvent(ApplicationModalType.MESSAGE, expectedMessage)

        verify(exactly = 1, timeout = 50) {
            view.display(
                withArg {
                    assertEquals(it.type, ApplicationModalType.MESSAGE)
                    assertEquals(it.content.size, 1)
                    assertEquals(it.content.first(), expectedMessage)
                    assertEquals(it.title, expectedModalTitle)
                    assertEquals(it.buttonText, expectedButtonText)
                    assertEquals(it.closeAction, view::close)
                }
            )
        }
    }

    @Test
    fun `Should properly handle a FATAL_ERROR event type in a locale other than ENGLISH`() {
        val config = ApplicationConfiguration(SupportedLanguage.GERMAN)
        val eventBus = ApplicationModalEventBus()
        val viewerEventBus = ViewerEventBus()
        val view = mockk<ApplicationModalView>()
        val modal = ApplicationModal(eventBus, config, view, viewerEventBus)

        val expectedModalTitle = "Fehler"
        val expectedButtonText = "Beenden"

        val expectedMessage = "This is a serious error"

        justRun { view.display(any()) }

        modal.eventBus.dispatchDisplayEvent(ApplicationModalType.FATAL_ERROR, expectedMessage)

        verify(exactly = 1, timeout = 50) {
            view.display(
                withArg {
                    assertEquals(it.type, ApplicationModalType.FATAL_ERROR)
                    assertEquals(it.content.size, 1)
                    assertEquals(it.content.first(), expectedMessage)
                    assertEquals(it.title, expectedModalTitle)
                    assertEquals(it.buttonText, expectedButtonText)
                    assertEquals(it.closeAction, viewerEventBus::dispatchQuitEvent)
                }
            )
        }
    }

    @Test
    fun `Should properly handle a FATAL_ERROR event with a multi line message`() {
        val config = ApplicationConfiguration(SupportedLanguage.ENGLISH)
        val eventBus = ApplicationModalEventBus()
        val viewerEventBus = ViewerEventBus()
        val view = mockk<ApplicationModalView>()
        val modal = ApplicationModal(eventBus, config, view, viewerEventBus)

        val expectedModalTitle = "Error"
        val expectedButtonText = "Quit"

        val expectedModalContentStrings = listOf(
            "This is a really serious error.",
            "You should definitely fix it.",
            "You have until tomorrow."
        )
        val expectedMessage = expectedModalContentStrings.joinToString("\n")

        justRun { view.display(any()) }

        modal.eventBus.dispatchDisplayEvent(ApplicationModalType.FATAL_ERROR, expectedMessage)

        verify(exactly = 1, timeout = 50) {
            view.display(
                withArg {
                    assertEquals(it.type, ApplicationModalType.FATAL_ERROR)
                    assertEquals(it.content, expectedModalContentStrings)
                    assertEquals(it.title, expectedModalTitle)
                    assertEquals(it.buttonText, expectedButtonText)
                    assertEquals(it.closeAction, viewerEventBus::dispatchQuitEvent)
                }
            )
        }
    }
}
