package com.open592.appletviewer.config

import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.localization.SupportedLanguage
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.modal.ApplicationModalType
import com.open592.appletviewer.modal.event.ApplicationModalEventBus
import com.open592.appletviewer.modal.view.ApplicationModalView
import com.open592.appletviewer.settings.SettingsStore
import com.open592.appletviewer.viewer.event.ViewerEventBus
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationConfigurationTest {
    @Test
    fun `Should show fatal error modal when there is no configuration present`() {
        val modalEventBus = ApplicationModalEventBus()
        val localization = Localization(SupportedLanguage.ENGLISH)
        val modalView = mockk<ApplicationModalView>()
        val viewerEventBus = mockk<ViewerEventBus>()
        val applicationModal = ApplicationModal(modalEventBus, localization, modalView, viewerEventBus)
        val settingsStore = mockk<SettingsStore>()
        val applicationConfiguration = ApplicationConfiguration(applicationModal, localization, settingsStore)

        every { settingsStore.getString("com.jagex.config") } returns ""
        every { settingsStore.getString("com.jagex.configfile") } returns ""

        justRun { modalView.display(any()) }

        applicationConfiguration.initialize()

        verify(exactly = 1) { settingsStore.getString("com.jagex.config") }
        verify(exactly = 1) { settingsStore.getString("com.jagex.configfile") }
        verify(exactly = 1, timeout = 50) {
            modalView.display(
                withArg {
                    assertEquals(it.type, ApplicationModalType.FATAL_ERROR)
                    assertEquals(it.title, "Error")
                    assertEquals(it.buttonText, "Quit")
                    assertEquals(it.content.size, 1)
                    assertEquals(it.content.first(), localization.getContent("err_missing_config"))
                    assertEquals(it.closeAction, viewerEventBus::dispatchQuitEvent)
                }
            )
        }
    }
}
