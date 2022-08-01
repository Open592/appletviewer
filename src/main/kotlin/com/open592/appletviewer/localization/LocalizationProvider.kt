package com.open592.appletviewer.localization

import com.open592.appletviewer.preferences.AppletViewerPreferences
import javax.inject.Inject
import javax.inject.Provider

public class LocalizationProvider @Inject constructor(
    private val preferences: AppletViewerPreferences
) : Provider<Localization> {
    override fun get(): Localization {
        val language = SupportedLanguage.resolve(preferences)

        return Localization(language)
    }
}
