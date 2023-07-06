package com.open592.appletviewer.environment

import com.open592.appletviewer.settings.SystemPropertiesSettingsStore
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

data class TestCase(val osName: String, val osArch: String, val expectedOperatingSystem: OperatingSystem, val expectedArchitecture: Architecture)

class EnvironmentTest {
    @Test
    fun `Should properly detect each operating system architecture combination`() {
        val testCases = listOf(
            TestCase("Windows 11", "amd64", OperatingSystem.WINDOWS, Architecture.AMD64),
            TestCase("Mac OS X", "aarch64", OperatingSystem.OSX, Architecture.AARCH64),
            TestCase("Linux", "amd64", OperatingSystem.LINUX, Architecture.AMD64),
        )

        testCases.forEach {
            val settingsStore = mockk<SystemPropertiesSettingsStore>()

            every { settingsStore.getString("os.name") } returns it.osName
            every { settingsStore.getString("os.arch") } returns it.osArch

            assertDoesNotThrow {
                val environment = Environment.detect(settingsStore)

                assertEquals(it.expectedOperatingSystem, environment.os)
                assertEquals(it.expectedArchitecture, environment.arch)
            }
        }
    }
}
