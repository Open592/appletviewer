package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration

public abstract class RemoteDependencyResolver(
    private val configuration: ApplicationConfiguration,
) {
    public abstract fun resolve()

    public data class ResolveException(public override val message: String) : Exception(message)

    protected fun getUrl(configKey: String): String {
        val codebaseUrl = getCodebaseUrl()
        val filename = configuration.getConfig(configKey)

        return "$codebaseUrl$filename"
    }

    private fun getCodebaseUrl(): String {
        return configuration.getConfig("codebase")
    }
}
