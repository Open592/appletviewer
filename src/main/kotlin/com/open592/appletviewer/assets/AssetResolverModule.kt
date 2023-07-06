package com.open592.appletviewer.assets

import com.google.inject.AbstractModule
import com.google.inject.Scopes

public object AssetResolverModule : AbstractModule() {
    override fun configure() {
        bind(ApplicationAssetResolver::class.java)
            .toProvider(ApplicationAssetResolverProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
