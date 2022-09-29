package com.open592.appletviewer.fetch

import com.google.inject.AbstractModule
import com.google.inject.Scopes

public object AssetFetchModule : AbstractModule() {
    public override fun configure() {
        bind(AssetFetch::class.java)
            .toProvider(AssetFetchProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
