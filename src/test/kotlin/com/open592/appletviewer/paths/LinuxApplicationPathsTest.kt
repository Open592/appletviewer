package com.open592.appletviewer.paths

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxApplicationPathsTest {
    @Test
    fun `Should return the correct cache path when cachesubdir is empty`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            every { config.getConfig("cachesubdir") } returns ""
            every { config.getConfigAsInt("modewhat") } returns 0
            every { settings.getString("user.home") } returns fs.getPath("/home/$USERNAME").toAbsolutePath().toString()

            val expectedPath = fs.getPath("/home/$USERNAME/.cache/.jagex_cache_32").toAbsolutePath()

            assertDoesNotThrow {
                val paths = LinuxApplicationPaths(config, fs, settings)

                val cachePath = paths.resolveCacheDirectoryPath("browsercontrol.so")

                assertEquals(cachePath, expectedPath)
            }

            verify(exactly = 1) { config.getConfig("cachesubdir") }
            verify(exactly = 1) { config.getConfigAsInt("modewhat") }
            verify(exactly = 1) { settings.getString("user.home") }
        }
    }

    @Test
    fun `Should return the correct cache path when we cannot resolve modewhat from the config`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val cacheSubDirectoryName = Constants.GAME_NAME

            every { config.getConfig("cachesubdir") } returns cacheSubDirectoryName
            every { config.getConfigAsInt("modewhat") } returns null
            every { settings.getString("user.home") } returns fs.getPath("/home/$USERNAME").toAbsolutePath().toString()

            val expectedPath =
                fs.getPath(
                    "/home/$USERNAME/.cache/.jagex_cache_32/$cacheSubDirectoryName",
                ).toAbsolutePath()

            assertDoesNotThrow {
                val paths = LinuxApplicationPaths(config, fs, settings)

                val cacheDirectory = paths.resolveCacheDirectoryPath("browsercontrol.so")

                assertEquals(expectedPath, cacheDirectory)
            }

            verify(exactly = 1) { config.getConfig("cachesubdir") }
            verify(exactly = 1) { config.getConfigAsInt("modewhat") }
            verify(exactly = 1) { settings.getString("user.home") }
        }
    }

    @Test
    fun `Should return the correct cache directory when the modewhat is non-zero`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()
            val cacheSubDirectoryName = Constants.GAME_NAME

            every { config.getConfig("cachesubdir") } returns cacheSubDirectoryName
            every { config.getConfigAsInt("modewhat") } returns 1
            every { settings.getString("user.home") } returns fs.getPath("/home/$USERNAME").toAbsolutePath().toString()

            val expectedPath =
                fs.getPath(
                    "/home/$USERNAME/.cache/.jagex_cache_33/$cacheSubDirectoryName",
                ).toAbsolutePath()

            assertDoesNotThrow {
                val paths = LinuxApplicationPaths(config, fs, settings)

                val cacheDirectory = paths.resolveCacheDirectoryPath("browsercontrol.so")

                assertEquals(expectedPath, cacheDirectory)
            }

            verify(exactly = 1) { config.getConfig("cachesubdir") }
            verify(exactly = 1) { config.getConfigAsInt("modewhat") }
            verify(exactly = 1) { settings.getString("user.home") }
        }
    }

    private fun useFilesystem(action: (fs: FileSystem) -> Unit) {
        MemoryFileSystemBuilder.newLinux().addUser(USERNAME).build().use(action)
    }

    companion object {
        private const val USERNAME = "test"
    }
}
