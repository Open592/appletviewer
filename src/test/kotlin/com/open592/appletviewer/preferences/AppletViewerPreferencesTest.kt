package com.open592.appletviewer.preferences

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class AppletViewerPreferencesTest {
    @Test
    fun `Should not throw an exception when the preferences file doesn't exist`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(PREFERENCES_FILE_NAME)

        assertDoesNotThrow {
            AppletViewerPreferences(filePath)
        }
    }

    @Test
    fun `Should properly write values to a previously empty file`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        val filePath = fs.getPath(PREFERENCES_FILE_NAME)
        val expectedData = Pair("Test", "Value")

        Files.createFile(filePath)

        val preferences = AppletViewerPreferences(filePath)

        assertDoesNotThrow {
            preferences.set(expectedData.first, expectedData.second)
        }

        assertEquals(expectedData.second, preferences.get(expectedData.first))
    }

    @Test
    fun `Should properly load data from an existing file and handle updating it`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        val filePath = fs.getPath(PREFERENCES_FILE_NAME)
        val initialData = Pair("Language", "0")
        val updatedData = Pair(initialData.first, "1")

        Files.createFile(filePath)
        Files.newBufferedWriter(filePath).use {
            it.write("${initialData.first}=${initialData.second}")
            it.newLine()
        }

        val preferences = AppletViewerPreferences(filePath)

        assertEquals(initialData.second, preferences.get(initialData.first))

        assertDoesNotThrow {
            preferences.set(updatedData.first, updatedData.second)
        }

        assertEquals(updatedData.second, preferences.get(updatedData.first))

        // Make sure the underlying filesystem was updated
        Files.newBufferedReader(filePath).useLines {
            it.forEach {
                assertEquals("${updatedData.first}=${updatedData.second}", it)
            }
        }
    }

    @Test
    fun `Should allow setting preferences without writing to the filesystem`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        val filePath = fs.getPath(PREFERENCES_FILE_NAME)
        // Our first set of values should not be written to the filesystem before the second set of values are set
        val firstExpectedValues = listOf(
            Pair("One", "1"),
            Pair("Two", "2"),
            Pair("Three", "3"),
            Pair("Four", "4"),
        )
        // We will write this value and trigger a flush to the filesystem
        val flushValue = Pair("Five", "5")

        // Create an empty file to start
        Files.createFile(filePath)

        val preferences = AppletViewerPreferences(filePath)

        assertDoesNotThrow {
            firstExpectedValues.forEach {
                preferences.set(it.first, it.second, shouldWrite = false)
            }
        }

        // Make sure the file hasn't been modified
        assertEquals(0, Files.newBufferedReader(filePath).lines().count())

        // Make sure the values are stored
        firstExpectedValues.forEach {
            assertEquals(it.second, preferences.get(it.first))
        }

        // Write the final value, and trigger the previous values
        // to be flushed to the preferences file
        assertDoesNotThrow {
            preferences.set(flushValue.first, flushValue.second)
        }

        Files.newBufferedReader(filePath).useLines {
            it.forEachIndexed { index, line ->
                // TODO: Ehh is this the most clean way of doing this?
                if (index >= firstExpectedValues.count()) {
                    assertEquals("${flushValue.first}=${flushValue.second}", line)

                    return
                }

                assertEquals("${firstExpectedValues[index].first}=${firstExpectedValues[index].second}", line)
            }
        }
    }

    private companion object {
        private const val PREFERENCES_FILE_NAME = "jagexappletviewer.preferences"
    }
}
