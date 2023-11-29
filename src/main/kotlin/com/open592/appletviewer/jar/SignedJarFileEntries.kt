package com.open592.appletviewer.jar

import com.open592.appletviewer.dependencies.resolver.JAGEX_PUBLIC_KEYS
import com.open592.appletviewer.dependencies.resolver.ORIGINAL_JAGEX_CERTIFICATE_SERIAL_NUMBER
import com.open592.appletviewer.dependencies.resolver.ORIGINAL_THAWTE_CERTIFICATE_SERIAL_NUMBER
import com.open592.appletviewer.dependencies.resolver.THAWTE_PUBLIC_KEYS
import okio.Buffer
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.jar.JarInputStream
import java.util.jar.Manifest

/**
 * The validations present within the original applet viewer were largely hand
 * rolled, and included:
 *
 * - Manually parsing all the manifest files
 * - Manual `PKCS7` initialization and validation
 * - Manual certificate inspection.
 *
 * We attempt to preserve the same level of validation, including supporting
 * the original jar files released by Jagex during the 592 release.
 *
 * In order to do this we rely on `JarInputStream` to perform the validations
 * for us, throwing an exception if the jar file has been tampered with in
 * any way that it can detect.
 *
 * After we have loaded the in-memory jar buffer into the `JarInputStream`
 * and it's been validated, we just need to verify that the private key used
 * to sign the jar was either ours, or Jagex's. If that is true then we can
 * provide a single public method which returns the file bytes of a given
 * entry.
 *
 * This provides a much more efficient implementation, since we don't need to
 * perform verifications for every single request for a file from the jar.
 */
public data class SignedJarFileEntries(
    private val entries: Map<String, Buffer>,
) {
    /**
     * Given a filename, attempt to find it within the jar file's entries,
     * returning it's `Buffer` if it exists.
     *
     * @param filename The filename of an entry which is assumed to exist
     * within the jar.
     *
     * @return A `Buffer` to the file's contents.
     */
    public fun getEntry(filename: String): Buffer? {
        return entries[filename]
    }

    public companion object {
        /**
         * Given a buffer pointing to a jar file, read the buffer, validating
         * its content, and store its entries for later access.
         *
         * @param jarBuffer A buffer pointing to a jar file.
         *
         * @return A `SignedJarFileEntries` if the jar file could be validated.
         */
        public fun readAndValidate(jarBuffer: Buffer): SignedJarFileEntries? {
            val jar = initializeJarInputStream(jarBuffer) ?: return null
            val signatureFile = Manifest()
            val certificates: MutableList<X509Certificate> = mutableListOf()
            val entries: MutableMap<String, Buffer> = mutableMapOf()

            generateSequence { jar.nextJarEntry }.forEach { entry ->
                val buffer = Buffer().readFrom(jar)

                when (entry.name) {
                    "META-INF/ZIGBERT.SF" -> signatureFile.read(buffer.inputStream())
                    "META-INF/ZIGBERT.RSA" -> certificates.addAll(collectCertificates(buffer))
                    else -> entries[entry.name] = buffer
                }
            }

            if (!validateCertificates(certificates)) {
                return null
            }

            return SignedJarFileEntries(entries)
        }

        /**
         * When working with jar files we want to utilize some of the validation
         * logic within the standard `JarInputStream` class. This is complicated
         * because of how this class makes assumptions about the overall
         * structure of the jar file, along with the naming of its entries.
         * Due to this we need to perform some additional work when encountering
         * the original Jagex jar files which use a non-standard entry order and
         * naming pattern. If we were to pass these "Jagex" jar files directly
         * to `JarInputStream` it would encounter an unexpected file and short
         * circuit its jar validation logic which we don't want.
         *
         * To mitigate the above we check if `JarInputStream` was unable to load
         * the jar manifest, and if so, we attempt to recreate the jar using
         * the standard naming and structure. The resulting `JarInputStream`
         * will then be passed to our verification helper to perform
         * additional checks.
         */
        private fun initializeJarInputStream(jarBuffer: Buffer): JarInputStream? {
            /**
             * `JarInputStream` is going to read a segment off the top of the `Buffer`.
             * To allow us to later read from the beginning of the buffer if we encounter
             * a non-standard jar we need to clone the Buffer.
             */
            val bufferClone = jarBuffer.clone()
            val jarStream = JarInputStream(jarBuffer.inputStream())

            /**
             * Internally `JarInputStream` will attempt to read in the `MANIFEST.MF`
             * file from the jar. It first skips `META-INF/` then expects `MANIFEST.MF`
             * as the second file. If it finds a valid manifest file at that location
             * it will parse it and store it.
             *
             * If we are able to find it, we can assume that the jar we are
             * working with is structured properly. Further validation will confirm
             * it's signed with either our own, or Jagex's private keys.
             */
            if (jarStream.manifest != null) {
                /**
                 * Since we don't need the duplicated `Buffer` let `okio` handle
                 * recycling its resources.
                 */
                bufferClone.clear()

                return jarStream
            }

            /**
             * At this point we have a jar file which is not structured according
             * to rules expected by `JarInputStream`. We need to restructure the
             * jar so that it can be properly read.
             *
             * This is an exceptional case, and most likely will only be
             * encountered then working with original Jagex jars from the 592
             * era.
             */
            return fixJagexJar(bufferClone)
        }

        private fun collectCertificates(buffer: Buffer): List<X509Certificate> {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificates: MutableList<X509Certificate> = mutableListOf()

            certificateFactory.generateCertificates(buffer.inputStream()).forEach {
                certificates.add(it as X509Certificate)
            }

            return certificates
        }

        /**
         * The original applet viewer had some rudimentary certificate
         * verification which included:
         *
         * 1. Checking the number of certificates present within the signed jar
         * 2. Checking the order the certificates are resolved
         * 3. Checking each of the certificates have the expected serial number
         * 4. Checking each of the certificates have the expected public key
         */
        private fun validateCertificates(certificates: List<X509Certificate>): Boolean {
            if (certificates.size != EXPECTED_CERTIFICATE_CHAIN_LENGTH) {
                return false
            }

            val base64Encoder = Base64.getEncoder()

            // First verify the Jagex public key
            val jagexCertificate = certificates
                .find { it.serialNumber.toString() == ORIGINAL_JAGEX_CERTIFICATE_SERIAL_NUMBER }
                ?: return false

            if (!JAGEX_PUBLIC_KEYS.contains(base64Encoder.encodeToString(jagexCertificate.publicKey.encoded))) {
                return false
            }

            // Next verify the Thawte public key
            val thawteCertificate = certificates
                .find { it.serialNumber.toString() == ORIGINAL_THAWTE_CERTIFICATE_SERIAL_NUMBER }
                ?: return false

            val publicKey = base64Encoder.encodeToString(thawteCertificate.publicKey.encoded)
            if (!THAWTE_PUBLIC_KEYS.contains(publicKey)) {
                return false
            }

            // Verify the jagex certificate is issued by the Thawte certificate
            return jagexCertificate.issuerX500Principal.equals(thawteCertificate.subjectX500Principal)
        }

        /**
         * The expected length of the signed jar's certificate chain.
         */
        private const val EXPECTED_CERTIFICATE_CHAIN_LENGTH = 2
    }
}
