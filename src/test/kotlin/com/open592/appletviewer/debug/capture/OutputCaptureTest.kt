package com.open592.appletviewer.debug.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class OutputCaptureTest {
    @Test
    fun singleMessageTest() {
        val interceptor = DummyInterceptor(CapturedMessagedType.OUT, System.out)
        val outputCapture = OutputCapture(setOf(interceptor))

        val input = "test"
        val expected = "$input\n"

        outputCapture.capture(shouldLogToSystemStream = false)

        println(input)

        val messages: List<String> = interceptor.getMessages()

        assertEquals(1, messages.size, "Expected CaptureType.OUT to have 1 entry")
        assertEquals(expected, messages[messages.lastIndex])
    }

    @Test
    fun multipleMessageTest() {
        val interceptor = DummyInterceptor(CapturedMessagedType.OUT, System.out)
        val outputCapture = OutputCapture(setOf(interceptor))

        val inputs = arrayOf("test", "one", "two", "three")
        val expected = "${inputs.joinToString("")}\n"

        outputCapture.capture(shouldLogToSystemStream = false)

        inputs.forEach {
            print(it)
        }

        println()

        val messages: List<String> = interceptor.getMessages()

        assertEquals(1, messages.size, "Expected CaptureType.OUT have to have 1 entry")
        assertEquals(expected, messages[messages.lastIndex])
    }

    @Test
    fun multipleMessageTypesTest() {
        val systemOutInterceptor = DummyInterceptor(CapturedMessagedType.OUT, System.out)
        val systemErrInterceptor = DummyInterceptor(CapturedMessagedType.ERR, System.err)
        val outputCapture = OutputCapture(setOf(systemOutInterceptor, systemErrInterceptor))

        val first = "first"
        val second = "second"
        val expected = listOf("$first\n", "$second\n")

        outputCapture.capture(shouldLogToSystemStream = false)

        println(first)
        System.err.println(second)

        assertEquals(1, systemOutInterceptor.getMessages().size, "Expected CaptureType.ERR to have 1 entry")
        assertEquals(1, systemErrInterceptor.getMessages().size, "Expected CaptureType.OUT to have 1 entry")
        assertEquals(expected[0], systemOutInterceptor.getMessages().first())
        assertEquals(expected[1], systemErrInterceptor.getMessages().first())
    }
}
