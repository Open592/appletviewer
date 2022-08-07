package com.open592.appletviewer.viewer.event

import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
public class ViewerEventBus : EventBus<ViewerEvent>() {
    public fun dispatchQuitEvent() {
        scope.launch {
            emitEvent(ViewerEvent.Quit)
        }
    }
}
