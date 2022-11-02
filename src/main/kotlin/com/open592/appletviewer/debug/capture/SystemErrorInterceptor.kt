package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.debug.DebugConsoleEvent
import com.open592.appletviewer.events.GlobalEventBus
import java.io.PrintStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SystemErrorInterceptor @Inject constructor(
    private val eventBus: GlobalEventBus
) : Interceptor(CapturedMessagedType.ERR, System.err) {
    public override fun capture(stream: PrintStream) {
        System.setErr(stream)
    }

    public override fun release() {
        System.setErr(systemStream)
    }

    public override fun write(message: String) {
        eventBus.dispatch(DebugConsoleEvent.MessageReceived(CapturedMessage(type, message)))

        // Defer to super class to determine if we should log to system stream
        super.write(message)
    }
}
