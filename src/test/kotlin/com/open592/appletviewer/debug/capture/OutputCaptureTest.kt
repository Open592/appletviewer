package com.open592.appletviewer.debug.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class OutputCaptureTest {
    @Test
    fun singleMessageTest() {
        val input = "test"
        val expected = "$input\n"
        val capture = OutputCapture(setOf(SystemOutInterceptor(), SystemErrorInterceptor()))

        println(input)

        val messages: List<String> = capture.get(CaptureType.OUT)

        assertEquals(0, capture.get(CaptureType.ERR).size, "Expected CaptureType.ERR to be empty")
        assertEquals(1, messages.size, "Expected CaptureType.OUT to have 1 entry")

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

        val messages: List<String> = capture.get(CaptureType.OUT)

        assertEquals(1, messages.size, "Expected CaptureType.OUT have to have 1 entry")

        assertEquals(expected, messages[messages.lastIndex])
    }

    @Test
    fun multipleMessageTypesTest() {
        val first = "first"
        val second = "second"
        val expected = listOf("$first\n", "$second\n")
        val capture = OutputCapture(setOf(SystemOutInterceptor(), SystemErrorInterceptor()))

        println(first)
        System.err.println(second)

        assertEquals(1, capture.get(CaptureType.ERR).size, "Expected CaptureType.ERR to have 1 entry")
        assertEquals(1, capture.get(CaptureType.OUT).size, "Expected CaptureType.OUT to have 1 entry")
        assertEquals(expected, capture.get())
    }
}
