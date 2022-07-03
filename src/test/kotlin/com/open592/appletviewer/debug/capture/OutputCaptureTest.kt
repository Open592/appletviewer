package com.open592.appletviewer.debug.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class OutputCaptureTest {
    @Test
    fun singleMessageTest() {
        val input = "test"
        val expected = "test\n"
        val capture = OutputCapture(setOf(SystemOutInterceptor(), SystemErrorInterceptor()))

        println(input)

        val messages: List<String> = capture.getOut()

        assertEquals(0, capture.getErr().size, "Expected StdErr list to not contain any values")
        assertEquals(1, messages.size, "Expected StdOut to have one value")

        assertEquals(expected, messages[messages.lastIndex])
    }

    @Test
    fun multipleMessageTest() {
        val inputs = arrayOf("test", "one", "two", "three")
        val expected = "${inputs.joinToString("")}\n"
        val capture = OutputCapture(setOf(SystemOutInterceptor(), SystemErrorInterceptor()))

        inputs.forEach {
            print(it)
        }

        println()

        val messages: List<String> = capture.getOut()

        assertEquals(1, messages.size, "Expected StdOut to have one value")

        assertEquals(expected, messages[messages.lastIndex])
    }
}
