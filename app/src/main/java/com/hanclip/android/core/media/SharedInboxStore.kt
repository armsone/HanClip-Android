package com.hanclip.android.core.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

object SharedInboxStore {
    private const val DirectoryName = "shared-inbox"

    fun pendingUris(context: Context): List<Uri> = inboxDirectory(context)
        .listFiles()
        .orEmpty()
        .filter { it.isFile && !it.name.endsWith(".pending") }
        .sortedBy(File::lastModified)
        .map(Uri::fromFile)

    suspend fun append(
        context: Context,
        sourceUris: List<Uri>,
        onProgress: suspend (completed: Int, total: Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val directory = inboxDirectory(context).apply { mkdirs() }
        val created = mutableListOf<File>()
        try {
            sourceUris.distinctBy(Uri::toString).forEachIndexed { index, uri ->
                currentCoroutineContext().ensureActive()
                val destination = destinationFile(context, directory, uri)
                val staging = File(directory, "${destination.name}.pending")
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(staging).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                            }
                            output.fd.sync()
                        }
                    } ?: error("공유 파일을 열 수 없습니다.")
                    check(staging.length() > 0L) { "공유 파일이 비어 있습니다." }
                    check(staging.renameTo(destination)) { "공유 파일을 보관하지 못했습니다." }
                    created += destination
                }.onFailure {
                    staging.delete()
                }
                withContext(Dispatchers.Main.immediate) {
                    onProgress(index + 1, sourceUris.size)
                }
            }
            created.size
        } catch (error: Throwable) {
            created.forEach(File::delete)
            directory.listFiles()
                .orEmpty()
                .filter { it.name.endsWith(".pending") }
                .forEach(File::delete)
            throw error
        }
    }

    fun clear(context: Context) {
        inboxDirectory(context).listFiles().orEmpty().forEach(File::delete)
    }

    private fun inboxDirectory(context: Context) = File(context.filesDir, DirectoryName)

    private fun destinationFile(context: Context, directory: File, uri: Uri): File {
        val displayName = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
        val namedExtension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
        val mimeExtension = context.contentResolver.getType(uri)
            ?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
        val extension = namedExtension ?: mimeExtension ?: "bin"
        return File(directory, "${System.currentTimeMillis()}-${UUID.randomUUID()}.$extension")
    }
}
