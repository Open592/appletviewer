package com.open592.appletviewer.debug.capture

import java.io.OutputStream
import java.io.PrintStream

/**
 * Implements a OutputStream which intercepts a PrintStream and accepts
 * a callback which is invoked after receiving a line of input.
 *
 * The original implementation read each character looking for `\n`
 * explicitly. We update the implementation by checking each
 * chunk for `System.lineSeparator()`.
 */
internal class OutputStreamCapture(
    private val systemStream: PrintStream,
    private val handler: (String) -> Unit
) : OutputStream() {
    private val line: StringBuilder = StringBuilder()

    override fun write(b: Int) {
        super.write(
            ByteArray(1) {
                b.toByte()
            }
        )
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val chunk = String(b, off, len)

        if (chunk == System.lineSeparator()) {
            /**
             * NOTE: In the original implementation, since they were checking for `\n`
             * within the `write(int)` function, on Windows they would retain the `\r`
             * portion of the lineSeparator.
             *
             * In our implementation we utilize `.appendLine` which appends `\n`
             * regardless of system line separator.
             */
            handler(line.appendLine().toString())

            line.clear()

            return
        }

        line.append(chunk)
    }

    override fun flush() {
        systemStream.flush()
    }
}
