package com.open592.appletviewer.environment

import com.open592.appletviewer.environment.Architecture.AMD64
import com.open592.appletviewer.environment.Architecture.AARCH64
import com.open592.appletviewer.environment.Architecture.I386

public enum class OperatingSystem(public val family: OperatingSystemFamily, private val supportedArchitectures: Set<Architecture>, private val needle: String) {
    LINUX(OperatingSystemFamily.Unix, setOf(AMD64), "linux"),
    OSX(OperatingSystemFamily.Unix, setOf(AARCH64, AMD64, I386), "mac"),
    WINDOWS(OperatingSystemFamily.Windows, setOf(AARCH64, AMD64, I386), "win");

    /**
     * Attempt to detect the supported architecture on this platform
     *
     * @returns The supported Architecture. If we are unable to detect this
     * we throw an exception
     */
    public fun detectArchitecture(osArch: String): Architecture {
        val architecture = Architecture.detect(osArch)

        if (!this.supportedArchitectures.contains(architecture)) {
            error("$osArch is not supported on $this")
        }

        return architecture
    }

    public companion object {
        /**
         * Given the value of "os.name" return the detected operating system.
         *
         * In the case we can't detect a supported operating system we throw
         * an exception.
         */
        public fun detect(osName: String): OperatingSystem {
            if (osName.startsWith(LINUX.needle, ignoreCase = true)) {
                return LINUX
            }

            if (osName.startsWith(OSX.needle, ignoreCase = true)) {
                return OSX
            }

            if (osName.startsWith(WINDOWS.needle, ignoreCase = true)) {
                return WINDOWS
            }

            error("Failed to detect a supported operating system! Found: $osName")
        }
    }
}
