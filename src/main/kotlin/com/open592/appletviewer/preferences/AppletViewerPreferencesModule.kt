package com.open592.appletviewer.preferences

import com.google.inject.AbstractModule
import java.nio.file.FileSystem
import java.nio.file.FileSystems

public object AppletViewerPreferencesModule : AbstractModule() {
    override fun configure() {
        bind(FileSystem::class.java).toInstance(FileSystems.getDefault())
    }
}
