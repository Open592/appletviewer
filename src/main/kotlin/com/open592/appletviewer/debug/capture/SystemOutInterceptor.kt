package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.debug.DebugConsoleEvent
import com.open592.appletviewer.events.GlobalEventBus
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.PrintStream

@Singleton
public class SystemOutInterceptor
@Inject
constructor(
    private val eventBus: GlobalEventBus,
) : Interceptor(CapturedMessagedType.OUT, System.out) {
    public override fun capture(stream: PrintStream) {
        System.setOut(stream)
    }

    public override fun release() {
        System.setOut(systemStream)
    }

    public override fun write(message: String) {
        eventBus.dispatch(DebugConsoleEvent.MessageReceived(CapturedMessage(type, message)))

        // Defer to super class to decide if we should log to system stream
        super.write(message)
    }
}
