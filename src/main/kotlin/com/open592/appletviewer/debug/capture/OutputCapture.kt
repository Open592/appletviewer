package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton

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
    settings: SettingsStore,
    private val captures: Set<Capture>
) {
    init {
        val shouldLogToSystemStream = settings.getBoolean("com.open592.debugConsoleLogToSystemStream")

        captures.forEach {
            it.shouldLogToSystemStream = shouldLogToSystemStream
            it.capture(PrintStreamCapture(it))
        }
    }

    public fun release() {
        captures.forEach {
            it.release()
        }
    }
}
