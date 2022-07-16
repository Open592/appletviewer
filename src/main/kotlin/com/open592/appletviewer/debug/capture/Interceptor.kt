package com.open592.appletviewer.debug.capture

import java.io.PrintStream

/**
 * Represents a single PrintStream which can be swapped with another PrintStream, and eventually
 * released back to its existing PrintStream.
 *
 * Examples supported by the OutputCapture class are:
 * - System.out
 * - System.err
 */
public abstract class Interceptor(
    public val type: CapturedMessagedType,
    public val systemStream: PrintStream,
    public var shouldLogToSystemStream: Boolean = false
) {
    public abstract fun capture(stream: PrintStream)
    public abstract fun release()
    public open fun write(message: String) {
        if (shouldLogToSystemStream) {
            systemStream.print(message)
        }
    }
    public open fun flush() {
        if (shouldLogToSystemStream) {
            systemStream.flush()
        }
    }
}
