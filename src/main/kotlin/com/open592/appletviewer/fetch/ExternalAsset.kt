package com.open592.appletviewer.fetch

import java.io.BufferedReader
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

public class ExternalAsset constructor(private val stream: InputStream) {
    public fun toBufferedReader(): BufferedReader {
        return stream.bufferedReader()
    }

    public companion object {
        public fun fromPath(path: Path): ExternalAsset {
            return ExternalAsset(Files.newInputStream(path))
        }
    }
}
