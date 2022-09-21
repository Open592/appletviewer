package com.open592.appletviewer.config.javconfig

/**
 * Within the JavConfig file there can exist multiple "servers"
 * which override the base configuration.
 *
 * This class represents a single server (as well as the root server)
 *
 * Each server is initialized with an "ID" which is used to identity the
 * server within the JavConfig overrides map. The root server has an
 * empty ID since it does not exist within the map.
 *
 * @property config Applet viewer configuration items
 * @property parameters Applet parameters
 * @property content Localized content
 */
public class ServerConfiguration constructor(public val id: String = "") {
    private val config = mutableMapOf<String, String>()
    private val content = mutableMapOf<String, String>()
    private val parameters = mutableMapOf<String, String>()

    /**
     * We explicitly create named setters and getters to create a cleaner
     * interface within the parent class
     */
    public fun getConfig(key: String): String {
        return config[key].orEmpty()
    }

    public fun getContent(key: String): String {
        return content[key].orEmpty()
    }

    public fun getParameter(key: String): String {
        return parameters[key].orEmpty()
    }

    public fun setConfig(key: String, value: String) {
        config[key] = value
    }

    public fun setContent(key: String, value: String) {
        content[key] = value
    }

    public fun setParameter(key: String, value: String) {
        parameters[key] = value
    }
}
