package com.open592.debug.capture

import java.io.OutputStream
import java.io.PrintStream

internal class OutputStreamCapture(
    private val systemStream: PrintStream,
    private val handler: (String) -> Unit
) : OutputStream() {
    override fun write(b: Int) {
        super.write(
            ByteArray(1) {
                b.toByte()
            }
        )
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        handler(String(b, off, len))

        systemStream.write(b, off, len)
    }

    override fun flush() {
        systemStream.flush()
    }
}
