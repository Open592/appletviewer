package com.open592.debug.capture

import java.io.PrintStream

internal class PrintStreamCapture(
    private val systemStream: PrintStream,
    handler: (String) -> Unit
) : PrintStream(OutputStreamCapture(systemStream, handler)) {
    fun getSystemStream(): PrintStream {
        return systemStream
    }
}
