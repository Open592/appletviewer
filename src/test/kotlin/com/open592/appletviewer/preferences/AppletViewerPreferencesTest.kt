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
            // This should return a path which doesn't resolve to a file
            val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)

            assertDoesNotThrow {
                AppletViewerPreferences(filePath)
            }
        }
    }

    @Test
    fun `Should properly write values to a previously empty file`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
            val (expectedKey, expectedValue) = Pair("Test", "Value")

            Files.createFile(filePath)

            val preferences = AppletViewerPreferences(filePath)

            assertDoesNotThrow {
                preferences.set(expectedKey, expectedValue)
            }

            assertEquals(expectedValue, preferences.get(expectedKey))
        }
    }

    @Test
    fun `Should properly load data from an existing file and handle updating it`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
            val (initialKey, initialValue) = Pair("Language", "0")
            val (updatedKey, updatedValue) = Pair(initialKey, "1")

            Files.createFile(filePath)
            Files.newBufferedWriter(filePath).use {
                it.write("$initialKey=$initialValue")
                it.newLine()
            }

            val preferences = AppletViewerPreferences(filePath)

            assertEquals(initialValue, preferences.get(initialKey))

            assertDoesNotThrow {
                preferences.set(updatedKey, updatedValue)
            }

            assertEquals(updatedValue, preferences.get(updatedKey))

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
            val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
            // Our first set of values should not be written to the filesystem before the second set of values are set
            val firstExpectedValues = listOf(
                Pair("One", "1"),
                Pair("Two", "2"),
                Pair("Three", "3"),
                Pair("Four", "4")
            )
            // We will write this value and trigger a flush to the filesystem
            val (flushKey, flushValue) = Pair("Five", "5")

            // Create an empty file to start
            Files.createFile(filePath)

            val preferences = AppletViewerPreferences(filePath)

            assertDoesNotThrow {
                firstExpectedValues.forEach { (key, value) ->
                    preferences.set(key, value, shouldWrite = false)
                }
            }

            // Make sure the file hasn't been modified
            assertEquals(0, Files.newBufferedReader(filePath).lines().count())

            // Make sure the values are stored
            firstExpectedValues.forEach { (key, value) ->
                assertEquals(value, preferences.get(key))
            }

            // Write the final value, and trigger the previous values
            // to be flushed to the preferences file
            assertDoesNotThrow {
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
}
