package com.open592.appletviewer.debug.capture

/**
 * Represents the types of messages supported within the OutputCapture class.
 *
 * The original appletviewer supported intercepting StdErr and StdOut, and we
 * support the same functionality.
 */
public enum class CaptureType {
    ERR,
    OUT,
}
