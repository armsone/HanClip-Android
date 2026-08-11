package com.hanclip.android.core.safety

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal object ExportFileTransaction {
    private const val PartialMarker = ".partial"

    fun stagingFile(finalFile: File): File {
        return File(finalFile.parentFile, "${finalFile.nameWithoutExtension}$PartialMarker.mp4")
    }

    fun cleanupInterrupted(directory: File) {
        directory.listFiles()
            ?.filter { file -> file.isFile && file.name.contains("$PartialMarker.") }
            ?.forEach { file -> runCatching { file.delete() } }
    }

    fun promote(stagingFile: File, finalFile: File): File {
        check(stagingFile.isFile && stagingFile.length() > 0L) {
            "검증된 완성본 임시 파일이 없습니다."
        }
        runCatching {
            Files.move(
                stagingFile.toPath(),
                finalFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE
            )
        }.recoverCatching {
            Files.move(stagingFile.toPath(), finalFile.toPath())
        }.getOrThrow()
        return finalFile
    }
}
