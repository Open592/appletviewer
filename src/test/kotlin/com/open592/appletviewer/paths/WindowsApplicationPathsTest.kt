package com.open592.appletviewer.paths

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okio.Buffer
import okio.buffer
import okio.source
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.attribute.DosFileAttributeView
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WindowsApplicationPathsTest {
    @Test
    fun `If we encounter an existing cache file we should return it's path`() {
        val expectedPaths =
            listOf(
                // No cachesubdir
                "C:\\rscache\\.jagex_cache_32" to "",
                // Present cachesubdir
                "C:\\.jagex_cache_32" to Constants.GAME_NAME,
                // Alternate cache directory name
                "C:\\Users\\test\\.file_store_32" to Constants.GAME_NAME,
                // Working directory
                "C:\\Users\\test\\AppData\\Local\\jagexlauncher\\.file_store_32" to Constants.GAME_NAME,
            )

        expectedPaths.forEach { (parentDirectory, cacheSubDirectory) ->
            useFilesystem { fs ->
                val config = mockk<ApplicationConfiguration>()
                val settings = mockk<SystemPropertiesSettingsStore>()

                // Create file
                val filename = "browsercontrol.dll"
                val cacheDirectory =
                    if (cacheSubDirectory.isNotEmpty()) {
                        fs.getPath(parentDirectory, cacheSubDirectory)
                    } else {
                        fs.getPath(parentDirectory)
                    }.createDirectories()
                val expectedCacheFilePath = cacheDirectory.resolve(filename).createFile()

                every { settings.getString("user.home") } returns
                    fs
                        .getPath("C:\\Users\\test")
                        .toAbsolutePath()
                        .toString()
                every { config.getConfig("cachesubdir") } returns cacheSubDirectory
                every { config.getConfigAsInt("modewhat") } returns 0

                assertDoesNotThrow {
                    val applicationPaths = WindowsApplicationPaths(config, fs, settings)
                    val path = applicationPaths.resolveCacheDirectoryPath(filename)

                    assertEquals(expectedCacheFilePath, path)
                    assertTrue(path.exists())
                }

                verify(exactly = 1) { settings.getString("user.home") }
                verify { config.getConfig("cachesubdir") }
                verify { config.getConfigAsInt("modewhat") }
            }
        }
    }

    @Test
    fun `When an existing file is found, but it is not writeable, it's path should not be returned`() {
        useFilesystem { fs ->
            val cacheSubDirectory = Constants.GAME_NAME
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            every { settings.getString("user.home") } returns
                fs
                    .getPath("C:\\Users\\test")
                    .toAbsolutePath()
                    .toString()
            every { config.getConfig("cachesubdir") } returns cacheSubDirectory
            every { config.getConfigAsInt("modewhat") } returns 0

            val unWriteableCacheDirectoryPath =
                fs
                    .getPath("C:\\Users\\test\\.jagex_cache_32\\$cacheSubDirectory")
                    .createDirectories()
            val unWriteableCacheFilePath = unWriteableCacheDirectoryPath.resolve(
                BROWSERCONTROL_LIBRARY_NAME,
            ).createFile()

            val unWriteableCacheFilePathAttributes =
                Files.getFileAttributeView(
                    unWriteableCacheFilePath,
                    DosFileAttributeView::class.java,
                )
            unWriteableCacheFilePathAttributes.setReadOnly(true)

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settings)
                val expectedPath = fs.getPath(
                    "C:\\rscache\\.jagex_cache_32\\$cacheSubDirectory\\$BROWSERCONTROL_LIBRARY_NAME",
                )
                val path = applicationPaths.resolveCacheDirectoryPath(BROWSERCONTROL_LIBRARY_NAME)

                assertEquals(expectedPath, path)
                assertTrue(path.exists())
            }

            verify { settings.getString("user.home") }
            verify { config.getConfig("cachesubdir") }
            verify { config.getConfigAsInt("modewhat") }
        }
    }

    @Test
    fun `When no existing files are present we should use the first cache path available`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            val cacheSubDirectory = "runescape"

            every { settings.getString("user.home") } returns
                fs
                    .getPath("C:\\Users\\test")
                    .toAbsolutePath()
                    .toString()
            every { config.getConfig("cachesubdir") } returns cacheSubDirectory
            every { config.getConfigAsInt("modewhat") } returns 0

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settings)
                val path = applicationPaths.resolveCacheDirectoryPath(BROWSERCONTROL_LIBRARY_NAME)
                val expectedPath = fs.getPath(
                    "C:\\rscache\\.jagex_cache_32\\$cacheSubDirectory\\$BROWSERCONTROL_LIBRARY_NAME",
                )

                assertEquals(expectedPath, path)
                assertTrue(path.exists())
            }

            verify { settings.getString("user.home") }
            verify { config.getConfig("cachesubdir") }
            verify { config.getConfigAsInt("modewhat") }
        }
    }

    @Test
    fun `It should respect the modewhat config when resolving cache directory`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            val cacheSubDirectory = "runescape"

            // Create the desired cache file, but in the wrong directory.
            // `.jagex_cache_32` would be used if our modewhat was `0` but
            // in this test case we set it to 1
            val wrongCacheDirectoryPath =
                fs
                    .getPath("C:\\Users\\test\\.jagex_cache_32\\$cacheSubDirectory")
                    .createDirectories()
            wrongCacheDirectoryPath.resolve(BROWSERCONTROL_LIBRARY_NAME).createFile()

            every { settings.getString("user.home") } returns
                fs
                    .getPath("C:\\Users\\test")
                    .toAbsolutePath()
                    .toString()
            every { config.getConfig("cachesubdir") } returns cacheSubDirectory
            every { config.getConfigAsInt("modewhat") } returns 1

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settings)
                val path = applicationPaths.resolveCacheDirectoryPath(BROWSERCONTROL_LIBRARY_NAME)
                val expectedPath = fs.getPath(
                    "C:\\rscache\\.jagex_cache_33\\$cacheSubDirectory\\$BROWSERCONTROL_LIBRARY_NAME",
                )

                assertEquals(expectedPath, path)
                assertTrue(path.exists())
            }

            verify { settings.getString("user.home") }
            verify { config.getConfig("cachesubdir") }
            verify { config.getConfigAsInt("modewhat") }
        }
    }

    @Test
    fun `Should properly save a file to the resolved cache path`() {
        useFilesystem { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settings = mockk<SystemPropertiesSettingsStore>()

            val cacheSubDirectory = "runescape"

            every { settings.getString("user.home") } returns
                fs
                    .getPath("C:\\Users\\test")
                    .toAbsolutePath()
                    .toString()
            every { config.getConfig("cachesubdir") } returns cacheSubDirectory
            every { config.getConfigAsInt("modewhat") } returns 0

            assertDoesNotThrow {
                val expectedContent = "open592"
                val applicationPaths = WindowsApplicationPaths(config, fs, settings)

                val sink = Buffer().writeString(expectedContent, Charset.defaultCharset())

                applicationPaths.saveCacheFile(BROWSERCONTROL_LIBRARY_NAME, sink)

                val path = fs.getPath("C:\\rscache\\.jagex_cache_32\\$cacheSubDirectory\\$BROWSERCONTROL_LIBRARY_NAME")

                path.source().buffer().use {
                    assertEquals(expectedContent, it.readString(Charset.defaultCharset()))
                }
            }

            verify { settings.getString("user.home") }
            verify { config.getConfig("cachesubdir") }
            verify { config.getConfigAsInt("modewhat") }
        }
    }

    private fun useFilesystem(action: (fs: FileSystem) -> Unit) {
        MemoryFileSystemBuilder
            .newWindows()
            .addUser(USERNAME)
            .setCurrentWorkingDirectory("C:\\Users\\$USERNAME\\AppData\\Local\\jagexlauncher")
            .build()
            .use(action)
    }

    private companion object {
        private const val USERNAME = "test"
        private const val BROWSERCONTROL_LIBRARY_NAME = "browsercontrol.dll"
    }
}
