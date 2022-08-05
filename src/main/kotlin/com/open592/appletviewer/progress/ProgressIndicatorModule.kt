package com.open592.appletviewer.progress

import com.google.inject.AbstractModule
import com.open592.appletviewer.localization.LocalizationModule
import com.open592.appletviewer.progress.view.ProgressIndicatorComponent
import com.open592.appletviewer.progress.view.ProgressIndicatorView

public object ProgressIndicatorModule : AbstractModule() {
    public override fun configure() {
        install(LocalizationModule)

        bind(ProgressIndicatorView::class.java)
            .to(ProgressIndicatorComponent::class.java)
    }
}
