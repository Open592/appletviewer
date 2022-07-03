package com.open592.appletviewer.debug.capture

import java.io.PrintStream

public class SystemErrorInterceptor : Interceptor(CaptureType.ERR, System.err) {
    public override fun intercept(stream: PrintStream) {
        System.setErr(stream)
    }

    public override fun release() {
        System.setErr(systemStream)
    }
}
