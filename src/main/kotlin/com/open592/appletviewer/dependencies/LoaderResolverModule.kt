package com.open592.appletviewer.dependencies

import com.google.inject.AbstractModule
import com.open592.appletviewer.jar.InMemoryClassLoaderModule

public object LoaderResolverModule : AbstractModule() {
    override fun configure() {
        install(InMemoryClassLoaderModule)
    }
}
