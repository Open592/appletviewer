package com.open592.appletviewer.config.resolver

import java.lang.Exception

/**
 * Base of all exceptions thrown while loading the configuration.
 *
 * @param contentKey Specifies the localized content key to use when showing fatal error modal
 */
public sealed class JavConfigResolveException(public val contentKey: String, override val message: String) : Exception(
    message
) {
    /**
     * We were not supplied with enough information to even find
     * the configuration.
     */
    public class MissingConfigurationException : JavConfigResolveException(
        contentKey = "err_missing_config",
        message = "Failed to find configuration"
    )

    /**
     * We failed to resolve the configuration from its destination.
     */
    public class LoadConfigurationException : JavConfigResolveException(
        contentKey = "err_load_config",
        message = "Failed to load configuration"
    )

    /**
     * We failed to parse the configuration
     */
    public class DecodeConfigurationException : JavConfigResolveException(
        contentKey = "err_decode_config",
        message = "Failed to decode configuration"
    )
}
