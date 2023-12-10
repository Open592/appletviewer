package com.open592.appletviewer.config

import com.open592.appletviewer.config.javconfig.JavConfig
import com.open592.appletviewer.config.javconfig.ServerConfiguration
import com.open592.appletviewer.config.language.SupportedLanguage
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
public class ApplicationConfiguration
@Inject
constructor(
    private val language: SupportedLanguage,
) {
    private var activeServerOverrideKey: String = ""
    private lateinit var javConfig: JavConfig

    /**
     * Before we can serve most application configuration we need to be supplied
     * a backing JavConfig. This is lazy-loaded from Viewer after the application
     * has been started.
     */
    public fun initialize(config: JavConfig) {
        /**
         * Upon loading the JavConfig, if we have overrides, we need to default to
         * the first encountered override.
         *
         * If the user wishes to load a different server they must select that one
         * through the server selection dialog.
         */
        if (config.overrides.isNotEmpty()) {
            activeServerOverrideKey = config.overrides.asIterable().first().key
        }

        javConfig = config
    }

    public fun getConfig(key: String): String {
        val overrideConfig = getActiveServerOverrides()?.getConfig(key)

        if (overrideConfig?.isNotEmpty() == true) {
            return overrideConfig
        }

        return javConfig.root.getConfig(key)
    }

    public fun getConfigAsInt(key: String): Int? {
        return try {
            getConfig(key).toInt()
        } catch (_: NumberFormatException) {
            null
        }
    }

    public fun getContent(key: String): String {
        if (!this::javConfig.isInitialized) {
            // `getContent` is unique in that it can function without a backing `javConfig` by
            // utilizing the `SupportedLanguage`'s packaged content.
            return language.getPackagedLocalizedContent(key)
        }

        val serverOverrideContent = getActiveServerOverrides()?.getContent(key)

        if (serverOverrideContent?.isNotEmpty() == true) {
            return serverOverrideContent
        }

        return javConfig.root.getContent(key).ifEmpty {
            language.getPackagedLocalizedContent(key)
        }
    }

    public fun getParameter(key: String): String {
        val serverOverrideParameter = getActiveServerOverrides()?.getParameter(key)

        if (serverOverrideParameter?.isNotEmpty() == true) {
            return serverOverrideParameter
        }

        return javConfig.root.getParameter(key)
    }

    /**
     * In case there are active server configuration overrides we need
     * to check for them before falling back to the root.
     */
    private fun getActiveServerOverrides(): ServerConfiguration? {
        if (!this::javConfig.isInitialized) {
            return null
        }

        return if (activeServerOverrideKey.isNotEmpty()) {
            javConfig.overrides[activeServerOverrideKey]
        } else {
            null
        }
    }
}
