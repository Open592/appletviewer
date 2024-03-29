package com.open592.appletviewer.config.language

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder
import com.open592.appletviewer.preferences.AppletViewerPreferences
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SupportedLanguageTest {
    private val storedLocale = Locale.getDefault()

    @AfterTest
    fun resetLocale() {
        Locale.setDefault(storedLocale)
    }

    @Test
    fun `Should resolve the us-EN user's language, when there is no preferences entry`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)

            // Mock locale
            Locale.setDefault(Locale("en", "US"))

            assertEquals(SupportedLanguage.ENGLISH, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Should resolve the de-DE user's language, when there is no preferences entry`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)

            // Mock locale
            Locale.setDefault(Locale("de", "DE"))

            assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Should resolve the fr-FR user's language, when there is no preferences entry`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)

            // Mock locale
            Locale.setDefault(Locale("fr", "FR"))

            assertEquals(SupportedLanguage.FRENCH, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Should resolve the pt-BR user's language, when there is no preferences entry`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)

            // Mock locale
            Locale.setDefault(Locale("pt", "BR"))

            assertEquals(SupportedLanguage.BRAZILIAN_PORTUGUESE, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Should fall back to ENGLISH for users with unsupported languages, when there is no preferences entry`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            // The preferences file is not present on the fs for this test
            val preferences = AppletViewerPreferences(fs)

            // Mock locale
            Locale.setDefault(Locale("uk", "UA"))

            assertEquals(SupportedLanguage.ENGLISH, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Should correctly resolve a user's language from the preferences file when it exists`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)
            // We use german to verify we aren't falling back to ENGLISH
            val expectedLanguage = SupportedLanguage.GERMAN

            // Write the language value to the preferences file
            preferences.set("Language", expectedLanguage.getLanguageId().toString())

            // Setting the locale to a different locale to verify we aren't falling back to the Locale
            // to resolve the users language
            Locale.setDefault(Locale("en", "US"))

            assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve(preferences))
        }
    }

    @Test
    fun `Given a invalid language code within the preferences file we should fall back to resolving the default Locale`() {
        MemoryFileSystemBuilder.newLinux().build().use { fs ->
            val preferences = AppletViewerPreferences(fs)

            // Write the invalid language ID to the preferences file. We use a value which can't be
            // converted to an `Int` to verify we are correctly handling the exception thrown from
            // `String.toInt()`
            preferences.set("Language", "not-a-int")

            Locale.setDefault(Locale("de", "DE"))

            assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve((preferences)))
        }
    }

    @Test
    fun `Should correctly return initial locale content for each supported language`() {
        val contentKey = "loaderbox_initial"

        // SupportedLanguage to expected output
        val testCases =
            mapOf(
                SupportedLanguage.ENGLISH to "Loading...",
                SupportedLanguage.GERMAN to "Lade...",
                SupportedLanguage.FRENCH to "Chargement...",
                SupportedLanguage.BRAZILIAN_PORTUGUESE to "Carregando...",
            )

        testCases.forEach { testCase ->
            assertEquals(testCase.value, testCase.key.getPackagedLocalizedContent(contentKey))
        }
    }
}
