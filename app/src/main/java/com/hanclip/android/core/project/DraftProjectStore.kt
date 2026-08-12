package com.hanclip.android.core.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.ClipMediaKind
import com.hanclip.android.core.model.LivePhotoMode
import com.hanclip.android.core.model.MoviePreset
import com.hanclip.android.core.model.OutputAspectRatio
import com.hanclip.android.core.model.OutputQualityPreset
import com.hanclip.android.core.model.VideoSegmentMode
import com.hanclip.android.core.model.CopyrightIconColorMode
import com.hanclip.android.core.model.EndingInfoCardTheme
import com.hanclip.android.core.model.WatermarkFontSize
import com.hanclip.android.core.model.WatermarkLineSpacing
import com.hanclip.android.core.model.WatermarkPlatform
import com.hanclip.android.core.model.WatermarkPosition
import com.hanclip.android.core.model.WatermarkSettings
import com.hanclip.android.core.safety.loadPrimaryOrBackup
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

data class DraftProject(
    val schemaVersion: Int = DraftProjectSchemaVersion,
    val projectId: String = UUID.randomUUID().toString(),
    val clips: List<ClipItem>,
    val preset: MoviePreset,
    val defaultDurationSeconds: Double,
    val defaultVideoSegmentMode: VideoSegmentMode,
    val outputAspectRatio: OutputAspectRatio?,
    val outputQualityPreset: OutputQualityPreset,
    val watermarkSettings: WatermarkSettings,
    val backgroundMusicUri: Uri?,
    val backgroundMusicTitle: String? = null,
    val backgroundMusicSampleId: String? = null,
    val backgroundMusicVolume: Double = 0.35,
    val originalAudioVolume: Double = 1.0,
    val similarPhotoRepresentativeInterval: Int = 6,
    val backgroundMusicLoopsToFillVideo: Boolean = true,
    val backgroundMusicFadeInEnabled: Boolean = true,
    val backgroundMusicFadeOutEnabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val savedAtMillis: Long = System.currentTimeMillis()
)

private const val DraftProjectSchemaVersion = 1

object DraftProjectStore {
    private const val PreferencesName = "hanclip_draft_project"
    private const val DraftKey = "draft"

    fun save(context: Context, project: DraftProject) {
        if (project.clips.isEmpty()) {
            clear(context)
            return
        }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(DraftKey, project.toJson().toString())
            .apply()
    }

    fun load(context: Context): DraftProject? {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        val raw = preferences.getString(DraftKey, null)
            ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val project = json.toDraftProject()
            if (json.optString("projectId").isBlank() && project.clips.isNotEmpty()) {
                preferences.edit().putString(DraftKey, project.toJson().toString()).apply()
            }
            project
        }.getOrNull()
    }

    fun hasDraft(context: Context): Boolean = load(context)?.clips?.isNotEmpty() == true

    fun clear(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .remove(DraftKey)
            .apply()
    }
}

private fun DraftProject.toJson(): JSONObject {
    return JSONObject()
        .put("schemaVersion", schemaVersion)
        .put("projectId", projectId)
        .put("preset", preset.routeValue)
        .put("defaultDurationSeconds", defaultDurationSeconds)
        .put("defaultVideoSegmentMode", defaultVideoSegmentMode.name)
        .put("outputAspectRatio", outputAspectRatio?.name)
        .put("outputQualityPreset", outputQualityPreset.name)
        .put("backgroundMusicUri", backgroundMusicUri?.toString())
        .put("backgroundMusicTitle", backgroundMusicTitle)
        .put("backgroundMusicSampleId", backgroundMusicSampleId)
        .put("backgroundMusicVolume", backgroundMusicVolume)
        .put("originalAudioVolume", originalAudioVolume)
        .put("similarPhotoRepresentativeInterval", similarPhotoRepresentativeInterval)
        .put("backgroundMusicLoopsToFillVideo", backgroundMusicLoopsToFillVideo)
        .put("backgroundMusicFadeInEnabled", backgroundMusicFadeInEnabled)
        .put("backgroundMusicFadeOutEnabled", backgroundMusicFadeOutEnabled)
        .put("createdAtMillis", createdAtMillis)
        .put("savedAtMillis", savedAtMillis)
        .put("watermarkSettings", watermarkSettings.toJson())
        .put("clips", JSONArray().also { array ->
            clips.forEach { clip -> array.put(clip.toJson()) }
        })
}

