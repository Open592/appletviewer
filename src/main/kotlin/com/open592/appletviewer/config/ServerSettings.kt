package com.open592.appletviewer.config

/**
 * An individual server's settings
 *
 * @param id The server's ID
 *
 * @property config Applet viewer configuration items
 * @property parameters Applet parameters
 * @property content Localized content
 */
public class ServerSettings constructor(public val id: String = "") {
    private val config = mutableMapOf<String, String>()
    private val content = mutableMapOf<String, String>()
    private val parameters = mutableMapOf<String, String>()

    public fun getConfigValue(key: String): String {
        return config[key].orEmpty()
    }

    public fun getContentValue(key: String): String {
        return content[key].orEmpty()
    }

    public fun getParameterValue(key: String): String {
        return parameters[key].orEmpty()
    }

    public fun setConfigValue(key: String, value: String) {
        config[key] = value
    }

    public fun setContentValue(key: String, value: String) {
        content[key] = value
    }

    public fun setParameterValue(key: String, value: String) {
        parameters[key] = value
    }
}
