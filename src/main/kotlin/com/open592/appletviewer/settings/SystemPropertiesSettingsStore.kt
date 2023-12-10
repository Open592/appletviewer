package com.open592.appletviewer.settings

import jakarta.inject.Singleton

/**
 * A simple settings store which is backed by the JVM system properties
 *
 * The applet viewer heavily uses the JVM system properties for configuration
 * before the JavConfig file is fetched from the server.
 */
@Singleton
public class SystemPropertiesSettingsStore : SettingsStore {
    public override fun getBoolean(key: String): Boolean {
        return getString(key).toBoolean()
    }

    public override fun getString(key: String): String {
        return System.getProperty(key).orEmpty()
    }

    public override fun exists(key: String): Boolean {
        return getString(key).isNotEmpty()
    }
}
