package com.open592.appletviewer.paths

import com.google.inject.AbstractModule
import com.open592.appletviewer.settings.SettingStoreModule
import java.nio.file.FileSystem
import java.nio.file.FileSystems

public object ApplicationPathsModule : AbstractModule() {
    override fun configure() {
        install(SettingStoreModule)

        bind(FileSystem::class.java).toInstance(FileSystems.getDefault())
    }
}
