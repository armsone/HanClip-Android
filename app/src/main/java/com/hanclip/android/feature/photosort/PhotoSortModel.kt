package com.hanclip.android.feature.photosort

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AtomicFile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

data class SortPhoto(val uri: Uri, val name: String, val path: String, val volume: String,
    val size: Long, val score: Double, val thumbnail: Bitmap)
data class PhotoAlbum(val volume: String, val path: String) {
    val label get() = "${path.trimEnd('/').substringAfterLast('/')} · $path ($volume)"
}

/** Reads and moves only confirmed local MediaStore rows. Never imports or deletes an asset. */
class PhotoSortModel(application: Application) : AndroidViewModel(application) {
    private val resolver = application.contentResolver
    var photos by mutableStateOf<List<SortPhoto>>(emptyList()); private set
    var albums by mutableStateOf<List<PhotoAlbum>>(emptyList()); private set
    var recommendedCount by mutableStateOf(0); private set
    var count by mutableStateOf(1)
    var destination by mutableStateOf<PhotoAlbum?>(null)
    var status by mutableStateOf("한양에게 추천을 요청하면 사진을 분석해 좋은 사진의 장수를 제안합니다."); private set
    var busy by mutableStateOf(false); private set
    var moving by mutableStateOf(false); private set
    var completed by mutableStateOf<Set<String>>(emptySet()); private set
    private var job: Job? = null
    private var session = ""
    private var journal: AtomicFile? = null
    private var pending = emptyList<SortPhoto>()
    private var frozen = false
    val selected get() = if (frozen) pending else photos.sortedWith(compareByDescending<SortPhoto> { it.score }
        .thenBy { photos.indexOf(it) }).take(count.coerceIn(1, max(1, photos.size)))
        .sortedBy { photos.indexOf(it) }
    val canChange get() = !busy && !frozen
    val remaining get() = pending.filter { it.uri.toString() !in completed }

    fun load(sessionID: String, input: List<Uri>) {
        require(java.util.UUID.fromString(sessionID).toString() == sessionID)
        if (busy || photos.isNotEmpty()) return
        session = sessionID
        journal = AtomicFile(File(getApplication<Application>().noBackupFilesDir, "photo-sort-$session.json"))
        busy = true
        job = viewModelScope.launch {
            try {
                require(Build.VERSION.SDK_INT >= 30) { "원본 앨범 이동은 Android 11 이상에서 지원합니다." }
                require(input.isNotEmpty() && input.size <= 200) { "사진을 1~200장 선택해 공유해 주세요." }
                val result = withContext(Dispatchers.IO) {
                    val canonical = input.map { original(it) }.distinct()
                    canonical.mapIndexed { index, uri ->
                        ensureActive()
                        withContext(Dispatchers.Main) { status = "사진 분석 ${index + 1}/${canonical.size}" }
                        inspect(uri, analyze = true)
                    }
                }
                photos = result
                recommendedCount = recommendation(result)
                count = recommendedCount
                albums = withContext(Dispatchers.IO) { readAlbums(result.map { it.volume }.toSet()) }
                restore()
                status = if (frozen) "이전 이동 결과를 복원했습니다. 완료 사진은 재시도에서 제외합니다."
                    else "한양이 ${photos.size}장 중 ${recommendedCount}장을 추천합니다."
            } catch (e: Exception) { photos = emptyList(); status = e.message ?: "사진을 읽지 못했습니다. 다시 공유해 주세요." }
            finally { busy = false }
        }
    }

    private fun recommendation(candidates: List<SortPhoto>): Int {
        val best = candidates.maxOfOrNull { it.score } ?: return 0
        val tolerance = max(abs(best) * 0.1, 0.01)
        return candidates.count { it.score >= best - tolerance }.coerceAtLeast(1)
    }