private fun JSONObject.toDraftProject(): DraftProject {
    val clipsArray = optJSONArray("clips") ?: JSONArray()
    val clips = buildList {
        repeat(clipsArray.length()) { index ->
            runCatching { clipsArray.getJSONObject(index).toClipItem() }
                .getOrNull()
                ?.let(::add)
        }
    }
    val savedAtMillis = optLong("savedAtMillis", System.currentTimeMillis())
    return DraftProject(
        schemaVersion = maxOf(optInt("schemaVersion", DraftProjectSchemaVersion), DraftProjectSchemaVersion),
        projectId = optString("projectId").takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString(),
        clips = clips,
        preset = MoviePreset.fromRouteValue(optString("preset", MoviePreset.NewMovie.routeValue)),
        defaultDurationSeconds = optDouble("defaultDurationSeconds", 3.0).coerceIn(0.1, 30.0),
        defaultVideoSegmentMode = enumValueOrDefault(
            optString("defaultVideoSegmentMode"),
            VideoSegmentMode.Multiple
        ),
        outputAspectRatio = optString("outputAspectRatio")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let { enumValueOrNull<OutputAspectRatio>(it) },
        outputQualityPreset = enumValueOrDefault(
            optString("outputQualityPreset"),
            OutputQualityPreset.Standard
        ),
        watermarkSettings = optJSONObject("watermarkSettings")?.toWatermarkSettings()
            ?: WatermarkSettings(),
        backgroundMusicUri = optString("backgroundMusicUri")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let(Uri::parse),
        backgroundMusicTitle = optString("backgroundMusicTitle")
            .takeIf { it.isNotBlank() && it != "null" },
        backgroundMusicSampleId = optString("backgroundMusicSampleId")
            .takeIf { it.isNotBlank() && it != "null" },
        backgroundMusicVolume = optDouble("backgroundMusicVolume", 0.35).coerceIn(0.0, 1.0),
        originalAudioVolume = optDouble("originalAudioVolume", 1.0).coerceIn(0.0, 1.0),
        similarPhotoRepresentativeInterval = optInt("similarPhotoRepresentativeInterval", 6)
            .coerceIn(2, 12),
        backgroundMusicLoopsToFillVideo = optBoolean("backgroundMusicLoopsToFillVideo", true),
        backgroundMusicFadeInEnabled = optBoolean("backgroundMusicFadeInEnabled", true),
        backgroundMusicFadeOutEnabled = optBoolean("backgroundMusicFadeOutEnabled", true),
        createdAtMillis = optLong("createdAtMillis", savedAtMillis).coerceAtMost(savedAtMillis),
        savedAtMillis = savedAtMillis
    )
}

data class EditableProjectSummary(
    val projectId: String,
    val preset: MoviePreset,
    val clipCount: Int,
    val totalDurationSeconds: Double,
    val outputAspectRatio: OutputAspectRatio?,
    val outputQualityPreset: OutputQualityPreset,
    val savedAtMillis: Long,
    val isPinned: Boolean,
    val memo: String,
    val thumbnailUri: Uri?,
    val thumbnailUris: List<Uri>,
    val displayByteCount: Long
)

enum class EditableProjectPinResult {
    Toggled,
    LimitReached,
    Missing
}

object EditableProjectStore {
    private const val PreferencesName = "hanclip_editable_projects"
    private const val ProjectsKey = "projects"
    private const val ProjectsDirectoryName = "editable-projects"
    private const val ProjectMetadataFilename = "project.json"
    private const val ProjectMetadataBackupFilename = "project.json.bak"
    private const val MediaDirectoryName = "media"
    const val MaximumProjectCount = 10
    const val MaximumAiShotProjectCount = 2
    const val MaximumPinnedProjectCount = 5

    fun upsert(context: Context, project: DraftProject): DraftProject {
        val records = loadRecords(context).toMutableList()
        val existingIndex = records.indexOfFirst { it.project.projectId == project.projectId }
        val existing = records.getOrNull(existingIndex)
        val record = EditableProjectRecord(
            project = persistProjectMedia(
                context,
                project.copy(savedAtMillis = System.currentTimeMillis())
            ),
            isPinned = existing?.isPinned ?: false,
            memo = existing?.memo.orEmpty()
        )
        if (existingIndex >= 0) records[existingIndex] = record else records += record
        val keptRecords = enforceLimits(records)
        saveRecords(context, keptRecords)
        deleteProjectDirectories(
            context,
            records.map { it.project.projectId }.toSet() -
                keptRecords.map { it.project.projectId }.toSet()
        )
        return load(context, project.projectId) ?: record.project
    }

    fun load(context: Context, projectId: String): DraftProject? {
        return loadRecords(context).firstOrNull { it.project.projectId == projectId }?.project
    }

