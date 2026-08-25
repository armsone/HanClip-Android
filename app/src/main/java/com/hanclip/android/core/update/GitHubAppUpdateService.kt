package com.hanclip.android.core.update

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class GitHubAppRelease(val versionCode: Int, val tagName: String, val assetName: String, val apkUrl: URL, val assetSizeBytes: Long, val sha256: String = "", val releaseNotes: String = "")

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data class Checking(val isManual: Boolean) : AppUpdateState
    data class Latest(val currentVersionCode: Int) : AppUpdateState
    data class Available(val release: GitHubAppRelease, val message: String? = null) : AppUpdateState
    data class Downloading(val release: GitHubAppRelease, val progressPercent: Int?, val isAutomatic: Boolean) : AppUpdateState
    data class Ready(val release: GitHubAppRelease, val apkFile: File) : AppUpdateState
    data class Failed(val message: String, val canRetry: Boolean = true) : AppUpdateState
}

class GitHubAppUpdateService(context: Context, private val currentVersionCode: Int) : Closeable {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences("app_update", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("HanClipUpdater"))
    private val mutableState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    private val isChecking = AtomicBoolean(false)
    private var job: Job? = null
    private var downloadId: Long? = null
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    var automaticDownloadEnabled: Boolean
        get() = preferences.getBoolean("automatic_download_enabled", true)
        set(value) { preferences.edit().putBoolean("automatic_download_enabled", value).apply() }

    fun checkForUpdate(isManual: Boolean = false) {
        if (mutableState.value is AppUpdateState.Downloading || !isChecking.compareAndSet(false, true)) return
        mutableState.value = AppUpdateState.Checking(isManual)
        scope.launch {
            try {
                val result = runCatching { fetchLatestRelease() }
                val release = result.getOrNull()
                when {
                    result.isFailure -> mutableState.value = if (isManual) AppUpdateState.Failed("최신 버전을 확인하지 못했습니다. 인터넷 연결을 확인한 뒤 다시 시도해 주세요.") else AppUpdateState.Idle
                    release == null || release.versionCode <= currentVersionCode -> mutableState.value = if (isManual) AppUpdateState.Latest(currentVersionCode) else AppUpdateState.Idle
                    !isManual && automaticDownloadEnabled -> { mutableState.value = AppUpdateState.Available(release); startDownload(release, true) }
                    else -> mutableState.value = AppUpdateState.Available(release)
                }
            } finally { isChecking.set(false) }
        }
    }

    fun download(release: GitHubAppRelease) = startDownload(release, false)
    fun cancelDownload() {
        downloadId?.let { manager.remove(it) }; downloadId = null; job?.cancel(); job = null
        val release = (mutableState.value as? AppUpdateState.Downloading)?.release
        mutableState.value = release?.let { AppUpdateState.Available(it, "다운로드를 취소했습니다. 다시 받을 수 있습니다.") } ?: AppUpdateState.Idle
    }
    fun dismiss() { if (mutableState.value !is AppUpdateState.Downloading) mutableState.value = AppUpdateState.Idle }
    override fun close() { job?.cancel(); scope.cancel() }

