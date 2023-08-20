package com.open592.appletviewer.paths

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okio.buffer
import okio.source
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApplicationPathsTest {
    @Test
    fun `Should return null when no game file path can be resolved`() {
        val config = mockk<ApplicationConfiguration>()
        val settingsStore = mockk<SystemPropertiesSettingsStore>()
        val fs = FileSystems.getDefault()

        every { settingsStore.getString("com.open592.launcherDirectoryOverride") } returns ""
        every { settingsStore.getString("user.home") } returns fs
            .getPath("C:\\Users\\test")
            .toAbsolutePath()
            .toString()
        every { settingsStore.getString("user.dir") } returns ""

        assertDoesNotThrow {
            val applicationPaths = WindowsApplicationPaths(config, fs, settingsStore)
            assertNull(applicationPaths.resolveGameFileDirectoryPath("nonexistent.ws"))
        }
    }

    @Test
    fun `Should properly resolve a path to the game directory`() {
        val fileName = "runescape.prm"
        val expectedText = "I exist in the game directory"

        useFileWithText(fileName, expectedText) { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settingsStore = mockk<SystemPropertiesSettingsStore>()

            every { settingsStore.getString("com.open592.launcherDirectoryOverride") } returns ""
            every { settingsStore.getString("user.home") } returns fs
                .getPath("C:\\Users\\test")
                .toAbsolutePath()
                .toString()
            every { settingsStore.getString("user.dir") } returns (
                fs.getPath(ApplicationPathsMocks.ROOT_DIR, "bin").toAbsolutePath().toString()
                )

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settingsStore)
                val path = applicationPaths.resolveGameFileDirectoryPath(fileName)

                assertNotNull(path)

                path.source().buffer().use {
                    assertEquals(expectedText, it.readUtf8())
                }
            }
        }
    }

    @Test
    fun `Should allow overriding the launcher directory when resolving a game file path`() {
        val fileName = "override-test.txt"
        val expectedText = "Happy, oh wait no, sad world!"

        useFileWithText(fileName, expectedText) { fs ->
            val config = mockk<ApplicationConfiguration>()
            val settingsStore = mockk<SystemPropertiesSettingsStore>()

            every {
                settingsStore.getString("com.open592.launcherDirectoryOverride")
            } returns fs.getPath(ApplicationPathsMocks.ROOT_DIR, "bin").toAbsolutePath().toString()
            every { settingsStore.getString("user.home") } returns fs
                .getPath("C:\\Users\\test")
                .toAbsolutePath()
                .toString()

            assertDoesNotThrow {
                val applicationPaths = WindowsApplicationPaths(config, fs, settingsStore)
                val path = applicationPaths.resolveGameFileDirectoryPath(fileName)

                assertNotNull(path)

                path.source().buffer().use {
                    assertEquals(expectedText, it.readUtf8())
                }

                verify(exactly = 0) { settingsStore.getString("user.dir") }
            }
        }
    }

    private fun useFileWithText(fileName: String, text: String, action: (fs: FileSystem) -> Unit) {
        ApplicationPathsMocks.createLauncherDirectoryStructure().use { fs ->
            val dir = fs.getPath(ApplicationPathsMocks.ROOT_DIR, Constants.GAME_NAME)
            val path = dir.resolve(fileName)

            Files.createFile(path)

            Files.writeString(path, text)

            action(fs)
        }
    }
}
