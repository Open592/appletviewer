package com.open592.appletviewer.localization

import com.open592.appletviewer.preferences.AppletViewerPreferences
import java.lang.NumberFormatException
import java.util.Locale

/**
 * Represents the supported languages for this version of the AppletViewer
 *
 * These languages will be represented in the initial locale strings before we
 * resolve the full locale content strings from the server.
 */
public enum class SupportedLanguage {
    ENGLISH,
    GERMAN,
    FRENCH,
    BRAZILIAN_PORTUGUESE;

    /**
     * Due to how Jagex references languages based on 0-indexed value, we
     * can conveniently use the enum's ordinal to reference the ID.
     */
    public fun getLanguageID(): Int {
        return this.ordinal
    }

    /**
     * Write the SupportedLanguage value to the applet viewer preferences file. This allows
     * for reference by future sessions.
     */
    private fun writeToPreferences(preferences: AppletViewerPreferences) {
        preferences.set(LANGUAGE_PREFERENCE_KEY, this.getLanguageID().toString())
    }

    public companion object {
        /**
         * Resolve the user's language.
         *
         * We first attempt to resolve the language from the preferences file (set during a previous sessions).
         * If that fails we resolve using default locale and write the resulting language ID to the preferences
         * file for future sessions.
         */
        public fun resolve(preferences: AppletViewerPreferences): SupportedLanguage {
            val languageFromPreferences = fromPreferences(preferences)

            if (languageFromPreferences != null) {
                return languageFromPreferences
            }

            val userLanguage = fromDefaultLocale()

            userLanguage.writeToPreferences(preferences)

            return userLanguage
        }

        /**
         * Attempt to resolve the user's language using their default locale.
         *
         * If we fail to find an appropriate locale we fall back to ENGLISH
         */
        private fun fromDefaultLocale(): SupportedLanguage {
            val defaultLocale = Locale.getDefault()

            return fromISO3LanguageID(defaultLocale.isO3Language)
                ?: fromISO3CountryID(defaultLocale.isO3Country)
                ?: ENGLISH
        }

        /**
         * Given an ISO3 country ID, attempt to resolve a SupportedLanguage.
         */
        private fun fromISO3CountryID(id: String): SupportedLanguage? {
            return when (id) {
                "GB", "US" -> ENGLISH
                "DE" -> GERMAN
                "FR" -> FRENCH
                "BR" -> BRAZILIAN_PORTUGUESE
                else -> null
            }
        }

        /**
         * Given an ISO3 language ID, attempt to resolve a SupportedLanguage.
         */
        private fun fromISO3LanguageID(id: String): SupportedLanguage? {
            return when (id) {
                "eng" -> ENGLISH
                "ger", "deu" -> GERMAN
                "fre", "fra" -> FRENCH
                "por" -> BRAZILIAN_PORTUGUESE
                else -> null
            }
        }

        /**
         * Given a language ID attempt to resolve a SupportedLanguage.
         */
        private fun fromLanguageID(id: Int): SupportedLanguage? {
            return when (id) {
                0 -> ENGLISH
                1 -> GERMAN
                2 -> FRENCH
                3 -> BRAZILIAN_PORTUGUESE
                else -> null
            }
        }

        /**
         * Attempt to get the user language by looking into the AppletViewer preferences files.
         */
        private fun fromPreferences(preferences: AppletViewerPreferences): SupportedLanguage? {
            val languageId = preferences.get(LANGUAGE_PREFERENCE_KEY)

            return if (languageId.isNotEmpty()) {
                try {
                    fromLanguageID(languageId.toInt())
                } catch (e: NumberFormatException) {
                    null
                }
            } else {
                null
            }
        }

        private const val LANGUAGE_PREFERENCE_KEY = "Language"
    }
}
