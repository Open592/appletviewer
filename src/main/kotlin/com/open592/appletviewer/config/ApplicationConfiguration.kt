package com.open592.appletviewer.config

import com.open592.appletviewer.localization.Localization
import com.open592.appletviewer.modal.ApplicationModal
import com.open592.appletviewer.modal.ApplicationModalType
import com.open592.appletviewer.preferences.AppletViewerPreferences
import com.open592.appletviewer.settings.SettingsStore
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ApplicationConfiguration @Inject constructor(
    private val applicationModal: ApplicationModal,
    private val appletViewerPreferences: AppletViewerPreferences,
    private val localization: Localization,
    private val settingsStore: SettingsStore
) {
    public fun initialize() {
        val configURL = settingsStore.getString(CONFIG_URL_PROPERTY_NAME)
        val configFileName = settingsStore.getString(CONFIG_FILE_PROPERTY_NAME)

        if (configURL.isEmpty() && configFileName.isEmpty()) {
            return applicationModal.eventBus.dispatchDisplayEvent(
                ApplicationModalType.FATAL_ERROR,
                localization.getContent("err_missing_config")
            )
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
     * @param url The templated URL
     * @return The resolved, valid, URL
     */
    private fun resolveConfigurationURLTemplate(url: String): String {
        var resolvedUrl = url

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
        return Path.of(settingsStore.getString("user.dir"), fileName)
    }

    private companion object {
        const val CONFIG_URL_PROPERTY_NAME = "com.jagex.config"
        const val CONFIG_FILE_PROPERTY_NAME = "com.jagex.configfile"
    }
}
