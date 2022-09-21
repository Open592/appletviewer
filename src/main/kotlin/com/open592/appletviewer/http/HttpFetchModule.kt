package com.open592.appletviewer.http

import com.google.inject.AbstractModule
import com.google.inject.Scopes

public object HttpFetchModule : AbstractModule() {
    public override fun configure() {
        bind(HttpFetch::class.java)
            .toProvider(HttpFetchProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
