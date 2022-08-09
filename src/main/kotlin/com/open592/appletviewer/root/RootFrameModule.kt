package com.open592.appletviewer.root

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import java.awt.Frame

public object RootFrameModule : AbstractModule() {
    public override fun configure() {
        bind(Frame::class.java)
            .annotatedWith(Root::class.java)
            .toProvider(RootFrameProvider::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
