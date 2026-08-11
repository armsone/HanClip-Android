package com.hanclip.android.core.safety

import java.io.File

internal object ImportFileTransaction {
    fun cleanupInterrupted(workingMediaDirectory: File) {
        workingMediaDirectory.listFiles()
            ?.filter { file -> file.isFile && file.name.endsWith(".tmp") }
            ?.forEach { file -> runCatching { file.delete() } }
    }
}
