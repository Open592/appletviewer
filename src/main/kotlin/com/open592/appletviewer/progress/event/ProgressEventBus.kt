package com.open592.appletviewer.progress.event

import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class ProgressEventBus : EventBus<ProgressEvent>() {
    private val scope = CoroutineScope(Dispatchers.Default)

    public fun dispatchChangeVisibilityEvent(visible: Boolean) {
        scope.launch {
            emitEvent(ProgressEvent.ChangeVisibility(visible))
        }
    }

    public fun dispatchChangeTextEvent(text: String) {
        scope.launch {
            emitEvent(ProgressEvent.ChangeText(text))
        }
    }

    public fun dispatchUpdateProgressEvent(percentage: Int) {
        scope.launch {
            emitEvent(ProgressEvent.UpdateProgress(percentage))
        }
    }
}
