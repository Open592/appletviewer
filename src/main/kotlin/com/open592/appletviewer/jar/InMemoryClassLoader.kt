package com.open592.appletviewer.jar

import jakarta.inject.Inject
import okio.Buffer
import java.security.AllPermission
import java.security.CodeSource
import java.security.Permissions
import java.security.ProtectionDomain
import java.security.cert.Certificate

/**
 * Given a `Buffer` pointing to a jar file, provide a `ClassLoader`
 * interface to that jar file.
 */
public class InMemoryClassLoader @Inject constructor(
    private val signedJarFileResolver: SignedJarFileResolver,
) : ClassLoader() {
    private val classCache: MutableMap<String, Class<*>> = mutableMapOf()
    private val protectionDomain: ProtectionDomain

    private lateinit var jarEntries: JarEntries

    init {
        // I feel like `CodeSource` is mistyped. It's documentation specifically specifies that
        // it's values can be nullable, but it has non-null types. Maybe this was an old Java
        // practise?
        val codeSource = CodeSource(null, null as Array<Certificate>?)
        val permissions = Permissions()

        permissions.add(AllPermission())

        protectionDomain = ProtectionDomain(codeSource, permissions)
    }

    public fun initialize(jarFile: Buffer) {
        assert(!isInitialized()) {
            "Invalid attempt to initialize InMemoryJarFileClassLoader more than once!"
        }

        jarEntries = signedJarFileResolver.resolveEntries(jarFile)
    }

    override fun loadClass(name: String): Class<*> {
        assert(isInitialized()) {
            "Invalid attempt to load class from InMemoryJarFileClassLoader before calling initialize()!"
        }

        if (classCache.contains(name)) {
            return classCache[name] as Class<*>
        }

        val bytes = jarEntries.getEntry("$name.class") ?: return super.findSystemClass(name)
        val byteArray = bytes.readByteArray()
        val klass = this.defineClass(name, byteArray, 0, byteArray.size, this.protectionDomain)

        classCache[name] = klass

        return klass
    }

    private fun isInitialized(): Boolean {
        return this::jarEntries.isInitialized
    }
}
