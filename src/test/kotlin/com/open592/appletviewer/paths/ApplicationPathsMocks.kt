package com.open592.appletviewer.paths

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import java.nio.file.FileSystem
import java.nio.file.Files

object ApplicationPathsMocks {
    const val ROOT_DIR = "jagexlauncher"

    fun createLauncherDirectoryStructure(): FileSystem {
        val fs = MemoryFileSystemBuilder.newLinux().build()

        val binDirectory = fs.getPath(ROOT_DIR, "bin")
        val libDirectory = fs.getPath(ROOT_DIR, "lib")
        val gameDirectory = fs.getPath(ROOT_DIR, Constants.GAME_NAME)

        Files.createDirectories(binDirectory)
        Files.createDirectories(libDirectory)
        Files.createDirectories(gameDirectory)

        return fs
    }
}
