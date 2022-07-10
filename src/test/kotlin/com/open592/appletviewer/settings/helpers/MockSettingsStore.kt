package com.open592.appletviewer.settings.helpers

import com.open592.appletviewer.settings.SettingsStore

/**
 * A settings store which operates on a provided map of mock values
 */
class MockSettingsStore constructor(
    private val settings: Map<String, String>
) : SettingsStore {
    override fun getBoolean(key: String): Boolean {
        return getString(key).toBoolean()
    }

    override fun getString(key: String): String? {
        return settings[key]
    }

    override fun exists(key: String): Boolean {
        return settings.containsKey(key)
    }
}