    fun list(context: Context): List<EditableProjectSummary> {
        return loadRecords(context)
            .sortedWith(
                compareByDescending<EditableProjectRecord> { it.isPinned }
                    .thenByDescending { it.project.savedAtMillis }
            )
            .map { record ->
                val renderableClips = record.project.clips.filter { it.isRenderableClip }
                EditableProjectSummary(
                    projectId = record.project.projectId,
                    preset = record.project.preset,
                    clipCount = renderableClips.size,
                    totalDurationSeconds = renderableClips.sumOf { it.durationSeconds },
                    outputAspectRatio = record.project.outputAspectRatio,
                    outputQualityPreset = record.project.outputQualityPreset,
                    savedAtMillis = record.project.savedAtMillis,
                    isPinned = record.isPinned,
                    memo = record.memo,
                    thumbnailUri = renderableClips.firstOrNull()?.thumbnailUri,
                    thumbnailUris = renderableClips.mapNotNull { it.thumbnailUri }.take(9),
                    displayByteCount = projectDisplayByteCount(context, record.project.projectId)
                )
            }
    }

    fun remove(context: Context, projectId: String) {
        saveRecords(context, loadRecords(context).filterNot { it.project.projectId == projectId })
        deleteProjectDirectories(context, setOf(projectId))
    }

    fun restoreWithoutUpdatingSavedAt(context: Context, project: DraftProject): DraftProject {
        val records = loadRecords(context).toMutableList()
        val index = records.indexOfFirst { it.project.projectId == project.projectId }
        val existing = records.getOrNull(index)
        val restored = EditableProjectRecord(
            project = persistProjectMedia(context, project),
            isPinned = existing?.isPinned ?: false,
            memo = existing?.memo.orEmpty()
        )
        if (index >= 0) records[index] = restored else records += restored
        saveRecords(context, records)
        return load(context, project.projectId) ?: restored.project
    }

    fun updateMemo(context: Context, projectId: String, memo: String) {
        saveRecords(
            context,
            loadRecords(context).map { record ->
                if (record.project.projectId == projectId) record.copy(memo = memo.trim()) else record
            }
        )
    }

    fun togglePinned(context: Context, projectId: String): EditableProjectPinResult {
        val records = loadRecords(context)
        val target = records.firstOrNull { it.project.projectId == projectId }
            ?: return EditableProjectPinResult.Missing
        if (!target.isPinned && records.count { it.isPinned } >= MaximumPinnedProjectCount) {
            return EditableProjectPinResult.LimitReached
        }
        saveRecords(
            context,
            records.map { record ->
                if (record.project.projectId == projectId) record.copy(isPinned = !record.isPinned) else record
            }
        )
        return EditableProjectPinResult.Toggled
    }

    private fun enforceLimits(records: List<EditableProjectRecord>): List<EditableProjectRecord> {
        var kept = records.toMutableList()
        fun removeOldestUnpinned(candidates: List<EditableProjectRecord>): Boolean {
            val target = candidates.filterNot { it.isPinned }.minByOrNull { it.project.savedAtMillis }
                ?: candidates.minByOrNull { it.project.savedAtMillis }
                ?: return false
            kept.removeAll { it.project.projectId == target.project.projectId }
            return true
        }
        while (kept.count { it.project.preset == MoviePreset.AiShot } > MaximumAiShotProjectCount) {
            val oldestAiShot = kept
                .filter { it.project.preset == MoviePreset.AiShot }
                .minByOrNull { it.project.savedAtMillis }
                ?: break
            kept.removeAll { it.project.projectId == oldestAiShot.project.projectId }
        }
        while (kept.size > MaximumProjectCount) {
            if (!removeOldestUnpinned(kept)) break
        }
        return kept
    }

    private fun loadRecords(context: Context): List<EditableProjectRecord> {
        val storedRecords = loadFileRecords(context)
        if (storedRecords.isNotEmpty()) return storedRecords

        val legacyRecords = loadLegacyRecords(context)
        if (legacyRecords.isNotEmpty()) {
            saveRecords(context, legacyRecords)
            return loadFileRecords(context)
        }
        return emptyList()
    }

    private fun loadFileRecords(context: Context): List<EditableProjectRecord> {
        return projectsRoot(context).listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { directory ->
                runCatching {
                    val json = loadProjectMetadata(directory) ?: error("프로젝트 메타데이터가 없습니다.")
                    val projectJson = json.getJSONObject("project")
                    val missingProjectId = projectJson.optString("projectId").isBlank()
                    val project = projectJson.toDraftProject().let { decoded ->
                        if (missingProjectId) decoded.copy(projectId = directory.name) else decoded
                    }
                    if (missingProjectId) {
                        val destination = File(directory, ProjectMetadataFilename)
                        writeVerifiedJsonAtomically(
                            metadata = json.put("project", project.toJson()),
                            staging = File(directory, "$ProjectMetadataFilename.tmp"),
                            destination = destination,
                            backup = File(directory, ProjectMetadataBackupFilename)
                        )
                    }
                    EditableProjectRecord(
                        project = project,
                        isPinned = json.optBoolean("isPinned", false),
                        memo = json.optString("memo", "")
                    )
                }.getOrNull()
            }
            .orEmpty()
    }

