package com.open592.appletviewer.jar

import com.open592.appletviewer.settings.SettingsStore
import io.mockk.every
import io.mockk.mockk
import okio.Buffer
import okio.buffer
import okio.source
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class SignedJarFileResolverTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid-jar-file-type.txt",
            "invalid-incorrect-signature.jar",
            "unsigned-jar.jar",
        ],
    )
    fun `Should not return entries for invalid jar files`(filename: String) {
        val settingsStore = mockk<SettingsStore>()

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns ""
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns ""
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val resolver = SignedJarFileResolver(certificateValidator)

        resolveFileResource(filename).use {
            assertEquals(emptyMap(), resolver.resolveEntries(it))
        }
    }

    @Test
    fun `Should successfully resolve entries for an unsigned jar when jar validation is disabled`() {
        val settingsStore = mockk<SettingsStore>()

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns ""
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns ""
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns true

        val certificateValidator = CertificateValidator(settingsStore)
        val resolver = SignedJarFileResolver(certificateValidator)

        resolveFileResource("unsigned-jar.jar").use { file ->
            val entries = resolver.resolveEntries(file)

            assertEquals(entries.size, 1)
            assertEquals(EXPECTED_TEST_JAR_ENTRY_CONTENTS, entries[TEST_JAR_ENTRY_NAME]?.readUtf8Line())
        }
    }

    @Test
    fun `Should successfully resolve entries for valid Jagex jar files`() {
        val settingsStore = mockk<SettingsStore>()

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns ""
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns ""
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val resolver = SignedJarFileResolver(certificateValidator)

        resolveFileResource("valid-official-jagex-loader.jar").use { file ->
            val entries = resolver.resolveEntries(file)

            // The official 592 loader jar contains 15 entries
            assertEquals(entries.size, 15)

            // Test for presence of some known entries
            assertNotNull(entries["loader.class"])
            assertNotNull(entries["unpack.class"])
        }
    }

    @Test
    fun `Should not return entries for valid Open592 jars by default`() {
        val settingsStore = mockk<SettingsStore>()

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns ""
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns ""
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val resolver = SignedJarFileResolver(certificateValidator)

        resolveFileResource("valid-open592-test-jar.jar").use {
            assertEquals(emptyMap(), resolver.resolveEntries(it))
        }
    }

    @Test
    fun `Should successfully resolve entries for valid Open592 jars when supplied with the correct public keys`() {
        val settingsStore = mockk<SettingsStore>()

        every { settingsStore.getString("com.open592.fakeThawtePublicKey") } returns FAKE_THAWTE_PUBLIC_KEY
        every { settingsStore.getString("com.open592.fakeJagexPublicKey") } returns FAKE_JAGEX_PUBLIC_KEY
        every { settingsStore.getBoolean("com.open592.disableJarValidation") } returns false

        val certificateValidator = CertificateValidator(settingsStore)
        val resolver = SignedJarFileResolver(certificateValidator)

        resolveFileResource("valid-open592-test-jar.jar").use { file ->
            val entries = resolver.resolveEntries(file)

            assertEquals(1, entries.size)
            assertEquals(EXPECTED_TEST_JAR_ENTRY_CONTENTS, entries[TEST_JAR_ENTRY_NAME]?.readUtf8Line())
        }
    }

    private fun resolveFileResource(filename: String): Buffer {
        val sink = Buffer()

        this::class.java.getResourceAsStream(filename)?.source()?.buffer()?.use {
            it.readAll(sink)
        } ?: fail("Failed to resolve $filename in SignedJarFileResolverTest")

        return sink
    }

    companion object {
        // The jar files (besides the original Jagex file) in the test fixtures include a single entry with the
        // following name
        const val TEST_JAR_ENTRY_NAME = "index.txt"

        // The expected contents of the test jar entry
        const val EXPECTED_TEST_JAR_ENTRY_CONTENTS = "Open592"

        // Our "Open592 Thawte" public key
        const val FAKE_THAWTE_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCg1kw0oUcTFY5OTCluVXHSCOA33ePAePh9" +
            "lMCRewLpX9XtVVsqDzhopFi4WD7ih19zpdXvMcK61HI7/99TnoD8upiBIeH3bP3h/30i1rQ6S4kDYLBDF58ErzWqp71NurE35sa1bFF1" +
            "SFtBy17AuSkJLszcK+Z+0Auxvd30sFcXQIDAQAB"

        // Our "Open592 Jagex" public key
        const val FAKE_JAGEX_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu5fJjw2jQ5wmDqjeEf7kYtwmVhk8Fd" +
            "y0y1Vg+G6azsCiW68pYRaLW7kr/VHfpl6eYyupfpfDnyWqTxGvKoHT28dJHETjN+PLubOGhiwL0KYMx6CUIoTBKXMRRBIa6P07RLYJu9" +
            "fJyFtKmhb+ept0os+hUDUYquOgCgNF42C2rpmNe3cxm1BO1EGDFXwZHBzwVX06F1v+xcnwkxBqCOFg1zuNpqlK/2THZX3iaMnnjl8B7a" +
            "d77D+7vzAQThdMPIOj4MmW5CGX70fQgyCRVXRYeRXvvbCpPNPUiZ2jtWCIib6G4pUPp1uAGQXouILp/wMQPhW4EoGABc21B8LVpboM8Q" +
            "IDAQAB"
    }
}
