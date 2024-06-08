package com.open592.appletviewer.jar

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

public object InMemoryClassLoaderModule : AbstractModule() {
    override fun configure() {
        install(FactoryModuleBuilder().build(InMemoryClassLoaderFactory::class.java))
    }
}
