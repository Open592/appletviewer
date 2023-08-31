package com.open592.appletviewer.events

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GlobalEventBusTest {
    private data class TestApplicationEvent(val name: String) : ApplicationEvent

    private class IgnoredApplicationEvent : ApplicationEvent

    @Test
    fun `Should successfully dispatch a single TestEvent`() =
        runTest {
            val expectedName = "Test 123"
            val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
            val results = mutableListOf<String>()

            eventBus.listen<TestApplicationEvent> {
                results.add(it.name)
            }

            eventBus.dispatch(TestApplicationEvent(expectedName))

            assertEquals(1, results.size)
            assertEquals(expectedName, results.first())
        }

    @Test
    fun `Should continue to dispatch events when dispatching to nonexistent event listeners`() =
        runTest {
            val expectedValues = listOf("Dumb", "Dumber", "Dumbest")
            val eventBus = GlobalEventBus(TestScope(UnconfinedTestDispatcher(testScheduler)))
            val results = mutableListOf<String>()

            eventBus.listen<TestApplicationEvent> {
                results.add(it.name)
            }

            eventBus.dispatch(TestApplicationEvent(expectedValues[0]))
            eventBus.dispatch(IgnoredApplicationEvent())
            eventBus.dispatch(IgnoredApplicationEvent())
            eventBus.dispatch(IgnoredApplicationEvent())
            eventBus.dispatch(TestApplicationEvent(expectedValues[1]))
            eventBus.dispatch(TestApplicationEvent(expectedValues[2]))

            assertEquals(expectedValues, results)
        }
}
