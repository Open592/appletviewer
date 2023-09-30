package com.open592.appletviewer.dependencies

/**
 * Jagex's certificates for the 592 revision had the following serial number.
 *
 * This value was validated by checking `loader.jar` and `loader_gl.jar`
 */
public const val ORIGINAL_JAGEX_CERTIFICATE_SERIAL_NUMBER: String = "105014014184937810784491209018632141624"

/**
 * Thawte's certificate had the following serial number.
 *
 * This value was validated by checking `loader.jar` and `loader_gl.jar`
 */
public const val ORIGINAL_THAWTE_CERTIFICATE_SERIAL_NUMBER: String = "10"

/**
 * Jagex's public key for files signed during the 592 client release.
 *
 * X.509, CN=Jagex Ltd, OU=SECURE APPLICATION DEVELOPMENT, O=Jagex Ltd, L=Cambridge, ST=Cambridgeshire, C=GB
 *       Signature algorithm: SHA1withRSA (weak), 2048-bit key
 *       [certificate expired on 9/12/10, 4:59 PM]
 *
 * This value was validated by checking `loader.jar` and `loader_gl.jar`
 */
public const val ORIGINAL_JAGEX_PUBLIC_KEY: String =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxehHTKQFiy/+t7xlQ0UYmmpQyoohClLm5Gfcy9hqwSps8riRS4LH4F3Ii9XnPYYC85R0" +
        "wMfsfFCQlqgPbHK4X2iuUNw/bAT8jVHeIAIHPrxBaBqIzq92CHfGmLDDWEMQh+R5EpKW6caR0HB38c/eNYce5Do8DwOIMI/tC0LTcfjkgSjB" +
        "2G19pT38W/ra1XwFVZR3fL/vvUGPiNDdcCcQCniPjYE1wLI/y9iNDfPcEnL92rhq3g5WVYrZ/CAXHAdQ9wCGBRyRgtVM1AjWYranZI9fNj+h" +
        "/KjRDa+Fsu+k5gKLiKRNz9PGk+mmrBFOWOWMCsjyOalnkkx+N1/Gh4KcRwIDAQAB"

/**
 * Jagex used Thawte as their CA, and it had the following public key.
 *
 * X.509, CN=Thawte Code Signing CA, O=Thawte Consulting (Pty) Ltd., C=ZA
 *       Signature algorithm: SHA1withRSA (weak), 1024-bit key (weak)
 *       [certificate expired on 8/5/13, 4:59 PM]
 *
 * This value was validated by checking `loader.jar` and `loader_gl.jar`
 */
public const val ORIGINAL_THAWTE_PUBLIC_KEY: String =
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGuLknYK8L45FpZdt+je2R5qrxvtXt/m3ULH/RcHf7JplXtN0/MLjcIepojYGS/C5LkTWEIPLa" +
        "Srq0/ObaiPIgxSGSCUeVoAkcpnm+sUwd/PGKblTSaaHxTJM6Qf591GR7Y0X3YGAdMR2k6dMPi/tuJiSzqP/l5ZDUtMLcUGCuWQIDAQAB"
