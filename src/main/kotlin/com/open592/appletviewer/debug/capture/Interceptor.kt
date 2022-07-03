package com.open592.appletviewer.debug.capture

import java.io.PrintStream

public abstract class Interceptor(
    public val type: CaptureType,
    public val systemStream: PrintStream
) {
    public abstract fun intercept(stream: PrintStream)
    public abstract fun release()
}
