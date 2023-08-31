package com.open592.appletviewer.environment

import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.test.Test
import kotlin.test.assertEquals

data class TestCase(
    val osName: String,
    val osArch: String,
    val expectedOperatingSystem: OperatingSystem,
    val expectedArchitecture: Architecture,
)

class EnvironmentTest {
    @Test
    fun `Should throw exception when encountering an unsupported operating system and architecture combination`() {
        val testCases =
            listOf(
                // We don't support ARM on Windows
                Pair("Windows 8.1", "aarch64"),
                // We don't currently support Mac
                Pair("Mac OS X", "aarch64"),
                Pair("Mac OS X", "aarch64"),
                Pair("Mac OS X", "x86_64"),
                // Linux is only supported on 64 bit arches
                Pair("Linux", "x86"),
                Pair("Linux", "i386"),
            )

        testCases.forEach { (osName, osArch) ->
            val settingsStore = mockk<SystemPropertiesSettingsStore>()

            every { settingsStore.getString("os.name") } returns osName
            every { settingsStore.getString("os.arch") } returns osArch

            assertThrows<IllegalStateException> {
                Environment(settingsStore)
            }
        }
    }

    @Test
    fun `Should properly detect each supported operating system and architecture combination`() {
        val testCases =
            listOf(
                TestCase("Windows 11", "amd64", OperatingSystem.WINDOWS, Architecture.X86_64),
                TestCase("Windows NT (unknown)", "x86_64", OperatingSystem.WINDOWS, Architecture.X86_64),
                TestCase("Linux", "x86_64", OperatingSystem.LINUX, Architecture.X86_64),
            )

        testCases.forEach {
            val settingsStore = mockk<SystemPropertiesSettingsStore>()

            every { settingsStore.getString("os.name") } returns it.osName
            every { settingsStore.getString("os.arch") } returns it.osArch

            assertDoesNotThrow {
                val environment = Environment(settingsStore)

                assertEquals(it.expectedOperatingSystem, environment.getOperatingSystem())
                assertEquals(it.expectedArchitecture, environment.getArchitecture())
            }
        }
    }
}
