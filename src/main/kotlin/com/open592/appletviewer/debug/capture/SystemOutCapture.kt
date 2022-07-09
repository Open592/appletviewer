package com.open592.appletviewer.debug.capture

import java.io.PrintStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SystemOutCapture @Inject constructor(
    private val writer: CaptureWriter
) : Capture(CapturedMessagedType.OUT, System.out) {
    public override fun capture(stream: PrintStream) {
        System.setOut(stream)
    }

    public override fun release() {
        System.setOut(systemStream)
    }

    public override fun write(message: String) {
        writer.write(CapturedMessage(type, message))
    }

    public override fun flush() {
        systemStream.flush()
    }
}
