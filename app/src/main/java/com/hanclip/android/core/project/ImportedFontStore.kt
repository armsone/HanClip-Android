package com.hanclip.android.core.project

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.UUID

data class ImportedFontSummary(
    val id: String,
    val displayName: String
)

object ImportedFontStore {
    private const val DirectoryName = "imported-fonts"
    private const val IdPrefix = "imported_font:"
    internal const val MaximumFontCount = 30

    fun list(context: Context): List<ImportedFontSummary> {
        return directory(context).listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in setOf("ttf", "otf") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                ImportedFontSummary(
                    id = IdPrefix + file.name,
                    displayName = displayName(file.name)
                )
            }
            .orEmpty()
    }

    fun import(context: Context, uri: Uri): ImportedFontSummary {
        check(canImportMoreFonts(list(context).size)) {
            "사용자 글꼴은 최대 ${MaximumFontCount}개까지 보관할 수 있습니다. 기존 프로젝트의 글꼴을 보호하기 위해 새 파일을 추가하지 않았습니다."
        }
        val sourceName = queryDisplayName(context, uri)
            ?: uri.lastPathSegment
            ?: "font.ttf"
        val extension = sourceName.substringAfterLast('.', "ttf").lowercase()
            .takeIf { it in setOf("ttf", "otf") }
            ?: "ttf"
        val baseName = sourceName.substringBeforeLast('.', sourceName)
            .replace(Regex("[^\\p{L}\\p{N}._ -]"), "_")
            .trim()
            .take(48)
            .ifBlank { "사용자 글꼴" }
        val target = File(
            directory(context),
            "$baseName--${UUID.randomUUID()}.$extension"
        )
        val staging = File(directory(context), ".font-staging-${UUID.randomUUID()}.$extension")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(staging).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            } ?: error("글꼴 파일을 열 수 없습니다.")
            require(staging.length() > 0L) { "비어 있는 글꼴 파일은 가져올 수 없습니다." }
            require(hasSupportedFontStructure(staging)) { "TTF 또는 OTF 글꼴 파일이 아닙니다." }
            runCatching { Typeface.createFromFile(staging) }
                .getOrElse { error ->
                    throw IllegalArgumentException("TTF 또는 OTF 글꼴 파일이 아닙니다.", error)
                }
            check(staging.renameTo(target)) { "검증한 글꼴 파일을 저장하지 못했습니다." }
        } catch (error: Throwable) {
            staging.delete()
            target.delete()
            throw error
        } finally {
            staging.delete()
        }
        return ImportedFontSummary(
            id = IdPrefix + target.name,
            displayName = displayName(target.name)
        )
    }

    internal fun canImportMoreFonts(currentCount: Int): Boolean {
        return currentCount.coerceAtLeast(0) < MaximumFontCount
    }

    internal fun hasSupportedFontStructure(file: File): Boolean = runCatching {
        if (!file.isFile || file.length() < 12L) return@runCatching false
        RandomAccessFile(file, "r").use { input ->
            val signature = input.readInt()
            if (signature == TrueTypeCollectionSignature) {
                input.readInt()
                val fontCount = input.readInt()
                fontCount in 1..MaximumCollectionFontCount &&
                    12L + fontCount * 4L <= input.length()
            } else {
                if (signature !in SupportedSfntSignatures) return@use false
                val tableCount = input.readUnsignedShort()
                tableCount in 1..MaximumTableCount &&
                    12L + tableCount * 16L <= input.length()
            }
        }
    }.getOrDefault(false)

    fun typeface(context: Context, id: String): Typeface? {
        val filename = id.takeIf { it.startsWith(IdPrefix) }
            ?.removePrefix(IdPrefix)
            ?: return null
        if (filename != File(filename).name) return null
        val file = File(directory(context), filename)
        if (!file.isFile) return null
        return runCatching { Typeface.createFromFile(file) }.getOrNull()
    }

    fun isImportedFont(id: String): Boolean = id.startsWith(IdPrefix)

    private fun directory(context: Context): File {
        return File(context.filesDir, DirectoryName).apply { mkdirs() }
    }

    private fun displayName(filename: String): String {
        return filename.substringBeforeLast('.')
            .substringBeforeLast("--")
            .ifBlank { "사용자 글꼴" }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    private const val TrueTypeCollectionSignature = 0x74746366
    private const val MaximumCollectionFontCount = 256
    private const val MaximumTableCount = 4_096
    private val SupportedSfntSignatures = setOf(
        0x00010000,
        0x4F54544F,
        0x74727565,
        0x74797031
    )
}
