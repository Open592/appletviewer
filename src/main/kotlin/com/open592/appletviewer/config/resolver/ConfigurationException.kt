package com.open592.appletviewer.config.resolver

/**
 * Base of all exceptions thrown while loading the configuration.
 *
 * @param contentKey Specifies the localized content key to use when showing fatal error modal
 */
public sealed class ConfigurationException(public val contentKey: String, override val message: String) : Exception(message) {
    /**
     * We were not supplied with enough information to even find
     * the configuration.
     */
    public class MissingConfigurationException : ConfigurationException(contentKey = "err_missing_config", message = "Failed to find configuration")
    /**
     * We failed to resolve the configuration from its destination.
     */
    public class LoadConfigurationException : ConfigurationException(contentKey = "err_load_config", message = "Failed to load configuration")
    /**
     * We failed to parse the configuration
     */
    public class DecodeConfigurationException : ConfigurationException(contentKey = "err_decode_config", message = "Failed to decode configuration")
}
