package com.open592.appletviewer.debug.capture

import java.io.PrintStream

public class SystemOutInterceptor : Interceptor(CaptureType.OUT, System.out) {
    public override fun intercept(stream: PrintStream) {
        System.setOut(stream)
    }

    public override fun release() {
        System.setOut(systemStream)
    }
}
