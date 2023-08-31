package com.open592.appletviewer.environment

/**
 * The currently supported operating systems.
 */
public enum class OperatingSystem(private val supportedArchitectures: Set<Architecture>, private val needle: String) {
    LINUX(setOf(Architecture.X86_64), "linux"),
    WINDOWS(setOf(Architecture.X86, Architecture.X86_64), "win"),
    ;

    /**
     * Detect the present architecture and determine if it is supported.
     *
     * @returns The supported architecture. If we can't detect a supported
     * architecture we throw an exception.
     */
    public fun detectArchitecture(osArch: String): Architecture {
        val architecture = Architecture.detect(osArch)

        if (!this.supportedArchitectures.contains(architecture)) {
            error("$osArch is not supported on $this")
        }

        return architecture
    }

    public companion object {
        public fun detect(osName: String): OperatingSystem {
            return try {
                OperatingSystem.values().first {
                    osName.startsWith(it.needle, ignoreCase = true)
                }
            } catch (_: NoSuchElementException) {
                error("Failed to detect a supported operating system! Found: $osName")
            }
        }
    }
}
