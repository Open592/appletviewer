package com.open592.appletviewer.jar

import java.security.cert.X509Certificate
import java.util.Base64

/**
 * Provides logic for verifying a signed jar's certificates, using the original
 * Jagex logic.
 *
 * For more information about the structure of certificates which we will be
 * verifying you can view of repository which is dedicated to constructing
 * Jagex like certificate chains: https://github.com/Open592/simulate-jagex-certificate
 *
 * NOTE: This is not secure, and is only present to match the original logic. It
 * should not be used as a reference for how to verify remote jar files in other
 * projects.
 *
 * @param fakeThawtePublicKey If present we verify jar files with this public
 * key in place of the original Thawte public key.
 * @param fakeJagexPublicKey If present we verify jar files with this public
 * key in place of the original Jagex public key.
 * @param disableJarValidation Allows for disabling the jar file validation
 * logic. Useful if you want to use unsigned jar files.
 */
public class CertificateValidator(
    fakeThawtePublicKey: String?,
    fakeJagexPublicKey: String?,
    private val disableJarValidation: Boolean,
) {
    /**
     * The original applet viewer had some rudimentary certificate
     * verification which included:
     *
     * 1. Checking the number of certificates present within the signed jar
     * 2. Checking the order the certificates are resolved
     * 3. Checking each of the certificates have the expected serial number
     * 4. Checking each of the certificates have the expected public key
     */
    public fun validateCertificates(certificates: List<X509Certificate>): Boolean {
        if (disableJarValidation) {
            return true
        }

        if (certificates.size != EXPECTED_CERTIFICATE_CHAIN_LENGTH) {
            return false
        }

        // First verify we have a valid "Jagex" certificate.
        val jagexCertificate = certificates
            .find { it.serialNumber.toString() == JAGEX_CERTIFICATE_SERIAL_NUMBER }
            ?: return false

        if (!jagexPublicKeys.contains(base64Encoder.encodeToString(jagexCertificate.publicKey.encoded))) {
            return false
        }

        // Next verify we have a valid "Thawte" certificate.
        val thawteCertificate = certificates
            .find { it.serialNumber.toString() == THAWTE_CERTIFICATE_SERIAL_NUMBER }
            ?: return false

        if (!thawtePublicKeys.contains(base64Encoder.encodeToString(thawteCertificate.publicKey.encoded))) {
            return false
        }

        // Lastly verify the Jagex certificate is issued by the Thawte certificate
        return jagexCertificate.issuerX500Principal.equals(thawteCertificate.subjectX500Principal)
    }

    /**
     * List of valid "Thawte" public keys which we will accept.
     */
    private val thawtePublicKeys: Set<String> = setOfNotNull(fakeThawtePublicKey, ORIGINAL_THAWTE_PUBLIC_KEY)

    /**
     * List of valid "Jagex" public keys which we will accept.
     */
    private val jagexPublicKeys: Set<String> = setOfNotNull(fakeJagexPublicKey, ORIGINAL_JAGEX_PUBLIC_KEY)
    private val base64Encoder: Base64.Encoder = Base64.getEncoder()

    private companion object {
        /**
         * Thawte certificates had the following serial number.
         *
         * This value was validated by checking `loader.jar` and `loader_gl.jar`
         */
        private const val THAWTE_CERTIFICATE_SERIAL_NUMBER: String = "10"

        /**
         * Jagex used Thawte as their CA, and it had the following public key.
         *
         * X.509, CN=Thawte Code Signing CA, O=Thawte Consulting (Pty) Ltd., C=ZA
         *       Signature algorithm: SHA1withRSA (weak), 1024-bit key (weak)
         *       [certificate expired on 8/5/13, 4:59 PM]
         *
         * This value was validated by checking `loader.jar` and `loader_gl.jar`
         */
        private const val ORIGINAL_THAWTE_PUBLIC_KEY: String =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGuLknYK8L45FpZdt+je2R5qrxvtXt/m3ULH/RcHf7JplXtN0/MLjcIepojYGS/C5L" +
                "kTWEIPLaSrq0/ObaiPIgxSGSCUeVoAkcpnm+sUwd/PGKblTSaaHxTJM6Qf591GR7Y0X3YGAdMR2k6dMPi/tuJiSzqP/l5ZDUtMLc" +
                "UGCuWQIDAQAB"

        /**
         * Jagex certificates for the 592 revision had the following serial number.
         *
         * This value was validated by checking `loader.jar` and `loader_gl.jar`
         */
        private const val JAGEX_CERTIFICATE_SERIAL_NUMBER: String = "105014014184937810784491209018632141624"

        /**
         * Jagex's public key for files signed during the 592 client release.
         *
         * X.509, CN=Jagex Ltd, OU=SECURE APPLICATION DEVELOPMENT, O=Jagex Ltd, L=Cambridge, ST=Cambridgeshire, C=GB
         *       Signature algorithm: SHA1withRSA (weak), 2048-bit key
         *       [certificate expired on 9/12/10, 4:59 PM]
         *
         * This value was validated by checking `loader.jar` and `loader_gl.jar`
         */
        private const val ORIGINAL_JAGEX_PUBLIC_KEY: String =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxehHTKQFiy/+t7xlQ0UYmmpQyoohClLm5Gfcy9hqwSps8riRS4LH4F3Ii9Xn" +
                "PYYC85R0wMfsfFCQlqgPbHK4X2iuUNw/bAT8jVHeIAIHPrxBaBqIzq92CHfGmLDDWEMQh+R5EpKW6caR0HB38c/eNYce5Do8DwOI" +
                "MI/tC0LTcfjkgSjB2G19pT38W/ra1XwFVZR3fL/vvUGPiNDdcCcQCniPjYE1wLI/y9iNDfPcEnL92rhq3g5WVYrZ/CAXHAdQ9wCG" +
                "BRyRgtVM1AjWYranZI9fNj+h/KjRDa+Fsu+k5gKLiKRNz9PGk+mmrBFOWOWMCsjyOalnkkx+N1/Gh4KcRwIDAQAB"

        /**
         * The expected length of the signed jar's certificate chain.
         */
        private const val EXPECTED_CERTIFICATE_CHAIN_LENGTH = 2
    }
}
