package com.open592.appletviewer.modal

import com.google.inject.AbstractModule
import com.open592.appletviewer.localization.LocalizationModule
import com.open592.appletviewer.modal.view.ApplicationModalComponent
import com.open592.appletviewer.modal.view.ApplicationModalView

public object ApplicationModalModule : AbstractModule() {
    public override fun configure() {
        install(LocalizationModule)

        bind(ApplicationModalView::class.java)
            .to(ApplicationModalComponent::class.java)
    }
}
