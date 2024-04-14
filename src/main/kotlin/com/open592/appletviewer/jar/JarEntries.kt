package com.open592.appletviewer.jar

import okio.Buffer

/**
 * A simple wrapper around jar entries, provide some helper
 * methods to extract their content.
 */
public data class JarEntries(public val entries: Map<String, Buffer>) {
    /**
     * Return the first entry with the provided file extension.
     *
     * @param extension The file extension - Example: "so"
     */
    public fun getEntryByFileExtension(extension: String): Buffer? {
        return entries.firstNotNullOfOrNull {
            it.takeIf { (key) -> key.endsWith(extension) }?.value
        }
    }

    public companion object {
        public fun emptyEntries(): JarEntries {
            return JarEntries(emptyMap())
        }
    }
}
