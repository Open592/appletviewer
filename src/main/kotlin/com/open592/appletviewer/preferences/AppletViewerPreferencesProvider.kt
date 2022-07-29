package com.open592.appletviewer.preferences

import java.nio.file.Path
import javax.inject.Provider

public class AppletViewerPreferencesProvider : Provider<AppletViewerPreferences> {
    override fun get(): AppletViewerPreferences {
        return AppletViewerPreferences(Path.of(AppletViewerPreferences.DEFAULT_FILE_NAME))
    }
}
