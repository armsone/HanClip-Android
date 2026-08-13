package com.hanclip.android.core.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class GitHubAppRelease(
    val versionCode: Int,
    val tagName: String,
    val assetName: String,
    val apkUrl: URL,
    val assetSizeBytes: Long
)

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class Available(
        val release: GitHubAppRelease,
        val message: String? = null
    ) : AppUpdateState
    data class Downloading(val release: GitHubAppRelease) : AppUpdateState
    data class Ready(val release: GitHubAppRelease, val apkFile: File) : AppUpdateState
}

class GitHubAppUpdateService(
    context: Context,
    private val currentVersionCode: Int
) : Closeable {
    private val applicationContext = context.applicationContext
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("GitHubAppUpdateService")
    )
    private val mutableState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    private val checkStarted = AtomicBoolean(false)

    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    fun checkForUpdate() {
        if (!checkStarted.compareAndSet(false, true)) return
        mutableState.value = AppUpdateState.Checking
        scope.launch {
            mutableState.value = runCatching { fetchLatestRelease() }
                .getOrNull()
                ?.takeIf { it.versionCode > currentVersionCode }
                ?.let(AppUpdateState::Available)
                ?: AppUpdateState.Idle
        }
    }

    fun download(release: GitHubAppRelease) {
        val current = mutableState.value
        if (current !is AppUpdateState.Available || current.release != release) return
        mutableState.value = AppUpdateState.Downloading(release)
        scope.launch {
            mutableState.value = runCatching { downloadAndVerify(release) }.fold(
                onSuccess = { AppUpdateState.Ready(release, it) },
                onFailure = {
                    Log.w(LogTag, "GitHub update download or verification failed", it)
                    AppUpdateState.Available(
                        release,
                        "업데이트를 받지 못했습니다. 인터넷 연결을 확인해 주세요."
                    )
                }
            )
        }
    }

    override fun close() {
        scope.cancel()
    }

    private fun fetchLatestRelease(): GitHubAppRelease? {
        val connection = openConnection(LatestReleaseUrl)
        try {
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_NOT_FOUND) return null
            if (status !in 200..299) throw IOException("GitHub returned HTTP $status")
            val payload = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.readText().also {
                    if (it.length > MaxReleaseJsonChars) {
                        throw IOException("GitHub release response is too large")
                    }
                }
            }
            return GitHubReleaseDecoder.decode(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadAndVerify(release: GitHubAppRelease): File {
        val updateDirectory = File(applicationContext.cacheDir, UpdateDirectory).apply {
            if (!isDirectory && !mkdirs()) throw IOException("Cannot create update directory")
        }
        updateDirectory.listFiles()?.forEach { oldFile ->
            if (oldFile.name != release.assetName) oldFile.delete()
        }
        val destination = File(updateDirectory, release.assetName)
        val partial = File(
            updateDirectory,
            "${release.assetName.removeSuffix(".apk")}.partial.apk"
        )
        partial.delete()

        val connection = openConnection(release.apkUrl)
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("APK download returned HTTP $status")
            if (connection.contentLengthLong > MaxApkBytes) throw IOException("APK is too large")
            var total = 0L
            connection.inputStream.use { input ->
                partial.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MaxApkBytes) throw IOException("APK is too large")
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (total <= 0L || (release.assetSizeBytes > 0L && total != release.assetSizeBytes)) {
                throw IOException("APK size does not match the release")
            }
            verifyDownloadedApk(partial, release.versionCode)
            destination.delete()
            if (!partial.renameTo(destination)) throw IOException("Cannot finalize APK download")
            return destination
        } finally {
            connection.disconnect()
            if (partial.exists()) partial.delete()
        }
    }

    @Suppress("DEPRECATION")
    private fun verifyDownloadedApk(apkFile: File, expectedVersionCode: Int) {
        val packageManager = applicationContext.packageManager
        val flags = PackageManager.GET_SIGNATURES
        val archive = packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags)
            ?: throw IOException("Android could not parse the downloaded APK")
        if (archive.packageName != applicationContext.packageName) {
            throw IOException("Downloaded APK package name does not match HanClip")
        }
        val archiveVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archive.longVersionCode
        } else {
            archive.versionCode.toLong()
        }
        if (archiveVersion != expectedVersionCode.toLong() || archiveVersion <= currentVersionCode) {
            throw IOException("Downloaded APK version does not match the release")
        }
        val installed = packageManager.getPackageInfo(applicationContext.packageName, flags)
        val installedCertificates = installed.signatures.orEmpty().map { it.toCharsString() }.toSet()
        val archiveCertificates = archive.signatures.orEmpty().map { it.toCharsString() }.toSet()
        if (installedCertificates.isEmpty() || archiveCertificates.isEmpty()) {
            throw IOException("Android returned no APK certificate")
        }
        if (installedCertificates != archiveCertificates) {
            throw IOException("Downloaded APK certificate does not match the installed app")
        }
    }

    private fun openConnection(url: URL): HttpURLConnection {
        require(url.protocol == "https")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NetworkTimeoutMillis
            readTimeout = NetworkTimeoutMillis
            instanceFollowRedirects = true
            useCaches = false
            doInput = true
            setRequestProperty("User-Agent", "HanClip-Android/$currentVersionCode")
        }
    }

    private companion object {
        val LatestReleaseUrl = URL(
            "https://api.github.com/repos/armsone/HanClip-Android/releases/latest"
        )
        const val UpdateDirectory = "updates"
        const val NetworkTimeoutMillis = 15_000
        const val MaxReleaseJsonChars = 1_000_000
        const val MaxApkBytes = 250L * 1_024L * 1_024L
        const val LogTag = "HanClipUpdate"
    }
}

internal object GitHubReleaseDecoder {
    fun decode(payload: String): GitHubAppRelease? {
        val root = JSONObject(payload)
        if (root.optBoolean("draft", true) || root.optBoolean("prerelease", true)) return null
        val tag = root.optString("tag_name")
        val versionCode = GitHubUpdatePolicy.versionCode(tag) ?: return null
        val assets = root.optJSONArray("assets") ?: return null
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            val name = asset.optString("name")
            val url = asset.optString("browser_download_url")
            val size = asset.optLong("size", -1L)
            if (GitHubUpdatePolicy.isApprovedApkAsset(name, url, versionCode, size)) {
                return GitHubAppRelease(
                    versionCode,
                    tag,
                    name,
                    URL(url),
                    size
                )
            }
        }
        return null
    }
}

internal object GitHubUpdatePolicy {
    private val TagPattern = Regex("^android-v([1-9]\\d*)$")

    fun versionCode(tagName: String): Int? = TagPattern.matchEntire(tagName)
        ?.groupValues
        ?.get(1)
        ?.toIntOrNull()

    fun isApprovedApkAsset(
        assetName: String,
        urlText: String,
        versionCode: Int,
        sizeBytes: Long
    ): Boolean {
        if (assetName != "HanClip-Android-v$versionCode.apk") return false
        if (sizeBytes <= 0L || sizeBytes > 250L * 1_024L * 1_024L) return false
        val url = runCatching { URL(urlText) }.getOrNull() ?: return false
        return url.protocol == "https" &&
            url.host == "github.com" &&
            url.path.startsWith(
                "/armsone/HanClip-Android/releases/download/android-v$versionCode/"
            ) &&
            url.path.endsWith("/$assetName")
    }
}
