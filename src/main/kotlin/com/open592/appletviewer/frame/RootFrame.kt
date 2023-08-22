package com.open592.appletviewer.frame

import com.open592.appletviewer.paths.ApplicationPaths
import java.awt.Frame
import java.awt.Toolkit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile

@Singleton
public class RootFrame @Inject constructor(
    private val applicationPaths: ApplicationPaths
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
        val path = applicationPaths.resolveGameFileDirectoryPath(ICON_FILE_NAME)

        if (path == null || !path.isReadable()) {
            return
        }

        frame.iconImage = Toolkit.getDefaultToolkit().getImage(path.toUri().toURL())
    }

    private companion object {
        private const val ICON_FILE_NAME = "jagexappletviewer.png"
    }
}
