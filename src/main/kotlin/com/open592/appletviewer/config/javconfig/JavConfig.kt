package com.open592.appletviewer.config.javconfig

import com.open592.appletviewer.config.language.SupportedLanguage
import okio.BufferedSource
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * JavConfig represents the bulk of the Applet Viewer's configuration.
 *
 * It is loaded at runtime and contains the following pieces of data:
 *
 * - Configuration options for the applet viewer itself
 *
 * - Localized content for the appletviewer
 *
 * - Parameters for the appletviewer as well as downstream components.
 *   (This is provided through the `getParameter` "applet" method)
 *
 * @param root          Root server configuration
 * @param overrides     Named server configurations (these override the root).
 *                      Insertion order is determined by order they appear within
 *                      the configuration file.
 * @param languageNames Map of language IDs to their localized name
 */
public data class JavConfig(
    public val root: ServerConfiguration,
    public val overrides: LinkedHashMap<String, ServerConfiguration>,
    public val languageNames: SortedMap<SupportedLanguage, String>
) {
    public companion object {
        public fun parse(config: String): JavConfig {
            val root = ServerConfiguration()
            val overrides = linkedMapOf<String, ServerConfiguration>()
            val languageNames = sortedMapOf<SupportedLanguage, String>()
            var currentServer = root

            config.lines().forEach { line ->
                when {
                    line.isEmpty() -> return@forEach
                    // Ignore comments (Multi-line comments are not supported)
                    line.startsWith("//") || line.startsWith("#") -> return@forEach
                    line.startsWith(SERVER_BLOCK_OPEN_TOKEN) -> {
                        val server = processServerBlock(line)

                        overrides[server.id] = server

                        currentServer = server
                    }
                    /**
                     * Process a content entry
                     *
                     * Content entries are in the following format:
                     *
                     * msg=key=value
                     */
                    line.startsWith(CONTENT_ENTRY_MAGIC_STRING) -> {
                        val (key, value) = getEntry(line, magicStringLength = CONTENT_ENTRY_MAGIC_STRING.length)

                        /*
                         * We may encounter entries with a key in the format:
                         *
                         * lang<ID> (lang0)
                         *
                         * When we see these values we must map the ID value to the locale preserving
                         * language name
                         *
                         * Example:
                         *
                         * msg=lang0=English
                         * msg=lang1=Deutsch
                         *
                         * We will use this to drive the locale selection dialog
                         */
                        if (key.length == 5 && key.startsWith("lang")) {
                            try {
                                val id = key.substring(4).toInt()

                                // Ignore unsupported languages
                                val language = SupportedLanguage.resolveFromLanguageId(id) ?: return@forEach

                                languageNames[language] = value

                                /**
                                 * NOTE: In the original applet viewer code the locale definitions were
                                 * added to the content map. This was done since locale definition creation
                                 * and configuration parsing was done in separate passes. We create the definitions
                                 * here, so we can short circuit.
                                 */
                                return@forEach
                            } catch (err: NumberFormatException) {
                                // Ignore
                                return@forEach
                            }
                        }

                        currentServer.setContent(key, value)
                    }
                    /**
                     * Process parameter entry
                     *
                     * Parameter entries are in the format:
                     *
                     * param=key=value
                     */
                    line.startsWith(PARAMETER_ENTRY_MAGIC_STRING) -> {
                        val (key, value) = getEntry(line, magicStringLength = PARAMETER_ENTRY_MAGIC_STRING.length)

                        currentServer.setParameter(key, value)
                    }
                    /**
                     * Process configuration entry
                     *
                     * Configuration entries are in the format:
                     *
                     * key=value
                     */
                    else -> {
                        val (key, value) = getEntry(line, magicStringLength = 0)

                        currentServer.setConfig(key, value)
                    }
                }
            }

            return JavConfig(root, overrides, languageNames)
        }

        /**
         * Process a server block declaration.
         *
         * These will be in the format:
         *
         * `[server_name]`
         *
         * And will declare a new server - which overrides any root level configurations.
         */
        private fun processServerBlock(line: String): ServerConfiguration {
            val blockEndPOS = line.lastIndexOf(SERVER_BLOCK_CLOSE_TOKEN)

            if (blockEndPOS < 0) {
                throw Exception("Encountered an invalid server block declaration: Missing servername")
            }

            val serverName = line.substring(1, line.lastIndexOf("]"))

            if (serverName.isEmpty()) {
                throw Exception("Encountered an invalid server block declaration: Empty servername")
            }

            return ServerConfiguration(serverName)
        }

        /**
         * Given the length of the magic string extract both the key and value
         *
         * Example input:
         * - <line>::msg=key=value , <magicStringLength>::4
         * - <line>::param=key=value , <magicStringLength>::5
         * - <line>::key=value , <magicStringLength>::0
         */
        private fun getEntry(line: String, magicStringLength: Int): Pair<String, String> {
            val valueStartPOS = line.indexOf("=", startIndex = magicStringLength)

            if (valueStartPOS < 0) {
                throw Exception("Encountered an invalid JavConfig entry: Missing key")
            }

            val key = line.substring(magicStringLength, valueStartPOS).trim().lowercase(Locale.getDefault())
            val value = line.substring(valueStartPOS + 1, line.length).trim()

            return Pair(key, value)
        }

        private const val SERVER_BLOCK_OPEN_TOKEN = "["
        private const val SERVER_BLOCK_CLOSE_TOKEN = "]"
        private const val CONTENT_ENTRY_MAGIC_STRING = "msg="
        private const val PARAMETER_ENTRY_MAGIC_STRING = "param="
    }
}
