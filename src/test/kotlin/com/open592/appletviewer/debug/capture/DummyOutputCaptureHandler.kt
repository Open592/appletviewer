package com.open592.appletviewer.debug.capture

import com.open592.appletviewer.event.ApplicationEventListener
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

class DummyOutputCaptureHandler(
    eventBus: EventBus<OutputCaptureEvent>
) : ApplicationEventListener<OutputCaptureEvent>(eventBus) {
    private val captures: MutableList<CapturedMessage> = mutableListOf()

    override fun processEvent(event: OutputCaptureEvent) {
        when (event) {
            is OutputCaptureEvent.MessageReceived -> handleMessageReceived(event)
        }
    }

    fun get(): List<String> {
        return captures.map {
            it.message
        }
    }

    fun get(type: CapturedMessagedType): List<String> {
        return captures.filter { it.type == type }.map { it.message }
    }

    suspend fun waitForMessages(expectedMessageCount: Int, timeout: Long = 500L) {
        withTimeout(timeout) {
            while (true) {
                if (captures.size == expectedMessageCount) {
                    return@withTimeout
                }

                delay(timeout / 10)
            }
        }
    }

    private fun handleMessageReceived(event: OutputCaptureEvent.MessageReceived) {
        captures.add(event.capture)
    }
}
