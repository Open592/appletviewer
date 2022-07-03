package com.open592.appletviewer.debug.capture

import java.io.PrintStream

internal class PrintStreamCapture(
    interceptor: Interceptor,
    handler: (CaptureType, String) -> Unit
) : PrintStream(OutputStreamCapture(interceptor, handler))
