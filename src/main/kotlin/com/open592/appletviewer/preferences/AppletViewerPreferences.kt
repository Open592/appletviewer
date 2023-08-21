package com.open592.appletviewer.preferences

import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds persistent data across AppletViewer sessions.
 *
 * Examples of data stored:
 *
 * - Language
 * - Membership status
 *
 * Unlike the original AppletViewer we only expose two public methods from this class:
 *
 * - get
 * - set
 *
 * And we initialize the class by reading the current values of the file if it exists. In
 * the case it doesn't exist we initialize the internal state to an empty map, and it stays
 * that way until the first write.
*/
@Singleton
public class AppletViewerPreferences @Inject constructor(
    private val fileSystem: FileSystem
) {
    private val preferences: MutableMap<String, String> = mutableMapOf()

    init {
        try {
            readFile()
        } catch (_: IOException) {
            // Ignored
        }
    }

    /**
     * Gets a preference value
     */
    public fun get(key: String): String {
        return preferences[key].orEmpty()
    }

    /**
     * Sets a preference value
     *
     * This function will automatically write the newly added value to the underlying
     * preferences file on the filesystem
     */
    public fun set(key: String, value: String) {
        set(key, value, shouldWrite = true)
    }

    /**
     * Sets a preference value and allows for controlling whether the newly added value
     * is added to the preferences file on the filesystem
     */
    public fun set(key: String, value: String, shouldWrite: Boolean) {
        preferences[key] = value

        if (shouldWrite) {
            writeFile()
        }
    }

    /**
     * Responsible for parsing the applet viewer preferences file and parsing it
     * into a map of <string,string> values.
     *
     * The format of the preferences file is as follows:
     *
     * ```txt
     * key=value
     * ```
     */
    private fun readFile() {
        Files.newBufferedReader(fileSystem.getPath(PREFERENCES_FILE_NAME)).forEachLine { line ->
            val (key, value) = line
                .split("=", limit = 2)
                .takeIf { it.size == 2 } ?: return@forEachLine

            set(key, value, shouldWrite = false)
        }
    }

    /**
     * Writes the provided preferences values to the
     */
    private fun writeFile() {
        Files.newBufferedWriter(fileSystem.getPath(PREFERENCES_FILE_NAME)).use { file ->
            preferences.forEach { (key, value) ->
                file.write("$key=$value")
                file.newLine()
            }
        }
    }

    private companion object {
        private const val PREFERENCES_FILE_NAME: String = "jagexappletviewer.preferences"
    }
}
