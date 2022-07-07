package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class OutputCaptureTest {
    @Test
    fun singleMessageTest() {
        runTest {
            val eventBus = EventBus<OutputCaptureEvent>()
            val eventHandler = DummyOutputCaptureHandler(eventBus)
            OutputCapture(setOf(SystemOutInterceptor()), eventBus)

            val input = "test"
            val expected = "$input\n"

            println(input)

            withContext(Dispatchers.Default) {
                eventHandler.waitForMessages(1)
            }

            val messages: List<String> = eventHandler.get(CaptureType.OUT)

            assertEquals(1, messages.size, "Expected CaptureType.OUT to have 1 entry")
            assertEquals(0, eventHandler.get(CaptureType.ERR).size, "Expected CaptureType.ERR to have 0 entries")

            assertEquals(expected, messages[messages.lastIndex])
        }
    }
    @Test
    fun multipleMessageTest() {
        runTest {
            val eventBus = EventBus<OutputCaptureEvent>()
            val eventHandler = DummyOutputCaptureHandler(eventBus)
            OutputCapture(setOf(SystemOutInterceptor()), eventBus)

            val inputs = arrayOf("test", "one", "two", "three")
            val expected = "${inputs.joinToString("")}\n"

            inputs.forEach {
                print(it)
            }

            println()

            withContext(Dispatchers.Default) {
                eventHandler.waitForMessages(1)
            }

            val messages: List<String> = eventHandler.get(CaptureType.OUT)

            assertEquals(1, messages.size, "Expected CaptureType.OUT have to have 1 entry")

            assertEquals(expected, messages[messages.lastIndex])
        }
    }

    @Test
    fun multipleMessageTypesTest() {
        runTest {
            val eventBus = EventBus<OutputCaptureEvent>()
            val eventHandler = DummyOutputCaptureHandler(eventBus)
            OutputCapture(setOf(SystemErrorInterceptor(), SystemOutInterceptor()), eventBus)

            val first = "first"
            val second = "second"
            val expected = listOf("$first\n", "$second\n")

            println(first)
            System.err.println(second)

            withContext(Dispatchers.Default) {
                eventHandler.waitForMessages(expected.size)
            }

            assertEquals(1, eventHandler.get(CaptureType.ERR).size, "Expected CaptureType.ERR to have 1 entry")
            assertEquals(1, eventHandler.get(CaptureType.OUT).size, "Expected CaptureType.OUT to have 1 entry")
            assertEquals(expected, eventHandler.get())
        }
    }
}
