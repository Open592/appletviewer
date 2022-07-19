package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.debug.event.DebugConsoleEventBus
import java.io.PrintStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SystemOutInterceptor @Inject constructor(
    private val eventBus: DebugConsoleEventBus
) : Interceptor(CapturedMessagedType.OUT, System.out) {
    public override fun capture(stream: PrintStream) {
        System.setOut(stream)
    }

    public override fun release() {
        System.setOut(systemStream)
    }

    public override fun write(message: String) {
        eventBus.dispatchMessageReceived(CapturedMessage(type, message))

        // Defer to super class to decide if we should log to system stream
        super.write(message)
    }
}
