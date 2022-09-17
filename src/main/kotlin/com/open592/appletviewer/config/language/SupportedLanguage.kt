package com.open592.appletviewer.config.language

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
     *
     */
    public fun getPackagedLocalizedContent(key: String): String {
        return PACKAGED_LOCALIZED_STRINGS[this]?.get(key).orEmpty()
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
        private fun fromLanguageId(id: Int): SupportedLanguage? {
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
                    fromLanguageId(languageId.toInt())
                } catch (e: NumberFormatException) {
                    null
                }
            } else {
                null
            }
        }

        private const val LANGUAGE_PREFERENCE_KEY = "Language"
        private val PACKAGED_LOCALIZED_STRINGS: Map<SupportedLanguage, Map<String, String>> = mapOf(
            ENGLISH to mapOf(
                "err_missing_config" to "Missing com.jagex.config setting",
                "err_invalid_config" to "Invalid com.jagex.config setting",
                "loading_config" to "Loading configuration",
                "err_load_config" to
                    "There was an error loading the game configuration from the website.\n" +
                    "If you have a firewall, check that this program is allowed to access the Internet.",
                "err_decode_config" to "Error decoding configuration",
                "loaderbox_initial" to "Loading...",
                "error" to "Error",
                "quit" to "Quit"
            ),
            GERMAN to mapOf(
                "err_missing_config" to "Einstellung com.jagex.config fehlt",
                "err_invalid_config" to "Einstellung com.jagex.config ist ungültig",
                "loading_config" to "Lade Konfiguration",
                "err_load_config" to
                    "Beim Laden der Spielkonfiguration von der Website ist ein " +
                    "Fehler aufgetreten.\nBitte überprüfen Sie Ihre Firewall-Einstellungen.",
                "err_decode_config" to "Fehler beim Entschlüsseln der Konfiguration",
                "loaderbox_initial" to "Lade...",
                "error" to "Fehler",
                "quit" to "Beenden"
            ),
            FRENCH to mapOf(
                "err_missing_config" to "Paramètre com.jagex.config manquant",
                "err_invalid_config" to "Paramètre com.jagex.config non valide",
                "loading_config" to "Chargement de la configuration",
                "err_load_config" to
                    "Une erreur s'est produite lors du chargement de la configuration du jeu.\n" +
                    "Si un pare-feu est actif sur votre ordinateur, assurez-vous\n" +
                    "qu'il laisse ce programme accéder à internet.",
                "err_decode_config" to "Erreur de décodage de configuration",
                "loaderbox_initial" to "Chargement...",
                "error" to "Erreur",
                "quit" to "Quitter"
            ),
            BRAZILIAN_PORTUGUESE to mapOf(
                "err_missing_config" to "Faltando configuração de com.jagex.config",
                "err_invalid_config" to "Configuração inválida de com.jagex.config",
                "loading_config" to "Carregando configuração",
                "err_load_config" to
                    "Houve um erro quando a configuração do jogo estava sendo carregada no site.\n" +
                    "Se você tiver firewall, verifique se o programa pode ter acesso à internet.",
                "err_decode_config" to "Erro ao decodificar configuração",
                "loaderbox_initial" to "Carregando...",
                "error" to "Erro",
                "quit" to "Fechar"
            )
        )
    }
}
