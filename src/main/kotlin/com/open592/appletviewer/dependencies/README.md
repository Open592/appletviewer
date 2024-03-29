# Dependency Resolver

## Overview

Handles resolving and validating game dependencies, which include:

- **browsercontrol**: This is the native library used to display advertisements
at the top of the game client.
- **loader.jar**: This is the jar file which downloads and unpacks a list of
game dependencies (game code, jaggl libs, sw3d).

## Signing Jar files

### Background

By examining the official jar files hosted by Jagex and reading through the
official applet viewer code we got a better understanding of how files were
signed and validated by Jagex.

The tools used are obviously unknown by anyone who wasn't actively working on
there, but we have enough information make an educated guess that they were
signed using `signtool` (previously named `zigbert`). With this information we
are able to simulate the signing process ourselves, and generate jar files which
resemble the official Jagex jar files.

**Note:**: Although we simulate the signing process, we can't create jar files
which completely pass the logic present in the original Jagex applet viewer.
This would require being able to sign files with Jagex's private key. That being
said, we include a copy of Jagex's public key within our implementation of the
applet viewer to allow loading their jar files within our applet viewer.

### Signing the Jar files

The official jar files from the 592 era were signed using `signtool` which was
earlier named `zigbert`. You can verify this by looking at the manifest file
provided within the browsercontrol jar file.

I have looked around and found some binaries of `signtool` - the source is also
available within Mozilla's `ncc` security suite. Unfortunately I haven't had
the time to work out how it locates the keystore. I have a feeling that one of
the following were true:

- Jagex was given a binary from Thwart which they used to automatically sign
their files.
- Jagex was given the keystore files by Thwart.

You can find the documentation of `signtool` here:

- https://docs.oracle.com/cd/E19957-01/816-6169-10/contents.htm
- https://web.archive.org/web/20230908073203/https://docs.oracle.com/cd/E19957-01/816-6169-10/contents.htm

What I have done to simulate signing of our jar files is a use modified version
of `jarsigner` (which I have hosted here: https://github.com/Open592/jarsigner).
It provides a very similar interface and produces almost identical jar files as
the ones hosted by Jagex back in 2010.

#### Instructions for creating a signing a jar file:
> Note: I am going to be providing the executable names as if you were running
  this on Linux. But on Windows, if running within Powershell, you should also
  include the `.exe` extension.


For the following instructions we are going to be packaging a sample file
called `browsercontrol64.dll`. This was the library file used by Jagex to
show a browser view containing advertisements in the client when it was
released. It's a signed jar.

First we are going to create a jar file wrapping the `.dll`:

```bash
jar cf browsercontrol_test.jar browsercontrol.dll
```

Next we need to create some self-signed certificates which emulate Jagex's
certificates present at the time. We can get the information required to do this
by running the `verify` command from `jarsigner` on a valid Jagex jar file from
the 592 era:

```bash
jarsigner -verify -verbose -certs loader.jar
```

```text
>>> Signer
X.509, CN=Jagex Ltd, OU=SECURE APPLICATION DEVELOPMENT, O=Jagex Ltd, L=Cambridge, ST=Cambridgeshire, C=GB
Signature algorithm: SHA1withRSA (weak), 2048-bit key
[certificate expired on 9/12/10, 4:59 PM]
X.509, CN=Thawte Code Signing CA, O=Thawte Consulting (Pty) Ltd., C=ZA
Signature algorithm: SHA1withRSA (weak), 1024-bit key (weak)
[certificate expired on 8/5/13, 4:59 PM]
[Invalid certificate chain: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target]

sm     11221 Fri Feb 19 14:26:20 PST 2010 loader.class
 ```

With this information, and the information provided in this repository as well
as de-obfuscation's of the official applet viewer, you should be able to create
self-signed certificates which pass all the verifications steps needed within
this application.

> Store the keystore wherever is convenient. In the below command we will be
using `~/source/keystore/fakejagex`

```text
jarsigner -verbose -keystore ~/source/keystore/fakejagex -sigfile ZIGBERT -digestalg MD5 -digestalg SHA1 browsercontrol_test.jar jagex
```

Note the following:
 1. It is **required** to use the
[modified `jarsigner`](https://github.com/Open592/jarsigner) in order to pass multiple `-digestalg` flags to
`jarsigner`. This is not a natively supported feature.
 2. To match Jagex make sure you note the order of the `-digestalg` flags.
 3. The `-sigfile` flag specifies the name for the `.sf` and `.rsa` files.
 4. It is expected that the keystore contains a key with alias `jagex`

#### Differences:
`jarsigner` requires that all the files within the `META-INF/` folder to be
  UPPERCASE, while `signtool` does not have this requirement. Thus, Jagex is
  expecting:
 - `META-INF/manifest.mf`
 - `META-INF/zigbert.sf`
 - `META-INF/zigbert.rsa`

**While we are producing:**
 - `META-INF/MANIFEST.MF`
 - `META-INF/ZIGBERT.SF`
 - `META-INF/ZIGBERT.RSA`

Our `jarsigner` also produces a different value for the `Created-By` attribute:

```text
Manifest-Version: 1.0
Created-By: 17.0.1 (Oracle Corporation)
Comments: PLEASE DO NOT EDIT THIS FILE. YOU WILL BREAK IT.

Name: browsercontrol64.dll
Digest-Algorithms: MD5 SHA1
MD5-Digest: rX4eUziU7xfmFNGDLE0Qng==
SHA1-Digest: TzPoBT0MuVHZ3Q9KxHaoxQTpHu0=
```

**Versus an official Jagex jar from 2010:**

```text
Manifest-Version: 1.0
Created-By: Signtool (signtool 1.3)
Comments: PLEASE DO NOT EDIT THIS FILE. YOU WILL BREAK IT.

Name: browsercontrol64.dll
Digest-Algorithms: MD5 SHA1
MD5-Digest: rX4eUziU7xfmFNGDLE0Qng==
SHA1-Digest: TzPoBT0MuVHZ3Q9KxHaoxQTpHu0=
```

*As you can see we are almost identical.*