    private fun loadLegacyRecords(context: Context): List<EditableProjectRecord> {
        val raw = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(ProjectsKey, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val json = array.getJSONObject(index)
                EditableProjectRecord(
                    project = json.getJSONObject("project").toDraftProject(),
                    isPinned = json.optBoolean("isPinned", false),
                    memo = json.optString("memo", "")
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveRecords(context: Context, records: List<EditableProjectRecord>) {
        records.forEach { originalRecord ->
            val record = originalRecord.copy(
                project = persistProjectMedia(context, originalRecord.project)
            )
            val directory = projectDirectory(context, record.project.projectId).apply { mkdirs() }
            val metadata = JSONObject()
                .put("project", record.project.toJson())
                .put("isPinned", record.isPinned)
                .put("memo", record.memo)
            val destination = File(directory, ProjectMetadataFilename)
            val staging = File(directory, "$ProjectMetadataFilename.tmp")
            val backup = File(directory, ProjectMetadataBackupFilename)
            writeVerifiedJsonAtomically(metadata, staging, destination, backup)
        }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .remove(ProjectsKey)
            .apply()
    }

    private fun deleteProjectDirectories(context: Context, projectIds: Set<String>) {
        projectIds.forEach { projectId ->
            runCatching { projectDirectory(context, projectId).deleteRecursively() }
        }
    }

    private fun persistProjectMedia(context: Context, project: DraftProject): DraftProject {
        val directory = File(projectDirectory(context, project.projectId), MediaDirectoryName)
            .apply { mkdirs() }
        val persistedSources = mutableMapOf<String, Uri>()
        val clips = project.clips.map { clip ->
            val source = persistedSources.getOrPut(clip.sourceUri.toString()) {
                persistProjectUri(
                    context = context,
                    source = clip.sourceUri,
                    destination = File(
                        directory,
                        "source-${safeFilename(clip.id)}.${fileExtension(clip.sourceUri, clip.mediaKind)}"
                    )
                )
            }
            val livePhotoStill = clip.livePhotoStillUri?.let { stillUri ->
                persistProjectUri(
                    context = context,
                    source = stillUri,
                    destination = File(directory, "live-photo-${safeFilename(clip.id)}.jpg")
                )
            }
            val thumbnail = ensureProjectThumbnail(
                source = livePhotoStill ?: source,
                mediaKind = if (livePhotoStill != null) ClipMediaKind.Photo else clip.mediaKind,
                destination = File(directory, "thumbnail-${safeFilename(clip.id)}.jpg")
            )
            clip.copy(
                sourceUri = source,
                thumbnailUri = thumbnail,
                livePhotoStillUri = livePhotoStill
            )
        }
        val musicUri = project.backgroundMusicUri?.let { source ->
            persistProjectUri(
                context = context,
                source = source,
                destination = projectAssetDestination(
                    directory = directory,
                    prefix = "background-music",
                    source = source,
                    extension = fileExtension(source, null)
                )
            )
        }
        val watermarkSettings = project.watermarkSettings.let { settings ->
            val source = settings.customCopyrightIconPath
                .takeIf { settings.platform == WatermarkPlatform.Custom && it.isNotBlank() }
                ?.let(::File)
                ?.takeIf(File::isFile)
            val persistedPath = source?.let { sourceFile ->
                val sourceUri = Uri.fromFile(sourceFile)
                runCatching {
                    persistProjectUri(
                        context = context,
                        source = sourceUri,
                        destination = projectAssetDestination(
                            directory = directory,
                            prefix = "copyright-icon",
                            source = sourceUri,
                            extension = fileExtension(sourceUri, null)
                        )
                    ).path
                }.getOrNull()
            }
            if (settings.platform == WatermarkPlatform.Custom) {
                settings.copy(customCopyrightIconPath = persistedPath.orEmpty())
            } else {
                settings
            }
        }
        return project.copy(
            clips = clips,
            backgroundMusicUri = musicUri,
            watermarkSettings = watermarkSettings
        )
    }

    private fun projectAssetDestination(
        directory: File,
        prefix: String,
        source: Uri,
        extension: String
    ): File {
        val sourceFile = source.takeIf { it.scheme == "file" }?.path?.let(::File)
        val isExistingProjectAsset = sourceFile?.let { file ->
            runCatching {
                file.parentFile?.canonicalFile == directory.canonicalFile &&
                    (file.name.startsWith("$prefix-") || file.name.startsWith("$prefix."))
            }.getOrDefault(false)
        } == true
        return if (isExistingProjectAsset) {
            requireNotNull(sourceFile)
        } else {
            File(directory, "$prefix-${UUID.randomUUID()}.$extension")
        }
    }

    private fun ensureProjectThumbnail(
        source: Uri,
        mediaKind: ClipMediaKind,
        destination: File
    ): Uri? {
        if (destination.isFile && destination.length() > 0L) return Uri.fromFile(destination)
        val sourcePath = source.takeIf { it.scheme == "file" }?.path ?: return null
        val decoded = runCatching {
            if (mediaKind == ClipMediaKind.Video) {
                MediaMetadataRetriever().let { retriever ->
                    try {
                        retriever.setDataSource(sourcePath)
                        retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    } finally {
                        retriever.release()
                    }
                }
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(sourcePath, bounds)
                val longest = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
                var sampleSize = 1
                while (longest / (sampleSize * 2) >= ProjectThumbnailLongEdgePx) sampleSize *= 2
                BitmapFactory.decodeFile(
                    sourcePath,
                    BitmapFactory.Options().apply { inSampleSize = sampleSize }
                )
            }
        }.getOrNull() ?: return null
        val thumbnail = decoded.scaledToLongEdge(ProjectThumbnailLongEdgePx)
        return runCatching {
            destination.parentFile?.mkdirs()
            destination.outputStream().use { output ->
                check(thumbnail.compress(Bitmap.CompressFormat.JPEG, 84, output))
            }
            Uri.fromFile(destination)
        }.getOrNull().also {
            if (thumbnail !== decoded) thumbnail.recycle()
            decoded.recycle()
        }
    }

    private fun Bitmap.scaledToLongEdge(targetLongEdge: Int): Bitmap {
        val longest = maxOf(width, height)
        if (longest <= targetLongEdge) return this
        val scale = targetLongEdge.toFloat() / longest.toFloat()
        return Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun persistProjectUri(
        context: Context,
        source: Uri,
        destination: File,
        replaceExisting: Boolean = false
    ): Uri {
        if (source.scheme == "sample") return source
        val sourceFile = source.takeIf { it.scheme == "file" }?.path?.let(::File)
        if (sourceFile?.canonicalPath == destination.canonicalPath) return Uri.fromFile(destination)
        if (!replaceExisting && destination.isFile && destination.length() > 0L) {
            return Uri.fromFile(destination)
        }
        destination.parentFile?.mkdirs()
        val staging = File(destination.parentFile, "${destination.name}.tmp-${UUID.randomUUID()}")
        try {
            if (sourceFile?.isFile == true) {
                runCatching { Files.createLink(staging.toPath(), sourceFile.toPath()) }
                    .recoverCatching { sourceFile.copyTo(staging, overwrite = true) }
                    .getOrThrow()
            } else {
                context.contentResolver.openInputStream(source)?.use { input ->
                    FileOutputStream(staging).use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                } ?: error("프로젝트 미디어를 열 수 없습니다: $source")
            }
            check(staging.isFile && staging.length() > 0L) { "프로젝트 미디어 복사본이 비어 있습니다: $source" }
            if (replaceExisting) {
                runCatching {
                    Files.move(
                        staging.toPath(),
                        destination.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }.recoverCatching {
                    Files.move(
                        staging.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }.getOrThrow()
            } else {
                check(staging.renameTo(destination)) { "프로젝트 미디어를 확정하지 못했습니다: $source" }
            }
        } finally {
            if (staging.exists()) staging.delete()
        }
        return Uri.fromFile(destination)
    }

    private fun loadProjectMetadata(directory: File): JSONObject? {
        val destination = File(directory, ProjectMetadataFilename)
        val backup = File(directory, ProjectMetadataBackupFilename)
        return loadPrimaryOrBackup(destination, backup) { raw ->
            JSONObject(raw).also { metadata -> metadata.getJSONObject("project") }
        }
    }

    private fun writeVerifiedJsonAtomically(
        metadata: JSONObject,
        staging: File,
        destination: File,
        backup: File
    ) {
        val parent = staging.parentFile ?: error("프로젝트 메타데이터 디렉터리가 없습니다.")
        check(parent.mkdirs() || parent.isDirectory) { "프로젝트 메타데이터 디렉터리를 준비하지 못했습니다." }
        if (staging.exists()) staging.delete()
        FileOutputStream(staging).use { output ->
            output.write(metadata.toString().toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        JSONObject(staging.readText()).getJSONObject("project")

        var movedExisting = false
        try {
            if (destination.exists()) {
                if (backup.exists()) backup.delete()
                check(destination.renameTo(backup)) { "기존 프로젝트 메타데이터를 백업하지 못했습니다." }
                movedExisting = true
            }
            check(staging.renameTo(destination)) { "새 프로젝트 메타데이터를 확정하지 못했습니다." }
            JSONObject(destination.readText()).getJSONObject("project")
        } catch (error: Throwable) {
            if (!destination.exists() && movedExisting) backup.renameTo(destination)
            throw error
        } finally {
            if (staging.exists()) staging.delete()
        }
    }

    private fun projectsRoot(context: Context): File {
        return File(context.filesDir, ProjectsDirectoryName).apply { mkdirs() }
    }

    private fun projectDirectory(context: Context, projectId: String): File {
        return File(projectsRoot(context), safeProjectDirectoryName(projectId))
    }

    private fun projectDisplayByteCount(context: Context, projectId: String): Long {
        return projectDirectory(context, projectId)
            .walkTopDown()
            .filter(File::isFile)
            .sumOf(File::length)
    }

    private fun safeProjectDirectoryName(projectId: String): String = safeFilename(projectId)

    private fun safeFilename(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { UUID.randomUUID().toString() }
    }

    private fun fileExtension(uri: Uri, mediaKind: ClipMediaKind?): String {
        return uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.matches(Regex("[a-z0-9]{1,5}")) }
            ?: if (mediaKind == ClipMediaKind.Video) "mp4" else "jpg"
    }

    private const val ProjectThumbnailLongEdgePx = 480
}

private data class EditableProjectRecord(
    val project: DraftProject,
    val isPinned: Boolean,
    val memo: String
)

private fun ClipItem.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("sourceUri", sourceUri.toString())
        .put("thumbnailUri", thumbnailUri?.toString())
        .put("durationSeconds", durationSeconds)
        .put("photoDurationSeconds", photoDurationSeconds)
        .put("livePhotoDurationSeconds", livePhotoDurationSeconds)
        .put("livePhotoStillUri", livePhotoStillUri?.toString())
        .put("isLivePhoto", isLivePhoto)
        .put("livePhotoMode", livePhotoMode.name)
        .put("mediaKind", mediaKind.name)
        .put("sourceDurationSeconds", sourceDurationSeconds)
        .put("trimStartSeconds", trimStartSeconds)
        .put("audioWaveform", JSONArray().also { array ->
            audioWaveform.forEach(array::put)
        })
        .put("audioPeakTimeSeconds", audioPeakTimeSeconds)
        .put("audioPeakTimesSeconds", JSONArray().also { array ->
            audioPeakTimesSeconds.forEach(array::put)
        })
        .put("videoSegmentMode", videoSegmentMode.name)
        .put("isVideoSegmentParent", isVideoSegmentParent)
        .put("videoSegmentParentId", videoSegmentParentId)
        .put("isVideoSegmentSelected", isVideoSegmentSelected)
        .put("photoSimilarityFingerprint", JSONArray().also { array ->
            photoSimilarityFingerprint.forEach(array::put)
        })
        .put("sourceCreatedAtMillis", sourceCreatedAtMillis)
        .put("originalSourceUriString", originalSourceUriString)
        .put("sourceLatitude", sourceLatitude)
        .put("sourceLongitude", sourceLongitude)
        .put("sourceLocationName", sourceLocationName)
        .put("similarPhotoGroupId", similarPhotoGroupId)
        .put("similarPhotoGroupIndex", similarPhotoGroupIndex)
        .put("similarPhotoGroupCount", similarPhotoGroupCount)
        .put("isSimilarPhotoGroupRepresentative", isSimilarPhotoGroupRepresentative)
        .put("sourceWidth", sourceWidth)
        .put("sourceHeight", sourceHeight)
}

internal data class NormalizedClipTiming(
    val durationSeconds: Double,
    val photoDurationSeconds: Double,
    val sourceDurationSeconds: Double?,
    val trimStartSeconds: Double
)

internal fun normalizeStoredClipTiming(
    isTimelineVideo: Boolean,
    durationSeconds: Double,
    photoDurationSeconds: Double,
    sourceDurationSeconds: Double?,
    trimStartSeconds: Double
): NormalizedClipTiming {
    val safePhotoDuration = photoDurationSeconds.takeIf(Double::isFinite)
        ?.coerceIn(0.1, 30.0) ?: 2.0
    if (!isTimelineVideo) {
        val safeDuration = durationSeconds.takeIf(Double::isFinite)
            ?.coerceIn(0.1, 30.0) ?: safePhotoDuration
        return NormalizedClipTiming(safeDuration, safePhotoDuration, sourceDurationSeconds, 0.0)
    }

    val safeSourceDuration = sourceDurationSeconds
        ?.takeIf { it.isFinite() && it > 0.0 }
        ?.coerceAtLeast(0.1)
    val safeDuration = durationSeconds.takeIf { it.isFinite() && it > 0.0 }
        ?.coerceAtLeast(0.1) ?: 0.1
    if (safeSourceDuration == null) {
        return NormalizedClipTiming(
            durationSeconds = safeDuration,
            photoDurationSeconds = safePhotoDuration,
            sourceDurationSeconds = null,
            trimStartSeconds = trimStartSeconds.takeIf(Double::isFinite)?.coerceAtLeast(0.0) ?: 0.0
        )
    }

    val boundedDuration = safeDuration.coerceAtMost(safeSourceDuration)
    val boundedStart = trimStartSeconds.takeIf(Double::isFinite)
        ?.coerceIn(0.0, safeSourceDuration - boundedDuration) ?: 0.0
    return NormalizedClipTiming(
        durationSeconds = boundedDuration,
        photoDurationSeconds = safePhotoDuration,
        sourceDurationSeconds = safeSourceDuration,
        trimStartSeconds = boundedStart
    )
}

private fun JSONObject.toClipItem(): ClipItem {
    val mediaKind = enumValueOrDefault(optString("mediaKind"), ClipMediaKind.Photo)
    val livePhotoMode = enumValueOrDefault(optString("livePhotoMode"), LivePhotoMode.Still)
    val timing = normalizeStoredClipTiming(
        isTimelineVideo = mediaKind == ClipMediaKind.Video ||
            (mediaKind == ClipMediaKind.LivePhoto && livePhotoMode == LivePhotoMode.Motion),
        durationSeconds = optDouble("durationSeconds", 2.0),
        photoDurationSeconds = optDouble("photoDurationSeconds", optDouble("durationSeconds", 2.0)),
        sourceDurationSeconds = optNullableDouble("sourceDurationSeconds"),
        trimStartSeconds = optDouble("trimStartSeconds", 0.0)
    )
    return ClipItem(
        id = getString("id"),
        sourceUri = Uri.parse(getString("sourceUri")),
        thumbnailUri = optString("thumbnailUri")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let(Uri::parse),
        durationSeconds = timing.durationSeconds,
        photoDurationSeconds = timing.photoDurationSeconds,
        livePhotoDurationSeconds = optNullableDouble("livePhotoDurationSeconds"),
        livePhotoStillUri = optString("livePhotoStillUri")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let(Uri::parse),
        isLivePhoto = optBoolean("isLivePhoto", false),
        livePhotoMode = livePhotoMode,
        mediaKind = mediaKind,
        sourceDurationSeconds = timing.sourceDurationSeconds,
        trimStartSeconds = timing.trimStartSeconds,
        audioWaveform = optDoubleList("audioWaveform"),
        audioPeakTimeSeconds = optNullableDouble("audioPeakTimeSeconds"),
        audioPeakTimesSeconds = optDoubleList("audioPeakTimesSeconds"),
        videoSegmentMode = enumValueOrDefault(optString("videoSegmentMode"), VideoSegmentMode.Single),
        isVideoSegmentParent = optBoolean("isVideoSegmentParent", false),
        videoSegmentParentId = optString("videoSegmentParentId")
            .takeIf { it.isNotBlank() && it != "null" },
        isVideoSegmentSelected = optBoolean("isVideoSegmentSelected", true),
        photoSimilarityFingerprint = optIntList("photoSimilarityFingerprint"),
        sourceCreatedAtMillis = optNullableLong("sourceCreatedAtMillis"),
        originalSourceUriString = optString("originalSourceUriString")
            .takeIf { it.isNotBlank() && it != "null" },
        sourceLatitude = optNullableDouble("sourceLatitude"),
        sourceLongitude = optNullableDouble("sourceLongitude"),
        sourceLocationName = optString("sourceLocationName")
            .takeIf { it.isNotBlank() && it != "null" },
        similarPhotoGroupId = optString("similarPhotoGroupId")
            .takeIf { it.isNotBlank() && it != "null" },
        similarPhotoGroupIndex = optInt("similarPhotoGroupIndex", 0),
        similarPhotoGroupCount = optInt("similarPhotoGroupCount", 1).coerceAtLeast(1),
        isSimilarPhotoGroupRepresentative = optBoolean("isSimilarPhotoGroupRepresentative", true),
        sourceWidth = optInt("sourceWidth", 1),
        sourceHeight = optInt("sourceHeight", 1)
    )
}

private fun WatermarkSettings.toJson(): JSONObject {
    return JSONObject()
        .put("isEnabled", isEnabled)
        .put("logoEnabled", logoEnabled)
        .put("address", address)
        .put("platform", platform.storedValue)
        .put("text", text)
        .put("position", position.name)
        .put("fontName", fontName)
        .put("textColorHex", textColorHex)
        .put("shadowEnabled", shadowEnabled)
        .put("shadowOpacity", shadowOpacity)
        .put("shadowColorHex", shadowColorHex)
        .put("lineSpacing", lineSpacing.name)
        .put("lineSpacingScale", lineSpacingScale)
        .put("fontSize", fontSize.name)
        .put("logoColorHex", logoColorHex)
        .put("logoShadowColorHex", logoShadowColorHex)
        .put("logoShadowOpacity", logoShadowOpacity)
        .put("copyrightPosition", copyrightPosition.name)
        .put("copyrightIconColorMode", copyrightIconColorMode.name)
        .put("copyrightIconColorHex", copyrightIconColorHex)
        .put("customCopyrightIconPath", customCopyrightIconPath)
        .put("includesEndingInfoCard", includesEndingInfoCard)
        .put("endingInfoCardDuration", endingInfoCardDuration)
        .put("endingInfoCardTheme", endingInfoCardTheme.name)
        .put("endingInfoCardVariation", endingInfoCardVariation)
}

private fun JSONObject.toWatermarkSettings(): WatermarkSettings {
    return WatermarkSettings(
        isEnabled = optBoolean("isEnabled", false),
        logoEnabled = optBoolean("logoEnabled", false),
        address = optString("address", ""),
        platform = WatermarkPlatform.fromStoredValue(optString("platform", "hanclip")),
        text = optString("text", ""),
        position = enumValueOrDefault(optString("position"), WatermarkPosition.TopLeading),
        fontName = optString("fontName", "pretendard"),
        textColorHex = optString("textColorHex", "#FFFFFF"),
        shadowEnabled = optBoolean("shadowEnabled", true),
        shadowOpacity = optDouble("shadowOpacity", 0.2),
        shadowColorHex = optString("shadowColorHex", "#000000"),
        lineSpacing = enumValueOrDefault(optString("lineSpacing"), WatermarkLineSpacing.Normal),
        lineSpacingScale = WatermarkLineSpacing.normalize(
            optDouble("lineSpacingScale", WatermarkLineSpacing.DefaultScale)
        ),
        fontSize = enumValueOrDefault(optString("fontSize"), WatermarkFontSize.Large),
        logoColorHex = optString("logoColorHex", "#007644"),
        logoShadowColorHex = optString("logoShadowColorHex", "#29AB87"),
        logoShadowOpacity = optDouble("logoShadowOpacity", 0.5).coerceIn(0.0, 1.0),
        copyrightPosition = enumValueOrDefault(
            optString("copyrightPosition"),
            WatermarkPosition.BottomTrailing
        ),
        copyrightIconColorMode = CopyrightIconColorMode.fromStoredValue(
            optString("copyrightIconColorMode")
        ),
        copyrightIconColorHex = optString("copyrightIconColorHex", "#007644"),
        customCopyrightIconPath = optString("customCopyrightIconPath", ""),
        includesEndingInfoCard = optBoolean("includesEndingInfoCard", false),
        endingInfoCardDuration = optDouble("endingInfoCardDuration", 2.0).coerceIn(1.0, 10.0),
        endingInfoCardTheme = enumValueOrDefault(
            optString("endingInfoCardTheme"),
            EndingInfoCardTheme.Caption
        ),
        endingInfoCardVariation = optInt("endingInfoCardVariation", 0).coerceAtLeast(0)
    )
}

private fun JSONObject.optDoubleList(key: String): List<Double> {
    val array = optJSONArray(key) ?: return emptyList()
    return List(array.length()) { index -> array.optDouble(index, 0.0) }
}

private fun JSONObject.optIntList(key: String): List<Int> {
    val array = optJSONArray(key) ?: return emptyList()
    return List(array.length()) { index -> array.optInt(index, 0) }
}

private fun JSONObject.optNullableDouble(key: String): Double? {
    if (!has(key) || isNull(key)) return null
    return optDouble(key)
}

private fun JSONObject.optNullableLong(key: String): Long? {
    if (!has(key) || isNull(key)) return null
    return optLong(key)
}

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? {
    return runCatching { enumValueOf<T>(value) }.getOrNull()
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T {
    return enumValueOrNull<T>(value) ?: default
}
