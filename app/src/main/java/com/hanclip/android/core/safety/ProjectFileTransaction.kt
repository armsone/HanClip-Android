package com.hanclip.android.core.safety

import java.io.File

internal object ProjectFileTransaction {
    fun cleanupInterrupted(filesDirectory: File) {
        val projectsDirectory = File(filesDirectory, "editable-projects")
        projectsDirectory.listFiles()
            ?.filter(File::isDirectory)
            ?.forEach { projectDirectory ->
                runCatching { File(projectDirectory, "project.json.tmp").delete() }
                File(projectDirectory, "media").listFiles()
                    ?.filter { file -> file.isFile && ".tmp-" in file.name }
                    ?.forEach { file -> runCatching { file.delete() } }
            }
    }
}
