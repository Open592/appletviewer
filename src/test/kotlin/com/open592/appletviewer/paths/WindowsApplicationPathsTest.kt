package com.open592.appletviewer.paths

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsApplicationPathsTest {
    @Test
    fun `If we encounter an existing cache file we should return it's path`() {
        val expectedPaths = listOf(
            // No cachesubdir
            "C:\\rscache\\.jagex_cache_32" to "",
            // Present cachesubdir
            "C:\\.jagex_cache_32" to Constants.GAME_NAME,
            // Alternate cache directory name
            "C:\\Users\\test\\.file_store_32" to Constants.GAME_NAME,
            // Working directory
            "C:\\work\\.file_store_32" to Constants.GAME_NAME
        )

        expectedPaths.forEach { (parentDirectory, cacheSubDirectory) ->
            MemoryFileSystemBuilder
                .newWindows()
                .addUser("test")
                .setCurrentWorkingDirectory("C:\\work")
                .build()
                .use { fs ->
                    val config = mockk<ApplicationConfiguration>()
                    val settings = mockk<SystemPropertiesSettingsStore>()
                    val applicationPaths = WindowsApplicationPaths(config, fs, settings)

                    // Create file
                    val filename = "browsercontrol.dll"
                    val cacheDirectory = if (cacheSubDirectory.isNotEmpty()) {
                        fs.getPath(parentDirectory, cacheSubDirectory)
                    } else {
                        fs.getPath(parentDirectory)
                    }.createDirectories()
                    val expectedCacheFilePath = cacheDirectory.resolve(filename).createFile()

                    every { settings.getString("user.home") } returns fs
                        .getPath("C:\\Users\\test")
                        .toAbsolutePath()
                        .toString()
                    every { config.getConfig("cachesubdir") } returns cacheSubDirectory
                    every { config.getConfigAsInt("modewhat") } returns 0

                    assertDoesNotThrow {
                        val path = applicationPaths.resolveCacheFilePath(filename)

                        assertEquals(expectedCacheFilePath, path)
                    }

                    verify(exactly = 1) { settings.getString("user.home") }
                    verify { config.getConfig("cachesubdir") }
                    verify { config.getConfigAsInt("modewhat") }
                }
        }
    }
}
