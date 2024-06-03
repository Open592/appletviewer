package com.open592.appletviewer.jar

import okio.Buffer

public interface InMemoryClassLoaderFactory {
    public fun create(jarFile: Buffer): InMemoryClassLoader
}
