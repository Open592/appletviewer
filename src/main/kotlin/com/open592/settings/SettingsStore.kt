package com.open592.settings

/**
 * An interface for interacting with Key Value settings.
 *
 * Should provide an easy way to modify, at runtime, the applications' behavior.
 *
 * Examples could include:
 *   - Environment variables
 *   - JVM system properties
 */
public interface SettingsStore {
    /**
     * Given a string key, attempt to resolve the associated settings value as a Boolean
     *
     * @param key The key of the setting you are wishing to retrieve
     * @return `true` if the resolved settings value is truthy, otherwise `false`
     */
    public fun getBoolean(key: String): Boolean

    /**
     * Given a string key, attempt to resolve the associated settings value as a string
     *
     * @param key the Key of the setting you are wishing to retrieve
     * @return the setting if possible to resolve, otherwise `null` is returned
     */
    public fun getString(key: String): String?

    /**
     * Given a string key, determine if the setting can be resolved.
     *
     * @param key the key of the setting you are wishing to retrieve
     * @return `true` is the provided key can resolve a valid value, otherwise `false`
     */
    public fun exists(key: String): Boolean
}
