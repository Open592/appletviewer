package com.open592.appletviewer.config.language

import com.open592.appletviewer.preferences.AppletViewerPreferences
import jakarta.inject.Inject
import jakarta.inject.Provider

public class SupportedLanguageProvider
@Inject
constructor(
    private val appletViewerPreferences: AppletViewerPreferences,
) : Provider<SupportedLanguage> {
    public override fun get(): SupportedLanguage {
        return SupportedLanguage.resolve(appletViewerPreferences)
    }
}
