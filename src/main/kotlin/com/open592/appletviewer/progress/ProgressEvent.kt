package com.open592.appletviewer.progress

import com.open592.appletviewer.events.ApplicationEvent

/**
 * Represents events handled by the ProgressIndicator component.
 */
public sealed interface ProgressEvent : ApplicationEvent {
    /**
     * Fired when we want to change the visibility of the ProgressIndicator
     */
    public data class ChangeVisibility(val visible: Boolean) : ProgressEvent

    /**
     * Fired when we want to change the text being displayed within the ProgressIndicator
     */
    public data class ChangeText(val text: String) : ProgressEvent

    /**
     * Fired when we want to update the progress percentage of the ProgressIndicator
     */
    public data class UpdateProgress(val percentage: Int) : ProgressEvent
}
