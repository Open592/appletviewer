package com.open592.appletviewer.fetch

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AssetFetchTest {
    private val timeout = Duration.ofMinutes(1)
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeout)
        .writeTimeout(timeout)
        .readTimeout(timeout)
        .followRedirects(true)
        .build()

    @Test
    fun `Should successfully resolve a local file`() {
        val fileName = "happy-path.txt"
        val expectedText = "Happy path test."

        useFileWithText(fileName, expectedText) { fs ->
            val settingsStore = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, fs, settingsStore)

            every { settingsStore.getString("com.open592.launcherDirectoryOverride") } returns ""
            every { settingsStore.getString("user.dir") } returns fs.getPath(ROOT).toAbsolutePath().toString()

            assertDoesNotThrow {
                val result = fetch.fetchLocalGameFile(fileName)

                assertEquals(expectedText, result)
            }
        }
    }

    @Test
    fun `Should gracefully handle attempting to resolve local a file which doesn't exist`() {
        val settingsStore = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settingsStore)

        every { settingsStore.getString("com.open592.launcherDirectoryOverride") } returns ""
        every { settingsStore.getString("user.dir") } returns "not-a-dir"

        assertDoesNotThrow {
            val result = fetch.fetchLocalGameFile("i-dont-exist.txt")

            assertNull(result)
        }
    }

    @Test
    fun `Should allow overriding the launcher directory when resolving a local file`() {
        val fileName = "override-test.txt"
        val expectedText = "Happy, oh wait no, sad world!"

        useFileWithText(fileName, expectedText) { fs ->
            val settingsStore = mockk<SystemPropertiesSettingsStore>()
            val fetch = AssetFetch(client, fs, settingsStore)

            every {
                settingsStore.getString("com.open592.launcherDirectoryOverride")
            } returns fs.getPath(ROOT).toAbsolutePath().toString()

            assertDoesNotThrow {
                val result = fetch.fetchLocalGameFile(fileName)

                assertEquals(expectedText, result)

                verify(exactly = 0) { settingsStore.getString("user.dir") }
            }
        }
    }

    @Test
    fun `Should successfully resolve a remote file`() {
        val expectedResult = "Hello world!"
        val server = MockWebServer()

        server.enqueue(MockResponse().setBody(expectedResult).setResponseCode(200))

        server.start()

        val settingsStore = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settingsStore)
        val url = server.url("/happy-path").toString()

        assertDoesNotThrow {
            val result = fetch.fetchRemoteFile(url)

            assertEquals(expectedResult, result)
        }
    }

    @Test
    fun `Should gracefully handle attempting to resolve a remote file which doesn't exist`() {
        val server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(404))

        server.start()

        val settingsStore = mockk<SystemPropertiesSettingsStore>()
        val fetch = AssetFetch(client, FileSystems.getDefault(), settingsStore)
        val url = server.url("/nothing-to-see-here").toString()

        assertDoesNotThrow {
            assertNull(fetch.fetchRemoteFile(url))
        }
    }

    private fun useFileWithText(fileName: String, text: String, action: (fs: FileSystem) -> Unit) {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        val dir = fs.getPath(ROOT, Constants.GAME_NAME)

        Files.createDirectories(dir)

        val path = dir.resolve(fileName)

        Files.createFile(path)

        Files.writeString(path, text)

        action(fs)

        fs.close()
    }

    private companion object {
        private const val ROOT = "launcher"
    }
}
