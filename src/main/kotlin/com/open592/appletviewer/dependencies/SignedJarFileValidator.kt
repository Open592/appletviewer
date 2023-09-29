package com.open592.appletviewer.dependencies

import okio.Buffer
import okio.buffer
import okio.sink
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import kotlin.io.path.createTempFile

public class SignedJarFileValidator(jar: JarInputStream) {
    public companion object {
        /**
         * The validations present within the original applet viewer were largely hand
         * rolled, and includes:
         *
         * - Manually parsing all the manifest files
         * - Manual `PKCS7` initialization and validation
         * - Manual certificate inspection.
         *
         * We attempt to preserve the same level of validation, including supporting
         * the original jar files released by Jagex during the 592 release.
         *
         * In order to do this we rely on `JarInputStream` to perform the validations
         * for us. But unfortunately it makes a lot of assumptions about the structure
         * of the jar file, and the naming of its entries. Because of that we are
         * unable to validate the original Jagex jar files as-is since they use a
         * non-standard entry order.
         *
         * To mitigate the above, if we encounter a jar file which `JarInputStream` is
         * unable to validate. We will attempt to recreate a new jar using the standard
         * naming and structure.
         */
        public fun load(jarBuffer: Buffer): SignedJarFileValidator {
            /**
             * `JarInputStream` is going to read a segment off the top of the `Buffer`.
             * To allow us to later read from the beginning of the buffer if we encounter
             * a non-standard jar we need to clone the Buffer.
             */
            val bufferClone = jarBuffer.clone()
            val originalJarInputStream = JarInputStream(jarBuffer.inputStream())

            /**
             * Internally `JarInputStream` will attempt to read in the `MANIFEST.MF`
             * file from the jar. It first skips `META-INF/` the expects `MANIFEST.MF`
             * as the second file. If it finds a valid manifest file at that location
             * it will parse it and store it.
             *
             * If we are able to find it, we can be confident that the jar we are
             * working with is structured properly. Further validation will confirm
             * it's signed with either our own, or Jagex's private keys.
             */
            if (originalJarInputStream.manifest != null) {
                /**
                 * Since we don't need the duplicated `Buffer` let `okio` handle
                 * recycling its resources.
                 */
                bufferClone.clear()

                return SignedJarFileValidator(originalJarInputStream)
            }

            /**
             * At this point we have a non-standard jar file, and need to modify
             * it to make `JarInputStream` happy.
             */
            val jarFile = getJarFile(bufferClone)

            return SignedJarFileValidator(JarInputStream(jarBuffer.inputStream()))
        }

        private fun getJarFile(jarBuffer: Buffer): JarFile {
            val jar = createTempFile(prefix = null, suffix = ".jar")
            val jarSink = jar.sink().buffer()

            jarSink.writeAll(jarBuffer)
            jarSink.flush()

            return JarFile(jar.toFile())
        }
    }
}
