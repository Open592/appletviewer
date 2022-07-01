package com.open592.debug.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class OutputCaptureTest {
    @Test
    fun singleCaptureTest() {
        val message = "test"
        val expected = "test\n"
        val capture = OutputCapture()

        println(message)

        val messages: List<String> = capture.getOut()

        assertEquals(0, capture.getErr().size, "Expected StdErr list to not contain any values")
        assertEquals(1, messages.size, "Expected StdOut to have one value")

        assertEquals(expected, messages[messages.lastIndex])
    }
}
