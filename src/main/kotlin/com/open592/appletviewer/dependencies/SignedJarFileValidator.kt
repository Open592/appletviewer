package com.open592.appletviewer.dependencies

import okio.Buffer
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.JarInputStream
import java.util.jar.Manifest

public class SignedJarFileValidator(
    private val certificate: List<X509Certificate>,
    private val entries: Map<String, Buffer>,
    private val manifestFile: Manifest,
    private val signatureFile: Manifest,
) {
    public fun validateEntry(filename: String): Buffer? {
        val buffer = entries[filename] ?: return null
        val md5 = buffer.md5().base64()
        val sha1 = buffer.sha1().base64()

        if (manifestFile.entries[filename]?.getValue("MD5-Digest") != md5) {
            return null
        }

        if (manifestFile.entries[filename]?.getValue("SHA1-Digest") != sha1) {
            return null
        }

        return buffer
    }

    public companion object {
        public fun load(jarFile: Buffer): SignedJarFileValidator {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val jarStream = JarInputStream(jarFile.inputStream())

            val certificates: MutableList<X509Certificate> = mutableListOf()
            val entries: MutableMap<String, Buffer> = mutableMapOf()
            val manifestFile = Manifest()
            val signatureFile = Manifest()

            generateSequence { jarStream.nextJarEntry }.forEach { entry ->
                val buffer = Buffer().readFrom(jarStream)

                when (entry.name.lowercase()) {
                    "meta-inf/manifest.mf" -> manifestFile.read(buffer.inputStream())
                    "meta-inf/zigbert.rsa" -> {
                        certificateFactory.generateCertificates(buffer.inputStream()).forEach { certificate ->
                            certificates.add(certificate as X509Certificate)
                        }
                    }
                    "meta-inf/zigbert.sf" -> signatureFile.read(buffer.inputStream())
                    else -> entries[entry.name] = buffer
                }
            }

            return SignedJarFileValidator(certificates, entries, manifestFile, signatureFile)
        }
    }
}
