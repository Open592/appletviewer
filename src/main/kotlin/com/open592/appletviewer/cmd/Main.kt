package com.open592.appletviewer.cmd

import com.google.inject.Guice
import com.open592.appletviewer.viewer.Viewer
import com.open592.appletviewer.viewer.ViewerModule
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

/*
 * Entry point of the AppletViewer
 *
 * In the original Applet Viewer a single argument was required which represented the "game" name. This was to allow
 * for serving multiple games.
 *
 * We only support a single game, but will allow for the previous calling convention as well as not passing any
 * arguments.
 *
 * This entry point is called from the launcher.
 * TODO: Add reference to the launcher calling code.
 */
public object Main {
    private const val GAME_NAME = "runescape"

    @JvmStatic
    public fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Invalid arguments")

            // Using the same status code as the original applet viewer
            exitProcess(0)
        }

        if (args.isNotEmpty()) {
            val gameName = args[0]

            if (!gameName.equals(GAME_NAME, true)) {
                println("Received $gameName, but only $GAME_NAME is supported. Exiting...")

                exitProcess(1)
            }
        }

        runBlocking {
            val viewer = Guice.createInjector(ViewerModule).getInstance(Viewer::class.java)
            viewer.initialize()
        }
    }
}
