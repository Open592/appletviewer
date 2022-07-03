package com.open592.appletviewer.debug.capture

/**
 * Provides easy access to output sent to either StdErr or StdOut
 *
 * We use this class to facilitate catching messages sent to the standard output streams
 * and displaying them within the debug console. In the original appletviewer, when this
 * feature was enabled, there was no choice but to intercept the messages, but in Open592's
 * implementation we have the option of passing them along to the underlying system stream.
 *
 * NOTE: Due to keeping the same behavior as the original implementation, we do not publish
 * a message until we receive a line separator.
 *
 * This class has a contract which requires that upon initialization both `System.out` and
 * `System.err` will be captured. In the case that you wish to return to using the original
 * system streams you must explicitly call `.release()`
 */
public class OutputCapture {
    private val err: PrintStreamCapture = PrintStreamCapture(System.err, ::captureErr)
    private val out: PrintStreamCapture = PrintStreamCapture(System.out, ::captureOut)
    private val messages: MutableList<CapturedMessage> = mutableListOf()

    init {
        System.setErr(this.err)
        System.setOut(this.out)
    }

    public fun release() {
        System.setErr(this.err.getSystemStream())
        System.setOut(this.out.getSystemStream())
    }

    public fun getErr(): List<String> {
        return get(CaptureType.ERR)
    }

    public fun getOut(): List<String> {
        return get(CaptureType.OUT)
    }

    private fun get(type: CaptureType): List<String> {
        return messages.filter { it.type == type }.map { it.message }
    }

    private fun captureErr(message: String) {
        capture(message, CaptureType.ERR)
    }

    private fun captureOut(message: String) {
        capture(message, CaptureType.OUT)
    }

    private fun capture(message: String, type: CaptureType) {
        messages.add(CapturedMessage(type, message))
    }
}
