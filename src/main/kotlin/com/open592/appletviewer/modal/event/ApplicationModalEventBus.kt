package com.open592.appletviewer.modal.event

import com.open592.appletviewer.event.EventBus
import com.open592.appletviewer.modal.ApplicationModalType
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
public class ApplicationModalEventBus : EventBus<ApplicationModalEvent>() {
    public fun dispatchDisplayEvent(type: ApplicationModalType, message: String) {
        scope.launch {
            emitEvent(ApplicationModalEvent.Display(type, message))
        }
    }
}
