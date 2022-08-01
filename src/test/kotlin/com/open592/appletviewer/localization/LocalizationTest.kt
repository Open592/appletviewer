package com.open592.appletviewer.localization

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizationTest {
    @Test
    fun `Should correctly return initial locale content for each supported language`() {
        val contentKey = "loaderbox_initial"
        // SupportedLanguage to expected output
        val testCases = mapOf(
            SupportedLanguage.ENGLISH to "Loading...",
            SupportedLanguage.GERMAN to "Lade...",
            SupportedLanguage.FRENCH to "Chargement...",
            SupportedLanguage.BRAZILIAN_PORTUGUESE to "Carregando..."
        )

        testCases.forEach { testCase ->
            val localization = Localization(testCase.key)

            assertEquals(testCase.value, localization.getContent(contentKey))
        }
    }

    @Test
    fun `Should handle adding a localized content string`() {
        val (newKey, newValue) = Pair("new-key", "new-value")
        val localization = Localization(SupportedLanguage.ENGLISH)

        localization.setContent(newKey, newValue)

        assertEquals(newValue, localization.getContent(newKey))
    }

    @Test
    fun `Should handle adding a group of new localized content strings`() {
        val contentStrings = mapOf(
            "first-key" to "first-value",
            "second-key" to "second-value",
            "third-key" to "third-value"
        )
        val localization = Localization(SupportedLanguage.ENGLISH)

        localization.setContent(contentStrings)

        contentStrings.forEach {
            assertEquals(it.value, localization.getContent(it.key))
        }
    }
}
