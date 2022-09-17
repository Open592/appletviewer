package com.open592.appletviewer.config

import com.open592.appletviewer.config.language.SupportedLanguage
import com.open592.appletviewer.config.resolver.ConfigurationResolver
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
public class ApplicationConfigurationProvider @Inject constructor(
    private val resolver: ConfigurationResolver,
    private val language: SupportedLanguage,
) : Provider<ApplicationConfiguration> {
    /**
     * Resolver will attempt to fetch the JavConfig from either a remote URL
     * or the filesystem.
     *
     * In the case of errors we throw an exception and the caller must handle
     * this by throwing a fatal error.
     */
    public override fun get(): ApplicationConfiguration {
        val config = resolver.resolve()

        return ApplicationConfiguration(config, language)
    }
}
