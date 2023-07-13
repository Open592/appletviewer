package com.open592.appletviewer.frame

import com.open592.appletviewer.fetch.AssetFetch
import java.awt.Frame
import java.awt.Toolkit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

@Singleton
public class RootFrame @Inject constructor(
    private val fetch: AssetFetch
) {
    private val frame = Frame()

    init {
        resolveIconImage()
    }

    public fun getFrame(): Frame {
        return frame
    }

    /**
     * Attempt to resolve the icon image from the game directory.
     */
    private fun resolveIconImage() {
        val path = fetch.resolveGameFileDirectoryPath(ICON_FILE_NAME)

        if (path.notExists() || !path.isRegularFile() || !path.isReadable()) {
            return
        }

        frame.iconImage = Toolkit.getDefaultToolkit().getImage(path.toUri().toURL())
    }

    private companion object {
        private const val ICON_FILE_NAME = "jagexappletviewer.png"
    }
}
