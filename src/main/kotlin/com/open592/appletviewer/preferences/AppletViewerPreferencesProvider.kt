package com.open592.appletviewer.preferences

import java.nio.file.Path
import javax.inject.Provider

public class AppletViewerPreferencesProvider : Provider<AppletViewerPreferences> {
    override fun get(): AppletViewerPreferences {
        return AppletViewerPreferences(FILE_PATH)
    }

    private companion object {
        private val FILE_PATH = Path.of("jagexappletviewer.preferences")
    }
}
