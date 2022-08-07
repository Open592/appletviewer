package com.open592.appletviewer.dialog

import com.google.inject.AbstractModule
import com.open592.appletviewer.dialog.view.ApplicationDialogComponent
import com.open592.appletviewer.dialog.view.ApplicationDialogView
import com.open592.appletviewer.localization.LocalizationModule

public object ApplicationDialogModule : AbstractModule() {
    public override fun configure() {
        install(LocalizationModule)

        bind(ApplicationDialogView::class.java)
            .to(ApplicationDialogComponent::class.java)
    }
}