    private fun startDownload(release: GitHubAppRelease, automatic: Boolean) {
        if (job?.isActive == true) return
        val request = DownloadManager.Request(Uri.parse(release.apkUrl.toExternalForm()))
            .setTitle("HanClip ${release.versionCode}").setDescription("업데이트 다운로드 중")
            .setMimeType(APK_MIME).setAllowedOverMetered(!automatic).setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val id = runCatching { manager.enqueue(request) }.getOrElse { mutableState.value = AppUpdateState.Failed("다운로드를 시작하지 못했습니다. 다시 시도해 주세요."); return }
        downloadId = id
        mutableState.value = AppUpdateState.Downloading(release, 0, automatic)
        job = scope.launch {
            try { monitor(id, release, automatic) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { manager.remove(id); mutableState.value = AppUpdateState.Failed("업데이트를 받거나 검증하지 못했습니다. 다시 시도해 주세요.") }
            finally { downloadId = null }
        }
    }

    private suspend fun monitor(id: Long, release: GitHubAppRelease, automatic: Boolean) {
        while (true) {
            val snapshot = query(id)
            when (snapshot.first) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uri = manager.getUriForDownloadedFile(id) ?: throw IOException("Downloaded file unavailable")
                    val file = copyAndVerify(uri, release); manager.remove(id)
                    mutableState.value = AppUpdateState.Ready(release, file); return
                }
                DownloadManager.STATUS_FAILED -> throw IOException("DownloadManager failed")
                else -> mutableState.value = AppUpdateState.Downloading(release, snapshot.second, automatic)
            }
            delay(300)
        }
    }

    private fun query(id: Long): Pair<Int, Int?> = manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
        if (!cursor.moveToFirst()) throw IOException("Download disappeared")
        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val done = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        status to if (total > 0) ((done * 100L) / total).toInt().coerceIn(0, 100) else null
    } ?: throw IOException("Download query failed")

    private fun copyAndVerify(uri: Uri, release: GitHubAppRelease): File {
        val directory = File(appContext.cacheDir, "updates").apply { mkdirs() }
        val partial = File(directory, "${release.assetName.removeSuffix(".apk")}.partial.apk")
        val destination = File(directory, release.assetName)
        partial.delete(); val digest = MessageDigest.getInstance("SHA-256"); var total = 0L
        appContext.contentResolver.openInputStream(uri)?.use { input -> partial.outputStream().buffered().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) { val count = input.read(buffer); if (count < 0) break; total += count; if (total > MAX_APK_BYTES) throw IOException("APK too large"); digest.update(buffer, 0, count); output.write(buffer, 0, count) }
        } } ?: throw IOException("Downloaded file cannot be opened")
        if (total != release.assetSizeBytes || digest.digest().toHex() != release.sha256) { partial.delete(); throw IOException("APK digest mismatch") }
        verifyApk(partial, release.versionCode); destination.delete(); if (!partial.renameTo(destination)) throw IOException("Cannot finalize APK")
        return destination
    }

    private fun fetchLatestRelease(): GitHubAppRelease? {
        val connection = (LATEST_RELEASE_URL.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"; connectTimeout = 15_000; readTimeout = 15_000; instanceFollowRedirects = false
            setRequestProperty("Accept", "application/vnd.github+json"); setRequestProperty("X-GitHub-Api-Version", "2022-11-28"); setRequestProperty("User-Agent", "HanClip-Android/$currentVersionCode")
        }
        return try { if (connection.responseCode == 404) null else { if (connection.responseCode !in 200..299) throw IOException("GitHub HTTP error"); val text = connection.inputStream.bufferedReader().use { it.readText() }; if (text.length > 1_000_000) throw IOException("Response too large"); GitHubReleaseDecoder.decode(text) } } finally { connection.disconnect() }
    }

    @Suppress("DEPRECATION") private fun verifyApk(file: File, expected: Int) {
        val pm = appContext.packageManager; val flags = PackageManager.GET_SIGNATURES
        val archive = pm.getPackageArchiveInfo(file.absolutePath, flags) ?: throw IOException("Invalid APK")
        val code = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        if (archive.packageName != appContext.packageName || code != expected.toLong() || code <= currentVersionCode) throw IOException("APK identity mismatch")
        val installed = pm.getPackageInfo(appContext.packageName, flags).signatures.orEmpty().map { it.toCharsString() }.toSet()
        val downloaded = archive.signatures.orEmpty().map { it.toCharsString() }.toSet()
        if (installed.isEmpty() || installed != downloaded) throw IOException("APK certificate mismatch")
    }

    private companion object {
        val LATEST_RELEASE_URL = URL("https://api.github.com/repos/armsone/HanClip-Android/releases/latest")
        const val APK_MIME = "application/vnd.android.package-archive"
        const val MAX_APK_BYTES = 250L * 1_024L * 1_024L
    }
}

internal object GitHubReleaseDecoder {
    fun decode(payload: String): GitHubAppRelease? {
        val root = JSONObject(payload); if (root.optBoolean("draft", true) || root.optBoolean("prerelease", true)) return null
        val tag = root.optString("tag_name"); val productVersion = GitHubUpdatePolicy.productVersion(tag) ?: return null; val releaseNotes = root.optString("body"); val code = GitHubUpdatePolicy.versionCode(releaseNotes) ?: return null; val assets = root.optJSONArray("assets") ?: return null
        for (index in 0 until assets.length()) { val asset = assets.optJSONObject(index) ?: continue; val name = asset.optString("name"); val url = asset.optString("browser_download_url"); val size = asset.optLong("size", -1); val digest = GitHubUpdatePolicy.sha256(asset.optString("digest")) ?: continue; if (GitHubUpdatePolicy.isApprovedApkAsset(name, url, productVersion, size)) return GitHubAppRelease(code, tag, name, URL(url), size, digest, releaseNotes) }
        return null
    }
}

internal object GitHubUpdatePolicy {
    private val tag = Regex("^android-v(\\d+\\.\\d+\\.\\d+)$"); private val code = Regex("(?m)^Android-Version-Code:\\s*([1-9]\\d*)\\s*$"); private val digest = Regex("^sha256:([0-9a-f]{64})$")
    fun productVersion(value: String): String? = tag.matchEntire(value)?.groupValues?.get(1)
    fun versionCode(value: String): Int? = code.find(value)?.groupValues?.get(1)?.toIntOrNull()
    fun sha256(value: String): String? = digest.matchEntire(value)?.groupValues?.get(1)
    fun isApprovedApkAsset(name: String, text: String, productVersion: String, size: Long): Boolean {
        if (name != "HanClip-Android-$productVersion.apk" || size !in 1..250L * 1_024L * 1_024L) return false
        val url = runCatching { URL(text) }.getOrNull() ?: return false
        return url.protocol == "https" && url.host == "github.com" && url.userInfo == null && (url.port == -1 || url.port == 443) && url.query == null && url.ref == null && url.path == "/armsone/HanClip-Android/releases/download/android-v$productVersion/$name"
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
