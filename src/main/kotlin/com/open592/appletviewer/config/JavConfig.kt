package com.open592.appletviewer.config

import java.io.BufferedReader
import java.util.Locale
import java.util.SortedMap
import kotlin.Exception
import kotlin.collections.LinkedHashMap

public data class JavConfig constructor(
    public val root: ServerSettings,
    public val overrides: LinkedHashMap<String, ServerSettings>,
    public val languageNames: SortedMap<Int, String>
) {
    public companion object {
        public fun parse(reader: BufferedReader): JavConfig {
            val root = ServerSettings()
            val overrides = linkedMapOf<String, ServerSettings>()
            val languageNames = sortedMapOf<Int, String>()
            var currentServer = root

            reader.lineSequence().map(String::trim).forEach { line ->
                when {
                    line.isEmpty() -> return@forEach
                    line.startsWith("//") || line.startsWith("#") -> return@forEach
                    line.startsWith(SERVER_BLOCK_OPEN_TOKEN) -> {
                        val server = processServerBlock(line)

                        overrides[server.id] = server

                        currentServer = server
                    }
                    line.startsWith(CONTENT_ENTRY_MAGIC_STRING) -> {
                        val (key, value) = processContentEntry(line)

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
                            val id = key.substring(4).toInt()

                            languageNames[id] = value

                            /**
                             * NOTE: In the original applet viewer code the locale definitions were
                             * added to the content map. This was done since locale definition creation
                             * and configuration parsing was done in separate passes. We perform create
                             * the definitions here, so we can short circuit
                             */
                            return@forEach
                        }

                        currentServer.setContentValue(key, value)
                    }
                    line.startsWith(PARAMETER_ENTRY_MAGIC_STRING) -> {
                        val (key, value) = processParameterEntry(line)

                        currentServer.setParameterValue(key, value)
                    }
                    else -> {
                        val (key, value) = processConfigEntry(line)

                        currentServer.setConfigValue(key, value)
                    }
                }
            }

            reader.close()

            return JavConfig(root, overrides, languageNames)
        }

        /**
         * Process a server block declaration.
         *
         * These will be in the format:
         *
         * `[server_name]`
         *
         * And will declare a new "Server" - which overrides any root level
         * values.
         *
         * Upon the first server block encountered we must set the active server key. If the
         * user wishes to switch to a different server, they must utilize the server selection
         * dialog within the toolbar.
         */
        private fun processServerBlock(line: String): ServerSettings {
            val blockEndPOS = line.lastIndexOf(SERVER_BLOCK_CLOSE_TOKEN)

            if (blockEndPOS < 0) {
                throw Exception("Encountered an invalid server block declaration: Missing servername")
            }

            val serverName = line.substring(1, line.lastIndexOf("]"))

            if (serverName.isEmpty()) {
                throw Exception("Encountered an invalid server block declaration: Empty servername")
            }

            return ServerSettings(serverName)
        }

        /**
         * Process a content entry
         *
         * Content entries are in the following format:
         *
         * msg=key=value
         */
        private fun processContentEntry(line: String): Pair<String, String> {
            return getEntry(line, magicStringLength = CONTENT_ENTRY_MAGIC_STRING.length)
        }

        /**
         * Process parameter entry
         *
         * Parameter entries are in the format:
         *
         * param=key=value
         */
        private fun processParameterEntry(line: String): Pair<String, String> {
            return getEntry(line, magicStringLength = PARAMETER_ENTRY_MAGIC_STRING.length)
        }

        /**
         * Process configuration entry
         *
         * Configuration entries are in the format:
         *
         * key=value
         */
        private fun processConfigEntry(line: String): Pair<String, String> {
            return getEntry(line, magicStringLength = 0)
        }

        /**
         * Given the length of the magic string extract both the key and value
         *
         * Example input:
         * - msg=key=value , 4
         * - param=key=value , 5
         * - key=value , 0
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
