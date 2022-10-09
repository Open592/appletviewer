package com.open592.appletviewer.frame

import com.open592.appletviewer.fetch.AssetFetch
import java.awt.Frame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class RootFrame @Inject constructor(
    fetch: AssetFetch
) {
    private val frame = Frame()

    // On load attempt to set the icon image
    init {
        val icon = fetch.fetchLocaleImage(ICON_FILE_NAME)

        if (icon != null) {
            frame.iconImage = icon
        }
    }

    public fun getFrame(): Frame {
        return frame
    }

    private companion object {
        private const val ICON_FILE_NAME = "jagexappletviewer.png"
    }
}
