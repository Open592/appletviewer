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
public abstract class Capture(
    public val type: CapturedMessagedType,
    public val systemStream: PrintStream
) {
    public abstract fun capture(stream: PrintStream)
    public abstract fun release()
    public abstract fun write(message: String)
    public abstract fun flush()
}
