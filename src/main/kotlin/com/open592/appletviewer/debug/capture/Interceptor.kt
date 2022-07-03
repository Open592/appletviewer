package com.open592.appletviewer.debug.capture

import java.io.PrintStream

/**
 * Represents a single PrintStream which can be swapped with another PrintStream, and eventually
 * released back to its existing PrintStream
 *
 * Examples supported by the OutputCapture class are:
 * - System.out
 * - System.err
 */
public abstract class Interceptor(
    public val type: CaptureType,
    public val systemStream: PrintStream
) {
    public abstract fun intercept(stream: PrintStream)
    public abstract fun release()
}
