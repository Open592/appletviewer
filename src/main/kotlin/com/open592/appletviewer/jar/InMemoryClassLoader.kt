package com.open592.appletviewer.jar

import com.google.inject.assistedinject.Assisted
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
    signedJarFileResolver: SignedJarFileResolver,
    @Assisted private val jarFile: Buffer,
) : ClassLoader() {
    private val resolvedClassCache: MutableMap<String, Class<*>> = mutableMapOf()
    private val protectionDomain: ProtectionDomain
    private val jarEntries: JarEntries

    init {
        // I feel like `CodeSource` is mistyped. It's documentation specifically specifies that
        // it's values can be nullable, but it has non-null types. Maybe this was an old Java
        // practise?
        val codeSource = CodeSource(null, null as Array<Certificate>?)
        val permissions = Permissions()

        permissions.add(AllPermission())

        protectionDomain = ProtectionDomain(codeSource, permissions)
        jarEntries = signedJarFileResolver.resolveEntries(jarFile)
    }

    override fun loadClass(name: String): Class<*> {
        if (resolvedClassCache.contains(name)) {
            return resolvedClassCache[name] as Class<*>
        }

        val bytes = jarEntries.getEntry("$name.class") ?: return super.findSystemClass(name)
        val byteArray = bytes.readByteArray()
        val resolvedClass = this.defineClass(name, byteArray, 0, byteArray.size, this.protectionDomain)

        resolvedClassCache[name] = resolvedClass

        return resolvedClass
    }
}
