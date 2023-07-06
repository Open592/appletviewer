package com.open592.appletviewer.common

public object Constants {
    public enum class OperatingSystemFamily {
        Windows,
    }

    public const val GAME_NAME: String = "runescape"
    public val OPERATING_SYSTEM_FAMILY: OperatingSystemFamily = resolveOperatingSystemFamily()

    private fun resolveOperatingSystemFamily(): OperatingSystemFamily {
        return OperatingSystemFamily.Windows
    }
}
