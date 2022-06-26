package com.open592.appletviewer.cmd

import kotlin.system.exitProcess

private object Constants {
    const val SupportedGameName = "runescape"
}

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
fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Invalid arguments")

        // Using the same status code as the original applet viewer
        exitProcess(0)
    }

    if (args.isNotEmpty()) {
        val gameName = args[0]

        if (!gameName.equals(Constants.SupportedGameName, true)) {
            println("Received $gameName, but only ${Constants.SupportedGameName} is supported. Exiting...")

            exitProcess(1)
        }
    }

    println("Hello world!")
}
