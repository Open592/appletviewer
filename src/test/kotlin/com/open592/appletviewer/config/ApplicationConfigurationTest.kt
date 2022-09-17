package com.open592.appletviewer.config

import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.config.resolver.ConfigurationResolver
import com.open592.appletviewer.http.HttpFetch
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationConfigurationTest {
    @Test
    fun `Should show fatal error modal when there is no configuration present`() {
        val preferences = mockk<AppletViewerPreferences>()
        val fetch = mockk<HttpFetch>()
        val settingsStore = mockk<SettingsStore>()
        val resolver = ConfigurationResolver(preferences, fetch, settingsStore)
        val applicationConfiguration = ApplicationConfiguration(resolver, SupportedLanguage.ENGLISH)

        every { settingsStore.getString("com.jagex.config") } returns ""
        every { settingsStore.getString("com.jagex.configfile") } returns ""

        assertEquals(applicationConfiguration.initialize(), ResolverStatus.ERROR_MISSING_CONFIG)

        verify(exactly = 1) { settingsStore.getString("com.jagex.config") }
        verify(exactly = 1) { settingsStore.getString("com.jagex.configfile") }
    }
}
