package com.open592.appletviewer.dependencies

/**
 * Represents the types of dependencies we resolve within the viewer.
 */
public enum class DependencyType {
    /**
     * The browsercontrol library is used to display advertisements
     * for non-members within the applet viewer header.
     */
    BROWSERCONTROL,

    /**
     * The loader is an Applet which is responsible for maintaining a list of client
     * dependencies and loading the client itself.
     */
    LOADER,
}
