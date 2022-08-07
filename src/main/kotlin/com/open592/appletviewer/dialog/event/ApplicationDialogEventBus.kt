package com.open592.appletviewer.dialog.event

import com.open592.appletviewer.dialog.ApplicationDialogType
import com.open592.appletviewer.event.EventBus
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
public class ApplicationDialogEventBus : EventBus<ApplicationDialogEvent>() {
    public fun dispatchDisplayEvent(type: ApplicationDialogType, message: String) {
        scope.launch {
            emitEvent(ApplicationDialogEvent.Display(type, message))
        }
    }
}
