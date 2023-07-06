package com.open592.appletviewer.environment

import com.google.inject.AbstractModule
import com.google.inject.Scopes

public object EnvironmentModule : AbstractModule() {
    override fun configure() {
        bind(Environment::class.java)
            .toProvider(EnvironmentProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
