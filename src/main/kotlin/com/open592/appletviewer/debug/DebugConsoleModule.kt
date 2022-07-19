package com.open592.appletviewer.debug

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import com.open592.appletviewer.debug.capture.Interceptor
import com.open592.appletviewer.debug.capture.SystemErrorInterceptor
import com.open592.appletviewer.debug.capture.SystemOutInterceptor
import com.open592.appletviewer.debug.view.DebugConsoleComponent
import com.open592.appletviewer.debug.view.DebugConsoleView
import com.open592.appletviewer.settings.SettingsStore
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore

public object DebugConsoleModule : AbstractModule() {
    override fun configure() {
        val binder = Multibinder.newSetBinder(binder(), Interceptor::class.java)
        binder.addBinding().to(SystemOutInterceptor::class.java)
        binder.addBinding().to(SystemErrorInterceptor::class.java)

        bind(SettingsStore::class.java).to(SystemPropertiesSettingsStore::class.java)

        bind(DebugConsoleView::class.java).to(DebugConsoleComponent::class.java)
    }
}
