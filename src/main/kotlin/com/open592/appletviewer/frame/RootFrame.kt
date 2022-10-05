package com.open592.appletviewer.frame

import com.open592.appletviewer.fetch.AssetFetch
import java.awt.Frame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class RootFrame @Inject constructor(
    private val fetch: AssetFetch
) {
    private val frame = Frame()

    public fun getFrame(): Frame {
        return frame
    }
}
