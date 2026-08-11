package com.hanclip.android.core.project

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

object BackgroundMusicStore {
    private const val DirectoryName = "background-music"
    private const val MaximumWorkingFileCount = 24

    fun persist(context: Context, source: Uri): Uri {
        if (source.scheme == "android.resource") return source
        val directory = File(context.filesDir, DirectoryName).apply { mkdirs() }.canonicalFile
        val sourceFile = source.takeIf { it.scheme == "file" }?.path?.let(::File)
        if (sourceFile?.canonicalFile?.parentFile == directory && sourceFile.isFile) {
            return Uri.fromFile(sourceFile)
        }

        val extension = sourceExtension(context, source)
        val target = File(directory, "hanclip-music-${UUID.randomUUID()}.$extension")
        val staging = File(directory, ".music-staging-${UUID.randomUUID()}.$extension")
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(staging).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            } ?: error("선택한 음악 파일을 열 수 없습니다.")
            require(staging.length() > 0L) { "비어 있는 음악 파일은 사용할 수 없습니다." }
            require(hasAudioTrack(staging)) { "선택한 파일에서 재생 가능한 오디오 트랙을 찾지 못했습니다." }
            runCatching {
                Files.move(
                    staging.toPath(),
                    target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE
                )
            }.recoverCatching {
                Files.move(staging.toPath(), target.toPath())
            }.getOrThrow()
            prune(directory, keeping = target)
            return Uri.fromFile(target)
        } finally {
            staging.delete()
        }
    }

    private fun sourceExtension(context: Context, source: Uri): String {
        val mimeType = context.contentResolver.getType(source).orEmpty()
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: source.lastPathSegment
                ?.substringAfterLast('.', missingDelimiterValue = "")
                ?.lowercase()
                ?.takeIf { it.matches(Regex("[a-z0-9]{1,5}")) }
            ?: "m4a"
    }

    private fun hasAudioTrack(file: File): Boolean = runCatching {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            (0 until extractor.trackCount).any { trackIndex ->
                extractor.getTrackFormat(trackIndex)
                    .getString(MediaFormat.KEY_MIME)
                    .orEmpty()
                    .startsWith("audio/")
            }
        } finally {
            extractor.release()
        }
    }.getOrDefault(false)

    private fun prune(directory: File, keeping: File) {
        directory.listFiles()
            ?.filter { it.isFile && it.name.startsWith("hanclip-music-") && it != keeping }
            ?.sortedByDescending(File::lastModified)
            ?.drop(MaximumWorkingFileCount - 1)
            ?.forEach { file -> runCatching { file.delete() } }
    }
}
