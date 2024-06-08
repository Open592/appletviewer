package com.open592.appletviewer.dependencies

import com.google.inject.AbstractModule

public object ViewerDependenciesModule : AbstractModule() {
    override fun configure() {
        install(LoaderResolverModule)
    }
}
