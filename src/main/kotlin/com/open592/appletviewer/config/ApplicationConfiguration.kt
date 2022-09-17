package com.open592.appletviewer.config

import com.open592.appletviewer.config.language.SupportedLanguage
import javax.inject.Singleton

@Singleton
public class ApplicationConfiguration constructor(
    private val config: JavConfig,
    private val language: SupportedLanguage,
) {
    private var activeServerOverrideKey: String = getDefaultServerOverrideKey()

    public fun getConfig(key: String): String {
        return getActiveServerOverrides()?.getConfigValue(key) ?: config.root.getConfigValue(key)
    }

    public fun getContent(key: String): String {
        return try {
            getActiveServerOverrides()?.getContentValue(key) ?: config.root.getContentValue(key).ifEmpty {
                language.getPackagedLocalizedContent(key)
            }
        } catch (e: UninitializedPropertyAccessException) {
            // `getContent` is unique in that it can function without a backing `javConfig` by
            // utilizing the `SupportedLanguage`'s packaged content.
            language.getPackagedLocalizedContent(key)
        }
    }

    public fun getParameter(key: String): String {
        return getActiveServerOverrides()?.getConfigValue(key) ?: config.root.getParameterValue(key)
    }

    private fun getActiveServerOverrides(): ServerSettings? {
        return if (activeServerOverrideKey.isNotEmpty()) {
            config.overrides[activeServerOverrideKey]
        } else {
            null
        }
    }

    private fun getDefaultServerOverrideKey(): String {
        /**
         * Upon loading the javconfig, if we have overrides, we need to default to
         * the first encountered override.
         *
         * If the user wishes to load a different server they must select that one
         * through the server selection dialog.
         */
        if (config.overrides.isNotEmpty()) {
            activeServerOverrideKey = config.overrides.asIterable().first().key
        }

        return ""
    }
}
