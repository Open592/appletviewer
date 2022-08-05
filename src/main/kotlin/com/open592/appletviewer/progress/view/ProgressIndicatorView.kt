package com.open592.appletviewer.progress.view

import java.awt.Frame

/**
 * Represents the user visible API of the ProgressIndicator
 */
public interface ProgressIndicatorView {
    /**
     * Initializes the ProgressIndicator component
     */
    public fun initialize(parentFrame: Frame)

    /**
     * Sets whether the ProgressIndicator is visible to the user or not
     */
    public fun changeVisibility(visible: Boolean)

    /**
     * Instructs the ProgressIndicator to update the text being displayed to the user
     */
    public fun changeText(text: String)

    /**
     * Tells the ProgressIndicator what progress to display within the progress bar
     */
    public fun setProgress(percentage: Int)
}
