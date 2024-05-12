package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.jar.InMemoryClassLoader
import jakarta.inject.Inject

public class LoaderResolver @Inject constructor(
    private val dependencyFetcher: RemoteDependencyFetcher,
    private val configuration: ApplicationConfiguration,
    private val inMemoryClassLoader: InMemoryClassLoader,
) : RemoteDependencyResolver(configuration, type = DependencyType.LOADER) {
    override fun resolve() {
        val jarFile = dependencyFetcher.fetchRemoteDependency(
            type = DependencyType.LOADER,
            getUrl(URL_CONFIG_KEY),
        ) ?: throw ResolveException(configuration.getContent(RESOLVE_LOADER_ERROR_KEY))

        inMemoryClassLoader.initialize(jarFile)

        val applet = inMemoryClassLoader.loadClass("loader")

        println(applet)
    }

    private companion object {
        private const val URL_CONFIG_KEY = "loader_jar"
        private const val RESOLVE_LOADER_ERROR_KEY = "err_target_applet"
    }
}
