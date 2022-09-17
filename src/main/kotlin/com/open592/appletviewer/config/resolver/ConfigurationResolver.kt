package com.open592.appletviewer.config.resolver

import com.open592.appletviewer.common.Constants
import com.open592.appletviewer.config.JavConfig
import com.open592.appletviewer.http.HttpFetch
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SettingsStore
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.notExists

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
public class ConfigurationResolver @Inject constructor(
    private val appletViewerPreferences: AppletViewerPreferences,
    private val httpFetch: HttpFetch,
    private val settingsStore: SettingsStore
){
    public fun resolve(): JavConfig {
        val configURLTemplate = settingsStore.getString(CONFIG_URL_PROPERTY_NAME)
        val configFileName = settingsStore.getString(CONFIG_FILE_PROPERTY_NAME)

        if (configURLTemplate.isEmpty() && configFileName.isEmpty()) {
            throw ConfigurationException.MissingConfigurationException()
        }

        return if (configURLTemplate.isNotEmpty()) {
            resolveRemoteConfiguration(configURLTemplate)
        } else {
            resolveLocalConfiguration(configFileName)
        }
    }

    private fun resolveRemoteConfiguration(urlTemplate: String): JavConfig {
        val url = resolveConfigurationURLTemplate(urlTemplate)
        val reader = httpFetch.get(url) ?: throw ConfigurationException.LoadConfigurationException()

        return try {
            JavConfig.parse(reader)
        } catch (e: Exception) {
            throw ConfigurationException.DecodeConfigurationException()
        }
    }

    private fun resolveLocalConfiguration(fileName: String): JavConfig {
        val path = getConfigFileDirectory(fileName)

        if (path.notExists()) {
            throw ConfigurationException.LoadConfigurationException()
        }

        return try {
            Files.newBufferedReader(path).use {
                JavConfig.parse(it)
            }
        } catch (e: IOException) {
            throw ConfigurationException.LoadConfigurationException()
        } catch (t: Throwable) {
            throw ConfigurationException.DecodeConfigurationException()
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
            val variableValue = appletViewerPreferences.get(variableName).ifEmpty {
                resolvedUrl.substring(variableSeparatorIndex + 1, defaultValueEndIndex)
            }

            resolvedUrl = resolvedUrl.replaceRange(
                startIndex = variableStartIndex,
                endIndex = defaultValueEndIndex + 1,
                variableValue
            )
        }
    }

    /**
     * It is assumed that the appletviewer will be invoked by the launcher which will be placed
     * in a directory a level above the "game directory" which includes a number of assets and
     * configuration files.
     *
     * > "jagexlauncher" (* Root directory for the installer *)
     * ----> "bin" > `user.dir` (* Location where the launcher will be invoked and where the jvm will be initialized *)
     * ----> "runescape" > (* Directory where we look for "com.jagex.configfile" *)
     */
    private fun getConfigFileDirectory(fileName: String): Path {
        return Path.of(settingsStore.getString("user.dir"), Constants.GAME_NAME, fileName)
    }

    private companion object {
        const val CONFIG_URL_PROPERTY_NAME = "com.jagex.config"
        const val CONFIG_FILE_PROPERTY_NAME = "com.jagex.configfile"
    }
}
