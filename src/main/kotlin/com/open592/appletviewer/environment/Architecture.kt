package com.open592.appletviewer.environment

/**
 * The currently supported architectures.
 */
public enum class Architecture(private val needles: Set<String>) {
    X86(setOf("x86", "i386")),
    X86_64(setOf("x86_64", "amd64")),
    ;

    public companion object {
        public fun detect(osArch: String): Architecture {
            return try {
                Architecture.values().first {
                    it.needles.contains(osArch)
                }
            } catch (_: NoSuchElementException) {
                error("The following architecture is not supported: $osArch")
            }
        }
    }
}
