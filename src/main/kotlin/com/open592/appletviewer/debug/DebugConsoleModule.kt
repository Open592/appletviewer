package com.open592.appletviewer.debug

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import com.open592.appletviewer.debug.capture.Interceptor
import com.open592.appletviewer.settings.SettingsStore
import com.open592.appletviewer.settings.SystemPropertiesSettingsStore

public class DebugConsoleModule : AbstractModule() {
    override fun configure() {
        val binder = Multibinder.newSetBinder(binder(), Interceptor::class.java)
        binder.addBinding().to(SystemOutInterceptor::class.java)
        binder.addBinding().to(SystemErrorInterceptor::class.java)

        bind(SettingsStore::class.java).to(SystemPropertiesSettingsStore::class.java)
    }
}