    private fun original(shared: Uri): Uri {
        require(shared.scheme == "content") { "휴대전화 사진 원본만 지원합니다." }
        val uri = if (shared.authority == MediaStore.AUTHORITY) shared
            else MediaStore.getMediaUri(getApplication(), shared)
        require(uri != null && uri.authority == MediaStore.AUTHORITY) {
            "클라우드 또는 다른 앱의 사진입니다. 휴대전화 갤러리의 원본을 선택해 주세요."
        }
        val parts = uri.pathSegments
        require(parts.size == 4 && parts[1] == "images" && parts[2] == "media" &&
            parts[3].toLongOrNull() != null && parts[0] != "internal") {
            "정확한 저장소의 사진 원본을 확인하지 못했습니다. 복사하거나 추정하지 않습니다."
        }
        val volume = if (parts[0] == "external") resolver.query(uri, arrayOf(MediaStore.MediaColumns.VOLUME_NAME), null, null, null)?.use { c ->
            require(c.moveToFirst() && c.count == 1) { "원본 저장소를 확인하지 못했습니다." }; c.getString(0)
        } else parts[0]
        require(volume != null && MediaStore.getExternalVolumeNames(getApplication()).contains(volume)) {
            "사진 저장소가 연결되어 있지 않습니다."
        }
        return ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri(volume!!), parts[3].toLong())
    }

    private fun inspect(uri: Uri, analyze: Boolean): SortPhoto {
        val columns = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.IS_PENDING,
            MediaStore.Images.Media.IS_TRASHED)
        val metadata = resolver.query(uri, columns, null, null, null)?.use { cursor ->
            require(cursor.moveToFirst() && cursor.count == 1) { "사진 원본이 없어졌습니다." }
            require(cursor.getInt(4) == 0 && cursor.getInt(5) == 0 && cursor.getString(3)?.startsWith("image/") == true) {
                "사용할 수 없는 사진 원본입니다."
            }
            Triple(cursor.getString(0), cursor.getString(1), cursor.getLong(2))
        } ?: error("사진 원본을 읽을 수 없습니다.")
        require(metadata.first.isNotBlank() && metadata.second.isNotBlank() && metadata.third > 0)
        if (!analyze) return SortPhoto(uri, metadata.first, metadata.second, uri.pathSegments[0], metadata.third,
            0.0, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        var pixels = 1L
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            pixels = info.size.width.toLong() * info.size.height
            val scale = min(1.0, 768.0 / max(info.size.width, info.size.height))
            decoder.setTargetSize(max(1, (info.size.width * scale).toInt()), max(1, (info.size.height * scale).toInt()))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        try {
            val w = bitmap.width; val h = bitmap.height
            val colors = IntArray(w * h); bitmap.getPixels(colors, 0, w, 0, 0, w, h)
            val gray = DoubleArray(colors.size) { i -> val c = colors[i]
                (0.2126 * ((c shr 16) and 255) + 0.7152 * ((c shr 8) and 255) + 0.0722 * (c and 255)) / 255.0 }
            var sum = 0.0; var square = 0.0; var n = 0
            for (y in 1 until h - 1) for (x in 1 until w - 1) {
                val i = y * w + x
                val lap = gray[i - 1] + gray[i + 1] + gray[i - w] + gray[i + w] - 4 * gray[i]
                sum += lap; square += lap * lap; n++
            }
            val variance = if (n > 0) max(0.0, square / n - (sum / n) * (sum / n)) else 0.0
            val sharpness = min(1.0, variance * 20)
            val brightness = colors.sumOf { c -> (((c shr 16) and 255) + ((c shr 8) and 255) + (c and 255)) / 765.0 } / colors.size
            val exposure = max(0.0, 1.0 - abs(brightness - 0.5) * 2)
            val score = sharpness * 0.4 + exposure * 0.2 + ln(max(1L, pixels).toDouble()) / 100
            val scale = min(1.0, 160.0 / max(w, h))
            val thumbnail = Bitmap.createScaledBitmap(bitmap, max(1, (w * scale).toInt()), max(1, (h * scale).toInt()), true)
            return SortPhoto(uri, metadata.first, metadata.second, uri.pathSegments[0], metadata.third, score,
                if (thumbnail === bitmap) bitmap.copy(Bitmap.Config.ARGB_8888, false) else thumbnail)
        } finally { bitmap.recycle() }
    }

    private fun readAlbums(volumes: Set<String>): List<PhotoAlbum> = volumes.flatMap { volume ->
        val paths = mutableSetOf<String>()
        resolver.query(MediaStore.Images.Media.getContentUri(volume), arrayOf(MediaStore.Images.Media.RELATIVE_PATH),
            "${MediaStore.Images.Media.IS_PENDING}=0 AND ${MediaStore.Images.Media.IS_TRASHED}=0", null, null)?.use { c ->
            while (c.moveToNext()) c.getString(0)?.let { if (validPath(it)) paths += it }
        }
        paths.sorted().map { PhotoAlbum(volume, it) }
    }
    private fun validPath(path: String) = (path.startsWith("Pictures/") || path.startsWith("DCIM/")) &&
        path.endsWith('/') && path.split('/').none { it == "." || it == ".." || it.contains('\\') }

    suspend fun prepare(): List<Uri> = withContext(Dispatchers.IO) {
        val target = destination ?: error("앨범을 선택해 주세요.")
        require(validPath(target.path)) { "사진 앨범 경로가 올바르지 않습니다." }
        val chosen = if (frozen) remaining else selected
        require(chosen.isNotEmpty()) { "모든 사진의 이동이 완료되었습니다." }
        require(chosen.all { it.volume == target.volume }) { "다른 저장소로 이동할 수 없습니다. 같은 저장소의 앨범을 고르세요." }
        require(chosen.map { it.name.lowercase() }.distinct().size == chosen.size) { "선택 사진의 파일명이 겹칩니다. 덮어쓰지 않고 중단했습니다." }
        val targetNames = mutableMapOf<String, Long>()
        resolver.query(MediaStore.Images.Media.getContentUri(target.volume),
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME),
            "${MediaStore.Images.Media.RELATIVE_PATH}=?", arrayOf(target.path), null)?.use { c ->
            while (c.moveToNext()) targetNames[c.getString(1).lowercase()] = c.getLong(0)
        } ?: error("대상 앨범의 파일명 충돌을 확인하지 못했습니다.")
        chosen.forEach { photo ->
            val live = inspect(photo.uri, false)
            live.thumbnail.recycle()
            require(live.name == photo.name && live.size == photo.size &&
                (live.path == photo.path || (frozen && live.path == target.path))) { "사진 정보가 바뀌었습니다. 다시 공유해 주세요." }
            require(targetNames[photo.name.lowercase()]?.let { it == ContentUris.parseId(photo.uri) } != false) {
                "대상 앨범에 같은 이름의 사진이 있습니다. 덮어쓰지 않고 중단했습니다."
            }
        }
        chosen.map { it.uri }
    }

    fun requestMove(requestConsent: (List<Uri>) -> Unit) {
        if (busy) return
        busy = true
        job = viewModelScope.launch {
            try {
                val uris = prepare()
                if (!frozen) { pending = selected; frozen = true }
                // Every consent attempt requires a durable journal, including retries after storage failure.
                withContext(Dispatchers.IO) { writeJournal() }
                requestConsent(uris)
            } catch (e: Exception) { status = e.message ?: "이동 준비에 실패했습니다."; busy = false }
        }
    }
    fun consentResult(allowed: Boolean) {
        if (!allowed) {
            if (completed.isEmpty()) { journal?.delete(); pending = emptyList(); frozen = false }
            status = "이동을 취소했습니다. 원본은 유지됩니다."; busy = false; return
        }
        moving = true
        job = viewModelScope.launch {
            var failure: String? = null
            try {
                prepare() // Revalidate every pending row and collision after the system consent dialog.
                withContext(Dispatchers.IO) { writeJournal() }
                val target = destination ?: error("앨범이 없습니다.")
                for (photo in remaining) {
                    try {
                        withContext(Dispatchers.IO) {
                            val live = inspect(photo.uri, false); live.thumbnail.recycle()
                            require(live.name == photo.name && live.size == photo.size &&
                                (live.path == photo.path || live.path == target.path)) {
                                "이동 직전 원본 정보가 바뀌었습니다. 중단했습니다."
                            }
                            if (live.path != target.path) {
                                require(resolver.update(photo.uri, ContentValues().apply {
                                    put(MediaStore.Images.Media.RELATIVE_PATH, target.path)
                                }, null, null) == 1) { "원본 이동이 반영되지 않았습니다." }
                            }
                            val after = inspect(photo.uri, false); after.thumbnail.recycle()
                            require(after.path == target.path && after.size == photo.size && after.name == photo.name) {
                                "이동 결과를 확인하지 못했습니다."
                            }
                        }
                        completed = completed + photo.uri.toString()
                        writeJournal()
                        status = "${completed.size}/${pending.size}장 이동 완료"
                    } catch (e: Exception) { failure = e.message ?: "사진 이동 실패"; break }
                }
                status = if (failure == null) "${completed.size}장 원본을 ${target.path} 앨범으로 이동했습니다."
                    else "${completed.size}/${pending.size}장 이동 완료. $failure 나머지는 다시 시도할 수 있습니다."
            } catch (e: Exception) { status = "${completed.size}/${pending.size}장 이동 완료. ${e.message}" }
            finally { moving = false; busy = false }
        }
    }
    fun cancelAnalysis() { if (!moving) job?.cancel() }
    fun launchFailure(e: Exception) { busy = false; status = e.message ?: "시스템 이동 동의를 열지 못했습니다." }
    private fun writeJournal() {
        val target = destination ?: return
        val json = JSONObject().put("volume", target.volume).put("path", target.path).put("count", count)
            .put("selected", JSONArray(pending.map { it.uri.toString() })).put("completed", JSONArray(completed.toList()))
        val file = journal ?: error("이동 기록을 준비하지 못했습니다.")
        val stream = file.startWrite()
        try { stream.write(json.toString().toByteArray()); file.finishWrite(stream) }
        catch (e: Exception) { file.failWrite(stream); throw e }
    }
    private fun restore() {
        val file = journal ?: return
        if (!file.baseFile.exists()) return
        val json = JSONObject(file.openRead().bufferedReader().use { it.readText() })
        destination = PhotoAlbum(json.getString("volume"), json.getString("path"))
        count = json.getInt("count")
        val ids = json.getJSONArray("selected").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
        pending = photos.filter { it.uri.toString() in ids }
        require(pending.size == ids.size) { "이전 이동 사진의 접근 권한을 다시 확인해 주세요." }
        completed = json.getJSONArray("completed").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
        // A process death between update and journal commit is reconciled against the same row ID.
        completed = completed + pending.filter { it.path == destination?.path }.map { it.uri.toString() }
        frozen = true
    }
}
