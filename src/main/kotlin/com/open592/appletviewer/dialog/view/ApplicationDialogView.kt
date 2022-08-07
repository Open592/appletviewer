package com.open592.appletviewer.dialog.view

/**
 * Represents the user visible API of the ApplicationDialog
 */
public interface ApplicationDialogView {
    /**
     * Closes the dialog
     */
    public fun close()

    /**
     * Displays a modal dialog with the provided properties
     */
    public fun display(properties: DialogProperties)
}
