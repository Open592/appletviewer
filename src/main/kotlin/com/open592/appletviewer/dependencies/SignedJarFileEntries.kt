package com.open592.appletviewer.dependencies

import okio.Buffer
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
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
            val certificateFactory = CertificateFactory.getInstance("X.509")

            val signatureFile = Manifest()
            val certificates: MutableList<X509Certificate> = mutableListOf()
            val entries: MutableMap<String, Buffer> = mutableMapOf()

            generateSequence { jar.nextJarEntry }.forEach { entry ->
                val buffer = Buffer().readFrom(jar)

                when (entry.name) {
                    "META-INF/ZIGBERT.SF" -> signatureFile.read(buffer.inputStream())
                    "META-INF/ZIGBERT.RSA" -> {
                        certificateFactory.generateCertificates(buffer.inputStream()).forEach { certificate ->
                            certificates.add(certificate as X509Certificate)
                        }
                    }
                    else -> entries[entry.name] = buffer
                }
            }

            // Validate the certificates
            certificates.forEachIndexed { index, certificate ->
                if (certificate.serialNumber.toString() != CERTIFICATE_SERIAL_NUMBERS[index]) {
                    return null
                }

                val publicKeyString = certificate.publicKey.encoded.decodeToString()

                if (!CERTIFICATE_PUBLIC_KEYS[index].contains(publicKeyString)) {
                    return null
                }
            }

            return SignedJarFileEntries(entries)
        }

        /**
         * We verify that the serial numbers of each of the code signers is correct.
         *
         * Our certificates contain the same serial numbers as the official jars from
         * Jagex.
         */
        private val CERTIFICATE_SERIAL_NUMBERS = arrayOf(
            // Jagex
            "42616207341001253724625765329114307230",
            // Thawte
            "10",
        )

        /**
         * These are "Jagex" keys, since in the original jars they are listed
         * under the common name "Jagex".
         *
         * - The first entry is Jagex's actual public key from when the 592 revision
         * was released.
         *
         * - The second entry is our "fake" public key which we will use to sign our
         * custom jars.
         */
        private val JAGEX_PUBLIC_KEYS = arrayOf(
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxehHTKQFiy/+t7xlQ0UYmmpQyoohClLm5Gfcy9hqwSps8riRS4LH4F3Ii9XnPYYC85R0wMfsfFCQlqgPbHK4X2iuUNw/bAT8jVHeIAIHPrxBaBqIzq92CHfGmLDDWEMQh+R5EpKW6caR0HB38c/eNYce5Do8DwOIMI/tC0LTcfjkgSjB2G19pT38W/ra1XwFVZR3fL/vvUGPiNDdcCcQCniPjYE1wLI/y9iNDfPcEnL92rhq3g5WVYrZ/CAXHAdQ9wCGBRyRgtVM1AjWYranZI9fNj+h/KjRDa+Fsu+k5gKLiKRNz9PGk+mmrBFOWOWMCsjyOalnkkx+N1/Gh4KcRwIDAQAB",
        )

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
        private val THAWTE_PUBLIC_KEYS = arrayOf(
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGuLknYK8L45FpZdt+je2R5qrxvtXt/m3ULH/RcHf7JplXtN0/MLjcIepojYGS/C5LkTWEIPLaSrq0/ObaiPIgxSGSCUeVoAkcpnm+sUwd/PGKblTSaaHxTJM6Qf591GR7Y0X3YGAdMR2k6dMPi/tuJiSzqP/l5ZDUtMLcUGCuWQIDAQAB",
        )

        /**
         * We verify that the certificate's public keys are correct.
         *
         * We support both the original pu
         */
        private val CERTIFICATE_PUBLIC_KEYS = arrayOf(JAGEX_PUBLIC_KEYS, THAWTE_PUBLIC_KEYS)
    }
}
