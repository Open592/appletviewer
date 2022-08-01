package com.open592.appletviewer.localization

import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides access to localized strings used within the applet viewer.
 *
 * Even though we provide the ability to select a new language through the language
 * selection toolbar action, we don't have to handle updating it here since we require
 * the user to restart the application for the changes to take effect.
 *
 * We provide a selection of initial localized strings in all supported languages to
 * use before we have resolved the jav_config.ws file. After resolving that file, we
 * will be provided subsequent strings from there.
 */
@Singleton
public class Localization @Inject constructor(
    language: SupportedLanguage
) {
    private val content = getInitialLocalizedContentStrings(language).toMutableMap()

    /**
     * Given a key into the localized content map, retrieve a localized content string.
     *
     * Before the jav_config.ws file is loaded from the server the content map will
     * consist only of the initial localized content strings.
     *
     * If no content string is found, an empty string is returned.
     */
    public fun getContent(key: String): String {
        return content[key].orEmpty()
    }

    /**
     * Set a single localized content string in the content map
     */
    public fun setContent(key: String, value: String) {
        content[key] = value
    }

    /**
     * Set multiple localized content strings in the content map
     */
    public fun setContent(strings: Map<String, String>) {
        content.putAll(strings)
    }

    private companion object {
        private fun getInitialLocalizedContentStrings(language: SupportedLanguage): Map<String, String> {
            return when (language) {
                SupportedLanguage.ENGLISH -> mapOf(
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
                )
                SupportedLanguage.GERMAN -> mapOf(
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
                )
                SupportedLanguage.FRENCH -> mapOf(
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
                )
                SupportedLanguage.BRAZILIAN_PORTUGUESE -> mapOf(
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
            }
        }
    }
}
