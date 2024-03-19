package com.open592.appletviewer.jar

import okio.Buffer
import okio.buffer
import okio.sink
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipFile
import kotlin.io.path.createTempFile

/**
 * We rely on `JarInputStream` to verify that our remote jar files have not been
 * tampered with, and that they have been signed with the proper private keys. But
 * unfortunately it makes a lot of assumptions about the structure of the jar
 * file, and the naming of its entries. Because of that we are unable to
 * validate the original Jagex jar files as-is since they use a non-standard
 * entry order.
 *
 * In order to mitigate this we provide this helper function which attempts
 * to recreate the jar file using the standard entry order and naming
 * conventions expected by `JarInputStream`.
 *
 * @param jarBuffer A buffer pointing to a "Jagex Jar"
 *
 * @return Returns a `JarInputStream` with the standard structure and entry order
 */
public fun fixJagexJar(jarBuffer: Buffer): JarInputStream? {
    val jarFile = try {
        bufferToJarFile(jarBuffer)
    } catch (e: Exception) {
        return null
    }

    jarFile.use { jar ->
        // Initialize a jar in memory with the correct format.
        //
        // Will return `null` if our buffer is not pointing to a jar
        // which includes the files we expect from a Jagex jar.
        val buffer = Buffer()
        val jarStream = initializeStandardizedJar(jar, buffer) ?: return null

        // Now we need to populate the jar with the remaining entries
        jar.entries().asSequence().forEach { entry ->
            if (entry.name.startsWith("META-INF/", ignoreCase = true)) {
                return@forEach
            }

            jar.getJarEntry(entry.name)
            jarStream.putNextEntry(entry)
            jarStream.write(jar.getInputStream(entry).readAllBytes())
            jarStream.closeEntry()
        }

        return JarInputStream(buffer.inputStream())
    }
}

/**
 * Given a `Buffer` pointing to a jar file in memory, create a
 * temporary file to hold the data and return back a reference
 * to a `JarFile` so that we can randomly access the jar's
 * entries.
 *
 * This allows us to reorder the jar's entries.
 */
private fun bufferToJarFile(jarBuffer: Buffer): JarFile {
    val jar = createTempFile(prefix = null, suffix = ".jar")
    val jarSink = jar.sink().buffer()

    jarSink.writeAll(jarBuffer)
    jarSink.flush()

    return JarFile(jar.toFile(), true, TEMPORARY_JAR_OPEN_MODE)
}

/**
 * Given a `JarFile` pointing to a jar with the expected Jagex structure,
 * and a Buffer which will be the backing store for our `JarOutputStream`,
 * initialize a new jar, copying over the manifest files from the old
 * jar, but placing them in the expected order.
 *
 * The resulting jar will only be populated with the manifest files, all
 * other entries will need to be added at a later step.
 *
 * @return A `JarOutputStream` with a standardized format, or `null` if the
 * provided `JarFile` doesn't include the files we would expect from a Jagex
 * jar.
 */
private fun initializeStandardizedJar(jarFile: JarFile, buffer: Buffer): JarOutputStream? {
    val jarStream = JarOutputStream(buffer.outputStream())

    // Start with the manifest file (META-INF/manifest.mf)
    val manifestFile = jarFile.getJarEntry(JAGEX_JAR_MANIFEST_NAME) ?: return null
    jarStream.putNextEntry(JarEntry(JarFile.MANIFEST_NAME))
    jarStream.write(jarFile.getInputStream(manifestFile).readAllBytes())
    jarStream.closeEntry()

    // Next the signature file (META-INF/zigbert.sf)
    val signatureFile = jarFile.getJarEntry(JAGEX_JAR_SIGNATURE_FILE_NAME) ?: return null
    jarStream.putNextEntry(JarEntry(signatureFile.name.uppercase()))
    jarStream.write(jarFile.getInputStream(signatureFile).readAllBytes())
    jarStream.closeEntry()

    // Finally the signature itself (META-INF/zigbert.rsa)
    val signature = jarFile.getJarEntry(JAGEX_JAR_SIGNATURE_NAME) ?: return null
    jarStream.putNextEntry(JarEntry(signature.name.uppercase()))
    jarStream.write(jarFile.getInputStream(signature).readAllBytes())
    jarStream.closeEntry()

    return jarStream
}

private const val JAGEX_JAR_MANIFEST_NAME = "META-INF/manifest.mf"
private const val JAGEX_JAR_SIGNATURE_FILE_NAME = "META-INF/zigbert.sf"
private const val JAGEX_JAR_SIGNATURE_NAME = "META-INF/zigbert.rsa"

// The mode in which we will open the temporary jar file
private const val TEMPORARY_JAR_OPEN_MODE = ZipFile.OPEN_READ or ZipFile.OPEN_DELETE
