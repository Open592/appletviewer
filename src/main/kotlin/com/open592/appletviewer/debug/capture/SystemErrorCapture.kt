package com.open592.appletviewer.debug.capture

import java.io.PrintStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SystemErrorCapture @Inject constructor(
    private val eventEmitter: OutputCaptureEventEmitter
) : Capture(CapturedMessagedType.ERR, System.err) {
    public override fun capture(stream: PrintStream) {
        System.setErr(stream)
    }

    public override fun release() {
        System.setErr(systemStream)
    }

    public override fun write(message: String) {
        eventEmitter.emit(CapturedMessage(type, message))

        // Defer to super class to determine if we should log to system stream
        super.write(message)
    }
}
