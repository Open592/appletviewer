package com.open592.appletviewer.assets

import okio.BufferedSource

public interface ApplicationAssetResolver {
    /**
     * Given a file, save it to the folder containing the cache.
     *
     * This folder is defined by a platform specific search routine.
     */
    public fun saveCacheFile(name: String, file: BufferedSource): Boolean
}
