package com.open592.appletviewer.root

import java.awt.Frame
import javax.inject.Provider

public class RootFrameProvider : Provider<Frame> {
    override fun get(): Frame {
        return Frame()
    }
}
