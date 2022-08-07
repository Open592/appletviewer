package com.open592.appletviewer.dialog.view

import com.open592.appletviewer.dialog.ApplicationDialogType

public data class DialogProperties(
    public val type: ApplicationDialogType,
    public val lines: List<String>,
    public val title: String,
    public val buttonText: String,
    public val closeAction: () -> Unit
)
