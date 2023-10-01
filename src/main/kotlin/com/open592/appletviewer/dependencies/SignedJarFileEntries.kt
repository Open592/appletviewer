package com.open592.appletviewer.dependencies

import okio.Buffer
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.jar.JarInputStream
import java.util.jar.Manifest

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
         * Given a properly structured `JarInputStream`, extract the contents
         * and validate the signatures within.
         */
        public fun loadAndValidate(jar: JarInputStream): SignedJarFileEntries? {
            val signatureFile = Manifest()
            val publicKeys: MutableMap<String, String> = mutableMapOf()
            val entries: MutableMap<String, Buffer> = mutableMapOf()

            generateSequence { jar.nextJarEntry }.forEach { entry ->
                val buffer = Buffer().readFrom(jar)

                when (entry.name) {
                    "META-INF/ZIGBERT.SF" -> signatureFile.read(buffer.inputStream())
                    "META-INF/ZIGBERT.RSA" -> publicKeys.putAll(collectPublicKeys(buffer))
                    else -> entries[entry.name] = buffer
                }
            }

            // Verify that we have the expected certificate chain length
            if (publicKeys.size != EXPECTED_CERTIFICATE_CHAIN_LENGTH) {
                return null
            }

            // First verify the Jagex public key
            if (!JAGEX_PUBLIC_KEYS.contains(publicKeys[ORIGINAL_JAGEX_CERTIFICATE_SERIAL_NUMBER])) {
                return null
            }

            // Next verify the Thawte public key
            if (!THAWTE_PUBLIC_KEYS.contains(publicKeys[ORIGINAL_THAWTE_CERTIFICATE_SERIAL_NUMBER])) {
                return null
            }

            return SignedJarFileEntries(entries)
        }

        private fun collectPublicKeys(buffer: Buffer): Map<String, String> {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val base64Encoder = Base64.getEncoder()
            val certificates: MutableMap<String, String> = mutableMapOf()

            certificateFactory.generateCertificates(buffer.inputStream()).forEach {
                val certificate = it as X509Certificate
                val serialNumber = certificate.serialNumber.toString()
                val publicKey = base64Encoder.encodeToString(certificate.publicKey.encoded)

                certificates[serialNumber] = publicKey
            }

            return certificates
        }

        /**
         * The expected length of the signed jar's certificate chain.
         */
        private const val EXPECTED_CERTIFICATE_CHAIN_LENGTH = 2

        /**
         * These are "Jagex" keys, since in the original jars they are listed
         * under the common name "Jagex Ltd".
         *
         * - The first entry is Jagex's actual public key from when the 592 revision
         * was released.
         *
         * - The second entry is our "fake" public key which we will use to sign our
         * custom jars.
         */
        private val JAGEX_PUBLIC_KEYS: Array<String> = arrayOf(ORIGINAL_JAGEX_PUBLIC_KEY, FAKE_JAGEX_PUBLIC_KEY)

        /**
         * These are "Thawte" keys, since in the original jars they are listed
         * under the common name "Thawte Code Signing CA".
         *
         * - The first entry is Thawte's actual public key from when the 592
         * revision was released.
         *
         * - The second is our fake public key which we will use to sign our
         * custom jars.
         */
        private val THAWTE_PUBLIC_KEYS = arrayOf(ORIGINAL_THAWTE_PUBLIC_KEY, FAKE_THAWTE_PUBLIC_KEY)
    }
}
