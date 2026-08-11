package com.hanclip.android.core.safety

import java.io.File

internal object UserAssetFileTransaction {
    private val stagingLocations = listOf(
        "imported-fonts" to ".font-staging-",
        "background-music" to ".music-staging-",
        "copyright-icons" to ".icon-staging-"
    )

    fun cleanupInterrupted(filesDirectory: File) {
        stagingLocations.forEach { (directoryName, prefix) ->
            File(filesDirectory, directoryName).listFiles()
                ?.filter { file -> file.isFile && file.name.startsWith(prefix) }
                ?.forEach { file -> runCatching { file.delete() } }
        }
    }
}
