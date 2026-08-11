package com.hanclip.android.core.project

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

object CopyrightIconStore {
    private const val DirectoryName = "copyright-icons"
    private const val IconFilename = "custom-icon"

    fun persist(context: Context, source: Uri): String {
        val directory = File(context.filesDir, DirectoryName).apply { mkdirs() }
        val target = File(directory, IconFilename)
        val staging = File(directory, ".icon-staging-${UUID.randomUUID()}")
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(staging).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            } ?: error("선택한 이미지를 읽을 수 없습니다.")
            require(staging.length() > 0L) { "비어 있는 이미지는 사용할 수 없습니다." }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(staging.absolutePath, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) {
                "선택한 파일에서 이미지를 확인하지 못했습니다."
            }
            runCatching {
                Files.move(
                    staging.toPath(),
                    target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }.recoverCatching {
                Files.move(
                    staging.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }.getOrThrow()
            return target.absolutePath
        } finally {
            staging.delete()
        }
    }
}
