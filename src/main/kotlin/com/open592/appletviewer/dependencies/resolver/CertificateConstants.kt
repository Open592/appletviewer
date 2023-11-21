package com.open592.appletviewer.dependencies.resolver

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
 * Our simulated "Jagex" public key
 */
public const val FAKE_JAGEX_PUBLIC_KEY: String =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuMxaBUjyNpGCKIHnZGHNs/8uNXUHYPJMannDFjdJHqMzYQNYTPKW2DXAROmXd8gSXVh5" +
        "jWIUk16uD3lleeBNJrgfkFgnBCBbJuC2m26shIO9WpumipgcAC6jmjXNnR72RyydLn7Yz0HYIbxqgGR6fP7drQ2LS3DZ0PyyyHvICdtwKKEg" +
        "qMQMCQ76JUapn+LPhGnfWkEuRcNnTP9MZLxo3l9behO+gBvoHujq+FNp0YcoR3mDyL06Ku8a2Zmx40HVqWGkF4TKf/jgjOBRP3ovg0HSes0N" +
        "flio3aTxQ0xYmw5bna1BfCvgopkOHeTuS8FENbiwzsPWvk3CYHxbi5pOowIDAQAB"

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

/**
 * Our simulated "Thawte" public key.
 */
public const val FAKE_THAWTE_PUBLIC_KEY: String =
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCe9NS0y8btwRdIX3ueWh0dnNPVf3ukq266miW2gRwGGon0uxyH0Ai2KTiVIYeZzXZcJf6qdf8l" +
        "0ve7uuu1cK0UCEPC0eNLFClqTl6Pi8cLJeDYjo6Q0yv5wtkWb2x4oc9Q67WUte2C+bBToXw8L7SFUqmj3EOP8BF8GmUDzsggAwIDAQAB"
