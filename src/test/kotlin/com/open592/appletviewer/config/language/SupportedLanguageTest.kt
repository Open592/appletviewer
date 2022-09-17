package com.open592.appletviewer.config.language

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.preferences.AppletViewerPreferences
import java.util.Locale
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
    fun `Should resolve the a us-EN user's language, when there doesnt exist a preferences entry`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Mock locale
        Locale.setDefault(Locale("en", "US"))

        assertEquals(SupportedLanguage.ENGLISH, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Should resolve the a de-DE user's language, when there doesnt exist a preferences entry`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Mock locale
        Locale.setDefault(Locale("de", "DE"))

        assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Should resolve the a fr-FR user's language, when there doesnt exist a preferences entry`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Mock locale
        Locale.setDefault(Locale("fr", "FR"))

        assertEquals(SupportedLanguage.FRENCH, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Should resolve the a pt-BR user's language, when there doesnt exist a preferences entry`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Mock locale
        Locale.setDefault(Locale("pt", "BR"))

        assertEquals(SupportedLanguage.BRAZILIAN_PORTUGUESE, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Should fall back to ENGLISH for user's with unsupported languages, when there doesnt exist a preferences entry`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Mock locale
        Locale.setDefault(Locale("uk", "UA"))

        assertEquals(SupportedLanguage.ENGLISH, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Should correctly resolve users language from the preferences file when it exists`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)
        // We use german to verify we aren't falling back to ENGLISH
        val expectedLanguage = SupportedLanguage.GERMAN

        // Write the language value to the preferences file
        preferences.set("Language", expectedLanguage.getLanguageID().toString())

        // Setting the locale to a different locale to verify we aren't falling back to the Locale
        // to resolve the users language
        Locale.setDefault(Locale("en", "US"))

        assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve(preferences))
    }

    @Test
    fun `Given a invalid language code within the preferences file we should fall back to resolving using default Locale`() {
        val fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
        // This should return a path which doesn't resolve to a file
        val filePath = fs.getPath(AppletViewerPreferences.DEFAULT_FILE_NAME)
        val preferences = AppletViewerPreferences(filePath)

        // Write the invalid language ID to the preferences file. We use a value which can't be
        // converted to an `Int` to verify we are correctly handling the exception thrown from
        // `String.toInt()`
        preferences.set("Language", "not-a-int")

        Locale.setDefault(Locale("de", "DE"))

        assertEquals(SupportedLanguage.GERMAN, SupportedLanguage.resolve((preferences)))
    }
}
