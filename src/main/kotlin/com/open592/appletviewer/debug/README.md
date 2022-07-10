# Debug Console

## Introduction
The applet viewer provides the ability to display both `Stdout` and `stderr`
logs within a separate window, instead of directly within the console.

![Screenshot of debug console](https://www.dropbox.com/s/0fweulsbz8e0z3o/jagex-debug-console.png?raw=1)

## Functionality
When `com.jagex.debug` is enabled the debug host console will hook into both
`StdErr` and `StdOut` and intercept everything sent to both of them. All this
will be displayed in the separate host window.

## Changes in Open592
As always, we attempt to keep the default behavior as close to the original as
possible, and all changes are stored behind new flags

### Ability to disable debug console in debug mode
In the original applet viewer, the debug console was behind the
`com.jagex.debug` JVM system property. This required the use of the debug
console anytime you wanted additional debug information.

Within Open592 we add the `com.open592.disableDebugConsole` flag which lets you
disable the debug console when it would otherwise be shown.

### Ability to log to both system stream and debug console.
In the original applet viewer, when enabled, the debug console forced everything
sent to StdErr and StdOut into the debug console. In Open592 we add the
`com.open592.debugConsoleLogToSystemStream` flag which specifies that
we should continue logging to the system stream, even when the debug console
is active.

This is helpful given that the debug console can sometimes be closed
unexpectedly without providing time to go over the logs.
