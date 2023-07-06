package com.open592.appletviewer.assets.resolver

import com.google.inject.AbstractModule

object AssetResolverModule : AbstractModule() {
    override fun configure() {
        install()
    }
}
