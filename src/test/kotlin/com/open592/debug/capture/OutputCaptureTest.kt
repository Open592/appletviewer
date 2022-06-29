package com.open592.debug.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class OutputCaptureTest {
    @Test
    fun singleCaptureTest() {
        val message = "test"
        val expected = "test${System.lineSeparator()}"
        val capture = OutputCapture()

        println(message)

        val captures: List<CapturedMessage> = capture.getOut()

        assertEquals(0, capture.getErr().size, "Expected StdErr list to not contain any values")
        assertEquals(1, captures.size, "Expected StdOut to have one value")

        assertEquals(expected, captures[captures.lastIndex].message)
    }
}
