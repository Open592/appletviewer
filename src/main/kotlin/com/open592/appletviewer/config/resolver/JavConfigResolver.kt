package com.open592.appletviewer.config.resolver

import com.open592.appletviewer.config.javconfig.JavConfig
import com.open592.appletviewer.paths.ApplicationPaths
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SettingsStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.source
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.isRegularFile

/**
 * Configuration can come from one of two places depending on which
 * system properties are set.
 *
 * If we are provided with a remote URL we will always default to this
 * method of resolving the configuration.
 *
 * If we are missing the remote URL, but are provided with a filename
 * we will search within the game directory for the referenced file.
 *
 * If neither are found we are in an invalid state and inform the caller.
 */
@Singleton
public class JavConfigResolver
    @Inject
    constructor(
        private val appletViewerPreferences: AppletViewerPreferences,
        private val applicationPaths: ApplicationPaths,
        private val httpClient: OkHttpClient,
        private val settingsStore: SettingsStore,
    ) {
        @Throws(JavConfigResolveException::class)
        public fun resolve(): JavConfig {
            val configURLTemplate = settingsStore.getString(CONFIG_URL_PROPERTY_NAME)
            val configFileName = settingsStore.getString(CONFIG_FILE_PROPERTY_NAME)

            return if (configURLTemplate.isNotEmpty()) {
                resolveRemoteConfiguration(configURLTemplate)
            } else if (configFileName.isNotEmpty()) {
                resolveLocalConfiguration(configFileName)
            } else {
                throw JavConfigResolveException.MissingConfigurationException()
            }
        }

        @Throws(JavConfigResolveException::class)
        private fun resolveRemoteConfiguration(urlTemplate: String): JavConfig {
            val url = resolveConfigurationURLTemplate(urlTemplate)
            val config = fetchRemoteConfiguration(url) ?: throw JavConfigResolveException.LoadConfigurationException()

            return try {
                JavConfig.parse(config)
            } catch (e: Exception) {
                throw JavConfigResolveException.DecodeConfigurationException()
            }
        }

        @Throws(JavConfigResolveException::class)
        private fun resolveLocalConfiguration(fileName: String): JavConfig {
            val path = applicationPaths.resolveGameFileDirectoryPath(fileName)

            if (path == null || !path.isRegularFile()) {
                throw JavConfigResolveException.LoadConfigurationException()
            }

            return try {
                path.source().buffer().use {
                    JavConfig.parse(it.readUtf8())
                }
            } catch (_: IOException) {
                throw JavConfigResolveException.LoadConfigurationException()
            } catch (_: Exception) {
                throw JavConfigResolveException.DecodeConfigurationException()
            }
        }

        /**
         * When resolving configuration from a remote URL the applet viewer is passed a dynamic URL
         * template which we must resolve.
         *
         * The URL is in the format:
         *
         * `https://www.example.com/l=$(<key>:<defaultValue>)
         *
         * Example:
         * `http://www.runescape.com/k=3/l=$(Language:0)/jav_config.ws`
         *
         * (In this case the template variable "Language" needs to be resolved, and if it can't be, it defaults
         * to "0" which in this case means we will be falling back to the english language.
         *
         * @param template The templated URL
         * @return The resolved, valid, URL
         */
        private fun resolveConfigurationURLTemplate(template: String): String {
            var resolvedUrl = template

            while (true) {
                val variableStartIndex = resolvedUrl.indexOf("$(")

                if (variableStartIndex < 0) {
                    return resolvedUrl
                }

                val keyStartIndex = variableStartIndex + 2
                val variableSeparatorIndex = resolvedUrl.indexOf(":", startIndex = keyStartIndex)

                if (variableSeparatorIndex < 0) {
                    // We keep with the original behavior and return what we have resolved so far upon seeing an invalid
                    // variable format
                    return resolvedUrl
                }

                val defaultValueEndIndex = resolvedUrl.indexOf(")", startIndex = variableSeparatorIndex)

                if (defaultValueEndIndex < 0) {
                    return resolvedUrl
                }

                val variableName = resolvedUrl.substring(keyStartIndex, variableSeparatorIndex)

                // We resolve template variables from the preferences file
                val variableValue =
                    appletViewerPreferences.get(variableName).ifEmpty {
                        resolvedUrl.substring(variableSeparatorIndex + 1, defaultValueEndIndex)
                    }

                resolvedUrl =
                    resolvedUrl.replaceRange(
                        startIndex = variableStartIndex,
                        endIndex = defaultValueEndIndex + 1,
                        variableValue,
                    )
            }
        }

        private fun fetchRemoteConfiguration(url: String): String? {
            try {
                val request = Request.Builder().url(url).build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return null
                    }

                    response.body.use {
                        return it?.string()
                    }
                }
            } catch (_: IOException) {
                return null
            } catch (_: IllegalArgumentException) {
                return null
            }
        }

        private companion object {
            const val CONFIG_URL_PROPERTY_NAME = "com.jagex.config"
            const val CONFIG_FILE_PROPERTY_NAME = "com.jagex.configfile"
        }
    }
