package com.open592.debug.capture

public data class CapturedMessage(private var _message: String, val type: CaptureType) {
    val message: String get() = _message

    public fun appendLine() {
        _message = "$_message${System.lineSeparator()}"
    }
}
