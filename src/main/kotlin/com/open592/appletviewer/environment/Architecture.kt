package com.open592.appletviewer.environment

public enum class Architecture (private val needles: Set<String>){
    AMD64(setOf("x86_64", "amd64", "k8")),
    I386(setOf("x86", "i386")),
    AARCH64(setOf("aarch64"));

    public companion object {
        public fun detect(osArch: String): Architecture {
            return try {
                Architecture.values().first { it.needles.contains(osArch) }
            } catch (_: NoSuchElementException) {
                error("The following architecture is not supported: $osArch")
            }
        }
    }
}
