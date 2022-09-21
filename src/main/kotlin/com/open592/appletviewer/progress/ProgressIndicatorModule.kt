package com.open592.appletviewer.progress

import com.google.inject.AbstractModule
import com.open592.appletviewer.config.ApplicationConfigurationModule
import com.open592.appletviewer.progress.view.ProgressIndicatorComponent
import com.open592.appletviewer.progress.view.ProgressIndicatorView

public object ProgressIndicatorModule : AbstractModule() {
    public override fun configure() {
        install(ApplicationConfigurationModule)

        bind(ProgressIndicatorView::class.java)
            .to(ProgressIndicatorComponent::class.java)
    }
}
