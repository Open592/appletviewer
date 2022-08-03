package com.open592.appletviewer.viewer.event

import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
public class ViewerEventBus : EventBus<ViewerEvent>() {
    private val scope = CoroutineScope(Dispatchers.Default)

    public fun dispatchQuitEvent() {
        scope.launch {
            emitEvent(ViewerEvent.Quit)
        }
    }
}
