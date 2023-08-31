package com.open592.appletviewer.debug.capture

import java.io.OutputStream
import java.io.PrintStream

public class PrintStreamCapture(
    interceptor: Interceptor,
) : PrintStream(OutputStreamCapture(interceptor)) {
    /**
     * Implements a OutputStream which intercepts a PrintStream and accepts
     * a callback which is invoked after receiving a line of input.
     *
     * The original implementation read each character looking for `\n`
     * explicitly. We update the implementation by checking each
     * chunk for `System.lineSeparator()`.
     */
    private class OutputStreamCapture(private val interceptor: Interceptor) : OutputStream() {
        private val line: StringBuilder = StringBuilder()

        override fun write(b: Int) {
            super.write(
                ByteArray(1) {
                    b.toByte()
                },
            )
        }

        override fun write(
            b: ByteArray,
            off: Int,
            len: Int,
        ) {
            val chunk = String(b, off, len)

            if (chunk == System.lineSeparator()) {
                /**
                 * NOTE: In the original implementation, since they were checking for `\n`
                 * within the `write(int)` function, on Windows they would retain the `\r`
                 * portion of the lineSeparator.
                 *
                 * In our implementation we utilize `.appendLine` which appends `\n`
                 * regardless of system line separator.
                 *
                 * We defer to the capture for whether we will write to the underlying system stream
                 * or not.
                 */
                interceptor.write(line.appendLine().toString())

                line.clear()

                return
            }

            line.append(chunk)
        }

        // We defer to the capture for whether we will flush the system stream or not
        override fun flush() {
            interceptor.flush()
        }
    }
}
