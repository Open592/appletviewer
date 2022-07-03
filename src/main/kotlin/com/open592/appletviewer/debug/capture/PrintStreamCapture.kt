package com.open592.appletviewer.debug.capture

import java.io.PrintStream

internal class PrintStreamCapture(
    interceptor: Interceptor,
    handler: MessageCaptureHandler
) : PrintStream(OutputStreamCapture(interceptor, handler))
