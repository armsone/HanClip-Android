package com.hanclip.android

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import androidx.core.content.FileProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hanclip.android.core.navigation.HanClipQuickAction
import com.hanclip.android.core.safety.ImportFileTransaction
import com.hanclip.android.core.safety.UserAssetFileTransaction
import com.hanclip.android.core.safety.ProjectFileTransaction
import com.hanclip.android.core.theme.HanClipTheme
import com.hanclip.android.core.update.AppUpdateDialog
import com.hanclip.android.core.update.AppUpdateState
import com.hanclip.android.core.update.GitHubAppUpdateService
import com.hanclip.android.feature.browser.BrowserFavoritesStore
import java.io.File

class MainActivity : ComponentActivity() {
    private var sharedMediaUris by mutableStateOf<List<Uri>>(emptyList())
    private var sharedBrowserFavorites by mutableStateOf<List<String>>(emptyList())
    private var sharedBrowserFavoritesImportAttempted by mutableStateOf(false)
    private var quickAction by mutableStateOf<HanClipQuickAction?>(null)
    private val appUpdateService by lazy {
        GitHubAppUpdateService(this, installedVersionCode())
    }
    private var pendingUpdateInstallFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImportFileTransaction.cleanupInterrupted(File(filesDir, "working-media"))
        UserAssetFileTransaction.cleanupInterrupted(filesDir)
        ProjectFileTransaction.cleanupInterrupted(filesDir)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        sharedMediaUris = extractSharedMediaUris()
        sharedBrowserFavoritesImportAttempted = hasBrowserFavoritesArchiveIntent()
        sharedBrowserFavorites = extractSharedBrowserFavorites()
        quickAction = intent.extractQuickAction()
        setContent {
            HanClipTheme {
                val appUpdateState by appUpdateService.state.collectAsStateWithLifecycle()
                var ignoredUpdateVersion by rememberSaveable { mutableStateOf<Int?>(null) }
                HanClipApp(
                    sharedMediaUris = sharedMediaUris,
                    sharedBrowserFavorites = sharedBrowserFavorites,
                    sharedBrowserFavoritesImportAttempted = sharedBrowserFavoritesImportAttempted,
                    quickAction = quickAction,
                    onSharedMediaHandled = {
                        sharedMediaUris = emptyList()
                    },
                    onSharedBrowserFavoritesHandled = {
                        sharedBrowserFavorites = emptyList()
                        sharedBrowserFavoritesImportAttempted = false
                    },
                    onQuickActionHandled = ::clearHandledQuickAction,
                    onKeepScreenOnChanged = ::setKeepScreenOn
                )
                val updateVersion = when (val update = appUpdateState) {
                    is AppUpdateState.Available -> update.release.versionCode
                    is AppUpdateState.Downloading -> update.release.versionCode
                    is AppUpdateState.Ready -> update.release.versionCode
                    AppUpdateState.Checking,
                    AppUpdateState.Idle -> null
                }
                if (updateVersion != null && ignoredUpdateVersion != updateVersion) {
                    AppUpdateDialog(
                        state = appUpdateState,
                        onDownload = {
                            (appUpdateState as? AppUpdateState.Available)?.let { available ->
                                appUpdateService.download(available.release)
                            }
                        },
                        onInstall = {
                            (appUpdateState as? AppUpdateState.Ready)?.let { ready ->
                                requestUpdateInstall(ready.apkFile)
                            }
                        },
                        onLater = { ignoredUpdateVersion = updateVersion }
                    )
                }
            }
        }
        appUpdateService.checkForUpdate()
    }

    override fun onResume() {
        super.onResume()
        val pendingFile = pendingUpdateInstallFile ?: return
        if (canRequestPackageInstalls()) {
            window.decorView.post { requestUpdateInstall(pendingFile) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedMediaUris = extractSharedMediaUris()
        sharedBrowserFavoritesImportAttempted = hasBrowserFavoritesArchiveIntent()
        sharedBrowserFavorites = extractSharedBrowserFavorites()
        quickAction = intent.extractQuickAction()
            ?: if (intent.isLauncherLaunch()) HanClipQuickAction.Open else null
    }

    private fun clearHandledQuickAction() {
        quickAction = null
        if (intent.extractQuickAction() != null) {
            setIntent(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClass(this, MainActivity::class.java)
            )
        }
    }

    private fun setKeepScreenOn(shouldKeepScreenOn: Boolean) {
        if (shouldKeepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun requestUpdateInstall(apkFile: File) {
        if (!apkFile.isFile) {
            showToast("업데이트 파일을 찾을 수 없습니다.")
            return
        }
        if (!canRequestPackageInstalls()) {
            pendingUpdateInstallFile = apkFile
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.fromParts("package", packageName, null)
            )
            runCatching { startActivity(intent) }
                .onFailure {
                    pendingUpdateInstallFile = null
                    showToast("업데이트 설치 권한 설정을 열 수 없습니다.")
                }
            return
        }

        val contentUri = runCatching {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
        }.getOrElse {
            showToast("업데이트 파일을 열 수 없습니다.")
            return
        }
        pendingUpdateInstallFile = null
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, ApkMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
            .onFailure { showToast("Android 설치 화면을 열 수 없습니다.") }
    }

    private fun canRequestPackageInstalls(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            packageManager.canRequestPackageInstalls()

    @Suppress("DEPRECATION")
    private fun installedVersionCode(): Int {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        return version.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        setKeepScreenOn(false)
        appUpdateService.close()
        super.onDestroy()
    }

    private companion object {
        const val ApkMimeType = "application/vnd.android.package-archive"
    }
}

private fun Intent?.extractQuickAction(): HanClipQuickAction? {
    if (this?.action != Intent.ACTION_VIEW) return null
    return HanClipQuickAction.fromUri(data)
}

private fun Intent?.isLauncherLaunch(): Boolean {
    return this?.action == Intent.ACTION_MAIN && hasCategory(Intent.CATEGORY_LAUNCHER)
}

private fun MainActivity.extractSharedMediaUris(): List<Uri> {
    val intent = intent ?: return emptyList()
    if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
        return emptyList()
    }
    return intent.extractSharedUris()
        .filterNot { uri -> isBrowserFavoritesArchive(uri) }
}

private fun MainActivity.extractSharedBrowserFavorites(): List<String> {
    val intent = intent ?: return emptyList()
    if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
        val uri = intent.data ?: return emptyList()
        if (!isBrowserFavoritesArchive(uri)) return emptyList()
        return runCatching {
            BrowserFavoritesStore.parseArchive(this, uri)
        }.getOrDefault(emptyList())
    }
    if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
        return emptyList()
    }
    return intent.extractSharedUris()
        .filter(::isBrowserFavoritesArchive)
        .flatMap { uri ->
            runCatching {
                BrowserFavoritesStore.parseArchive(this, uri)
            }.getOrDefault(emptyList())
        }
        .distinct()
}

private fun MainActivity.hasBrowserFavoritesArchiveIntent(): Boolean {
    val intent = intent ?: return false
    if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
        return isBrowserFavoritesArchive(intent.data ?: return false)
    }
    if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
        return false
    }
    return intent.extractSharedUris().any(::isBrowserFavoritesArchive)
}

private fun MainActivity.isBrowserFavoritesArchive(uri: Uri): Boolean {
    val type = contentResolver.getType(uri).orEmpty()
    return type == BrowserFavoritesStore.ArchiveMimeType ||
        uri.lastPathSegment.orEmpty().endsWith(".hanclipfavorites", ignoreCase = true)
}

private fun Intent.extractSharedUris(): List<Uri> {
    val uris = mutableListOf<Uri>()
    clipData?.let { data ->
        for (index in 0 until data.itemCount) {
            data.getItemAt(index).uri?.let(uris::add)
        }
    }
    getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(uris::add)
    getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(uris::addAll)
    return uris.distinct()
}
