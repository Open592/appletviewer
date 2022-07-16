package com.open592.appletviewer.debug.capture

import java.io.PrintStream

class DummyInterceptor constructor(
    type: CapturedMessagedType,
    systemStream: PrintStream
): Interceptor(type, systemStream) {
    private val messages: MutableList<CapturedMessage> = mutableListOf()

    override fun capture(stream: PrintStream) {
        when (type) {
            CapturedMessagedType.OUT -> System.setOut(stream)
            CapturedMessagedType.ERR -> System.setErr(stream)
        }
    }

    override fun release() {
        when (type) {
            CapturedMessagedType.OUT -> System.setOut(systemStream)
            CapturedMessagedType.ERR -> System.setErr(systemStream)
        }
    }

    override fun write(message: String) {
        messages.add(CapturedMessage(type, message))
    }

    override fun flush() {
        // Noop
    }

    fun getMessages(): List<String> {
        return messages.map {
            it.message
        }
    }
}
