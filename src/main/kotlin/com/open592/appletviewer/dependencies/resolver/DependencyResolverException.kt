package com.open592.appletviewer.dependencies.resolver

/**
 * Represents errors encountered while resolving viewer dependencies.
 */
public sealed class DependencyResolverException(override val message: String) : Exception(message) {
    /**
     * We failed to fetch the remote dependency.
     *
     * @param filename The name of the file we were attempting to download.
     */
    public class FetchDependencyException(
        public val filename: String,
    ) : DependencyResolverException("Failed to download $filename")

    /**
     * We failed to validate the remote dependency.
     *
     * An example when this exception would be thrown would be when we fail
     * to verify a signed jar file.
     */
    public class VerifyDependencyException : DependencyResolverException("Unable to verify dependency.")
}
