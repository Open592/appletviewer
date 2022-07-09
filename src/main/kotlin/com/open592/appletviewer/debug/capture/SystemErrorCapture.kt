package com.open592.appletviewer.debug.capture

import java.io.PrintStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SystemErrorCapture @Inject constructor(
    private val writer: CaptureWriter
) : Capture(CapturedMessagedType.ERR, System.err) {
    public override fun capture(stream: PrintStream) {
        System.setErr(stream)
    }

    public override fun release() {
        System.setErr(systemStream)
    }

    public override fun write(message: String) {
        writer.write(CapturedMessage(type, message))
    }

    public override fun flush() {
        systemStream.flush()
    }
}
