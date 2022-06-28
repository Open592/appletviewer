package com.open592.settings

import kotlin.Boolean

/**
 * A simple settings store which is backed by the JVM system properties
 *
 * The applet viewer heavily uses the JVM system properties for configuration
 * before the JavConfig file is fetched from the server.
 */
public class JVMPropertiesSettingsStore : SettingsStore {
    public override fun getBoolean(key: String): Boolean {
        return getString(key).toBoolean().or(false)
    }

    public override fun getString(key: String): String? {
        return System.getProperty(key)
    }

    public override fun exists(key: String): Boolean {
        return getString(key) != null
    }
}
