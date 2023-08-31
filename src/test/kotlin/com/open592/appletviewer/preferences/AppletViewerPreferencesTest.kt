package com.open592.appletviewer.preferences

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class AppletViewerPreferencesTest {
    @Test
    fun `Should not throw an exception when the preferences file doesn't exist`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            assertDoesNotThrow {
                AppletViewerPreferences(fs)
            }
        }
    }

    @Test
    fun `Should properly write values to a previously empty file`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferencesFilePath = fs.getPath(PREFERENCES_FILE_NAME)
            Files.createFile(preferencesFilePath)

            val (expectedKey, expectedValue) = Pair("Test", "Value")

            assertDoesNotThrow {
                val preferences = AppletViewerPreferences(fs)

                preferences.set(expectedKey, expectedValue)

                assertEquals(expectedValue, preferences.get(expectedKey))
            }
        }
    }

    @Test
    fun `Should properly load data from an existing file and handle updating it`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val filePath = fs.getPath(PREFERENCES_FILE_NAME)
            val (initialKey, initialValue) = Pair("Language", "0")
            val (updatedKey, updatedValue) = Pair(initialKey, "1")

            Files.createFile(filePath)
            Files.newBufferedWriter(filePath).use {
                it.write("$initialKey=$initialValue")
                it.newLine()
            }

            assertDoesNotThrow {
                val preferences = AppletViewerPreferences(fs)

                assertEquals(initialValue, preferences.get(initialKey))

                preferences.set(updatedKey, updatedValue)

                assertEquals(updatedValue, preferences.get(updatedKey))
            }

            // Make sure the underlying filesystem was updated
            Files.newBufferedReader(filePath).useLines {
                it.forEach {
                    assertEquals("$updatedKey=$updatedValue", it)
                }
            }
        }
    }

    @Test
    fun `Should allow setting preferences without writing to the filesystem`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val filePath = fs.getPath(PREFERENCES_FILE_NAME)
            // Our first set of values should not be written to the filesystem before the second set of values are set
            val firstExpectedValues =
                listOf(
                    Pair("One", "1"),
                    Pair("Two", "2"),
                    Pair("Three", "3"),
                    Pair("Four", "4"),
                )
            // We will write this value and trigger a flush to the filesystem
            val (flushKey, flushValue) = Pair("Five", "5")

            // Create an empty file to start
            Files.createFile(filePath)

            assertDoesNotThrow {
                val preferences = AppletViewerPreferences(fs)

                firstExpectedValues.forEach { (key, value) ->
                    preferences.set(key, value, shouldWrite = false)
                }

                // Make sure the file hasn't been modified
                assertEquals(0, Files.newBufferedReader(filePath).lines().count())

                // Make sure the values are stored
                firstExpectedValues.forEach { (key, value) ->
                    assertEquals(value, preferences.get(key))
                }

                // Write the final value, and trigger the previous values
                // to be flushed to the preferences file
                preferences.set(flushKey, flushValue)
            }

            Files.newBufferedReader(filePath).useLines {
                it.forEachIndexed { index, line ->
                    // TODO: Ehh is this the most clean way of doing this?
                    if (index >= firstExpectedValues.count()) {
                        assertEquals("$flushKey=$flushValue", line)

                        return
                    }

                    assertEquals("${firstExpectedValues[index].first}=${firstExpectedValues[index].second}", line)
                }
            }
        }
    }

    private companion object {
        const val PREFERENCES_FILE_NAME = "jagexappletviewer.preferences"
    }
}
