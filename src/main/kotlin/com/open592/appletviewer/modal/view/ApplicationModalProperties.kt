package com.open592.appletviewer.modal.view

/**
 * The properties needed to display a modal to the user.
 */
public data class ApplicationModalProperties(
    public val content: List<String>,
    public val title: String,
    public val buttonText: String,
    public val closeAction: () -> Unit
)
