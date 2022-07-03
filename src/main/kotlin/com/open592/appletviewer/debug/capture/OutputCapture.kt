package com.open592.appletviewer.debug.capture

import javax.inject.Inject
import javax.inject.Singleton

internal typealias MessageCaptureHandler = (CaptureType, String) -> Unit

/**
 * Provides easy access to PrintStreams
 *
 * We use this class to facilitate catching messages sent to the standard output streams
 * and displaying them within the debug console. In the original appletviewer, when this
 * feature was enabled, there was no choice but to intercept the messages, but in Open592's
 * implementation we have the option of passing them along to the underlying system stream.
 *
 * NOTE: Due to keeping the same behavior as the original implementation, we do not publish
 * a message until we receive a line separator.
 */
@Singleton
public class OutputCapture @Inject constructor(
    private val interceptors: Set<Interceptor>
) {
    private val messages: MutableList<CapturedMessage> = mutableListOf()

    init {
        interceptors.forEach {
            it.intercept(PrintStreamCapture(it, ::capture))
        }
    }

    public fun release() {
        interceptors.forEach {
            it.release()
        }
    }

    public fun get(type: CaptureType): List<String> {
        return messages.filter { it.type == type }.map { it.message }
    }

    public fun get(): List<String> {
        return messages.map { it.message }
    }

    private fun capture(type: CaptureType, message: String) {
        messages.add(CapturedMessage(type, message))
    }
}
