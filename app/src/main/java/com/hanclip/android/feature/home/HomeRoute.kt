package com.hanclip.android.feature.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import com.hanclip.android.core.media.MediaSelectionContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SportsGolf
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hanclip.android.R
import com.hanclip.android.core.model.MoviePreset
import com.hanclip.android.core.model.OutputQualityPreset
import com.hanclip.android.core.model.WatermarkPlatform
import com.hanclip.android.core.model.WatermarkPosition
import com.hanclip.android.core.model.WatermarkSettings
import com.hanclip.android.core.model.drawableResId
import com.hanclip.android.core.project.ExportHistoryStore
import com.hanclip.android.core.project.ExportedMovieSummary
import com.hanclip.android.core.project.CollectedMovie
import com.hanclip.android.core.project.CollectionVideoSizeOption
import com.hanclip.android.core.project.MovieCollectionStore
import com.hanclip.android.core.project.hanClipCompletionTitle
import com.hanclip.android.core.safety.InsufficientStorageException
import com.hanclip.android.core.settings.SleepPreventionMode
import com.hanclip.android.core.theme.HanClipPalette
import com.hanclip.android.core.theme.HanClipThemeMode
import com.hanclip.android.core.theme.HanClipThemeStore
import com.hanclip.android.core.theme.HanClipSystemBars
import com.hanclip.android.core.theme.currentPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRoute(
    exportedMovieSummaries: List<ExportedMovieSummary>,
    collectionMovies: List<CollectedMovie>,
    recentlySavedMovieUriString: String?,
    hasDraftProject: Boolean,
    editableProjectSummaries: List<DraftProjectSummary>,
    sharedInboxCount: Int,
    sharedInboxCopyCurrent: Int,
    sharedInboxCopyTotal: Int,
    onCreateMovieFromSharedInbox: () -> Unit,
    onAddSharedInboxToMovie: () -> Unit,
    onClearSharedInbox: () -> Unit,
    onCancelSharedInboxCopy: () -> Unit,
    sleepPreventionMode: SleepPreventionMode,
    watermarkSettings: WatermarkSettings,
    onStartPreset: (MoviePreset) -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenProject: () -> Unit,
    onOpenEditableProject: (DraftProjectSummary) -> Unit,
    onRemoveEditableProject: (DraftProjectSummary) -> Unit,
    onToggleEditableProjectPin: (DraftProjectSummary) -> Boolean,
    onUpdateEditableProjectMemo: (DraftProjectSummary, String) -> Unit,
    onOpenExportedMovie: (ExportedMovieSummary) -> Unit,
    onRemoveExportedMovie: (ExportedMovieSummary) -> Unit,
    onToggleExportedMoviePin: (ExportedMovieSummary) -> Boolean,
    onUpdateExportedMovieMemo: (ExportedMovieSummary, String) -> Unit,
    onCollectionChanged: () -> Unit,
    onOpenCollectionMovie: (CollectedMovie) -> Unit,
    onSleepPreventionModeChange: (SleepPreventionMode) -> Unit,
    onWatermarkSettingsChange: (WatermarkSettings) -> Unit,
    onOpenBrowser: () -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val presetColumnCount = 3
    val standardProjectColumnCount = if (screenWidthDp >= 600) 2 else 1
    val collectionColumnCount = if (screenWidthDp >= 600) 3 else 2
    val coroutineScope = rememberCoroutineScope()
    var themeMode by remember {
        mutableStateOf(HanClipThemeStore.load(context))
    }
    var orderedThemeModes by remember {
        mutableStateOf(HanClipThemeStore.loadVisibleOrder(context))
    }
    var showThemeSelection by remember { mutableStateOf(false) }
    var showSettingsInfo by remember { mutableStateOf(false) }
    val palette = themeMode.currentPalette
    HanClipSystemBars(palette.solidPanel)
    var removalCandidate by remember { mutableStateOf<ExportedMovieSummary?>(null) }
    var memoCandidate by remember { mutableStateOf<ExportedMovieSummary?>(null) }
    var editableRemovalCandidate by remember { mutableStateOf<DraftProjectSummary?>(null) }
    var editableMemoCandidate by remember { mutableStateOf<DraftProjectSummary?>(null) }
    var memoText by remember { mutableStateOf("") }
    var showPinLimitAlert by remember { mutableStateOf(false) }
    var isImportingCollection by remember { mutableStateOf(false) }
    var collectionImportCompleted by remember { mutableStateOf(0) }
    var collectionImportTotal by remember { mutableStateOf(0) }
    var collectionError by remember { mutableStateOf<String?>(null) }
    var showCollectionImportSource by remember { mutableStateOf(false) }
    var collectionImportJob by remember { mutableStateOf<Job?>(null) }
    var collectionCompressionCandidate by remember { mutableStateOf<CollectedMovie?>(null) }
    var collectionCompressionMovieTitle by remember { mutableStateOf("") }
    var collectionCompressionProgress by remember { mutableStateOf(0.0) }
    var collectionCompressionJob by remember { mutableStateOf<Job?>(null) }
    var collectionPosterRepairCompleted by remember { mutableStateOf(0) }
    var collectionPosterRepairTotal by remember { mutableStateOf(0) }

    val outdatedCollectionPosterIds = collectionMovies
        .filter {
            (it.posterSelectionVersion ?: 0) < MovieCollectionStore.CurrentPosterSelectionVersion
        }
        .map(CollectedMovie::id)
    LaunchedEffect(outdatedCollectionPosterIds) {
        if (outdatedCollectionPosterIds.isEmpty()) {
            collectionPosterRepairCompleted = 0
            collectionPosterRepairTotal = 0
            return@LaunchedEffect
        }
        MovieCollectionStore.regenerateOutdatedPosters(context) { completed, total ->
            withContext(Dispatchers.Main.immediate) {
                collectionPosterRepairCompleted = completed
                collectionPosterRepairTotal = total
            }
        }
        onCollectionChanged()
    }

    fun beginCollectionCompression(movie: CollectedMovie, option: CollectionVideoSizeOption) {
        collectionCompressionJob?.cancel()
        collectionCompressionMovieTitle = movie.title
        collectionCompressionProgress = 0.0
        collectionCompressionJob = coroutineScope.launch {
            try {
                val result = MovieCollectionStore.reduceFileSize(context, movie, option) { progress ->
                    collectionCompressionProgress = progress
                }
                onCollectionChanged()
                collectionError = "파일 용량을 줄였습니다. ${collectionFileSize(result.originalBytes)} → ${collectionFileSize(result.compressedBytes)}"
            } catch (cancelled: CancellationException) {
                collectionError = "파일 용량 줄이기를 취소했습니다. 원본 파일은 그대로 유지됩니다."
            } catch (error: Throwable) {
                collectionError = error.message ?: "영상 용량을 줄이지 못했습니다."
            } finally {
                collectionCompressionJob = null
                collectionCompressionProgress = 0.0
            }
        }
    }

    fun beginCollectionBulkCompression(option: CollectionVideoSizeOption) {
        val movies = collectionMovies
        if (movies.isEmpty()) return
        collectionCompressionJob?.cancel()
        collectionCompressionMovieTitle = "컬렉션 준비 중"
        collectionCompressionProgress = 0.0
        collectionCompressionJob = coroutineScope.launch {
            var convertedCount = 0
            var skippedCount = 0
            var failedCount = 0
            var originalBytes = 0L
            var compressedBytes = 0L
            try {
                movies.forEachIndexed { index, movie ->
                    currentCoroutineContext().ensureActive()
                    collectionCompressionMovieTitle = "${index + 1}/${movies.size}  ${movie.title}"
                    val info = runCatching {
                        MovieCollectionStore.compressionInfo(context, movie)
                    }.getOrNull()
                    if (info == null) {
                        failedCount += 1
                    } else if (info.isAtOrBelow(option)) {
                        skippedCount += 1
                    } else {
                        try {
                            val result = MovieCollectionStore.reduceFileSize(context, movie, option) { progress ->
                                collectionCompressionProgress = (index + progress) / movies.size.toDouble()
                            }
                            convertedCount += 1
                            originalBytes += result.originalBytes
                            compressedBytes += result.compressedBytes
                            onCollectionChanged()
                        } catch (storageError: InsufficientStorageException) {
                            throw storageError
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Throwable) {
                            failedCount += 1
                        }
                    }
                    collectionCompressionProgress = (index + 1).toDouble() / movies.size
                }
                collectionError = buildString {
                    append("일괄 변환 완료: ${convertedCount}개 변환")
                    if (skippedCount > 0) append(", ${skippedCount}개 유지")
                    if (failedCount > 0) append(", ${failedCount}개 실패")
                    if (convertedCount > 0) {
                        append("\n${collectionFileSize(originalBytes)} → ${collectionFileSize(compressedBytes)}")
                    }
                }
            } catch (storageError: InsufficientStorageException) {
                collectionError = storageError.message
            } catch (_: CancellationException) {
                collectionError = "컬렉션 일괄 변환을 취소했습니다. 완료된 영상은 그대로 유지됩니다."
            } finally {
                collectionCompressionJob = null
                collectionCompressionProgress = 0.0
            }
        }
    }

    fun importCollectionUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        collectionImportTotal = uris.size
        collectionImportCompleted = 0
        isImportingCollection = true
        collectionImportJob?.cancel()
        collectionImportJob = coroutineScope.launch {
            var failedCount = 0
            var importedCount = 0
            var duplicateCount = 0
            try {
                uris.forEach { uri ->
                    currentCoroutineContext().ensureActive()
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    try {
                        val outcome = MovieCollectionStore.importMovieWithOutcome(context, uri)
                        if (outcome.wasDuplicate) {
                            duplicateCount += 1
                        } else {
                            importedCount += 1
                            onCollectionChanged()
                        }
                    } catch (storageError: InsufficientStorageException) {
                        throw storageError
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Throwable) {
                        failedCount += 1
                    }
                    collectionImportCompleted += 1
                }
                if (failedCount > 0 || duplicateCount > 0) {
                    collectionError = buildList {
                        add("새 영화 ${importedCount}개를 컬렉션에 추가했습니다.")
                        if (duplicateCount > 0) add("이미 보관된 ${duplicateCount}개는 중복 복사하지 않았습니다.")
                        if (failedCount > 0) add("동영상이 아니거나 읽을 수 없는 ${failedCount}개는 제외했습니다.")
                    }.joinToString(" ")
                }
            } catch (storageError: InsufficientStorageException) {
                collectionError = storageError.message
            } catch (cancelled: CancellationException) {
                collectionError = buildList {
                    add("컬렉션 가져오기를 취소했습니다.")
                    if (importedCount > 0) add("완료된 새 영화 ${importedCount}개는 그대로 보관됩니다.")
                    if (duplicateCount > 0) add("중복 ${duplicateCount}개는 복사하지 않았습니다.")
                }.joinToString(" ")
            } finally {
                isImportingCollection = false
                collectionImportJob = null
            }
        }
    }

    val collectionFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            importCollectionUris(MediaSelectionContract.fromResultIntent(context, result.data).uris)
        }
    }
    val collectionPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris -> importCollectionUris(MediaSelectionContract.normalize(uris).uris) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        if (!showSettingsInfo) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        item(key = "home-header") {
            Spacer(Modifier.height(6.dp))
            HomeHeader(
                palette = palette,
                onCycleTheme = {
                    val modes = orderedThemeModes
                    val currentIndex = modes.indexOf(themeMode).coerceAtLeast(0)
                    themeMode = modes[(currentIndex + 1) % modes.size]
                    HanClipThemeStore.save(context, themeMode)
                },
                onOpenThemeSelection = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    showThemeSelection = true
                },
                onOpenAiShot = { onStartPreset(MoviePreset.AiShot) },
                onOpenPhotos = onOpenPhotos,
                onOpenCalendar = onOpenCalendar,
                onOpenFiles = onOpenFiles
            )
            Spacer(Modifier.height(8.dp))
        }
        item(key = "preset-grid") {
            PresetGrid(onStartPreset, palette, presetColumnCount)
            Spacer(Modifier.height(8.dp))
        }
        savedProjectItems(
            palette = palette,
            summaries = exportedMovieSummaries,
            recentlySavedMovieUriString = recentlySavedMovieUriString,
            hasDraftProject = hasDraftProject,
            editableProjectSummaries = editableProjectSummaries,
            standardProjectColumnCount = standardProjectColumnCount,
            onOpenProject = onOpenProject,
            onOpenEditableProject = onOpenEditableProject,
            onRemoveEditableProject = { editableRemovalCandidate = it },
            onToggleEditableProjectPin = { summary ->
                if (!onToggleEditableProjectPin(summary)) showPinLimitAlert = true
            },
            onEditEditableProjectMemo = {
                editableMemoCandidate = it
                memoText = it.memo
            },
            onOpenExportedMovie = onOpenExportedMovie,
            onRemoveExportedMovie = { removalCandidate = it },
            onToggleExportedMoviePin = { summary ->
                if (!onToggleExportedMoviePin(summary)) {
                    showPinLimitAlert = true
                }
            },
            onEditExportedMovieMemo = {
                memoCandidate = it
                memoText = it.memo
            },
            collectionMovies = collectionMovies,
            isImportingCollection = isImportingCollection,
            collectionImportCompleted = collectionImportCompleted,
            collectionImportTotal = collectionImportTotal,
            onImportCollection = { showCollectionImportSource = true },
            onCancelCollectionImport = { collectionImportJob?.cancel() },
            collectionPosterRepairCompleted = collectionPosterRepairCompleted,
            collectionPosterRepairTotal = collectionPosterRepairTotal,
            onOpenCollectionMovie = onOpenCollectionMovie,
            onToggleCollectionMoviePin = { movie ->
                MovieCollectionStore.togglePin(context, movie.id)
                onCollectionChanged()
            },
            onMovePinnedCollectionMovie = { source, target ->
                MovieCollectionStore.movePinnedMovie(context, source.id, target.id)
                onCollectionChanged()
            },
            onRenameCollectionMovie = { movie, title ->
                MovieCollectionStore.updateTitle(context, movie.id, title)
                onCollectionChanged()
            },
            onRemoveCollectionMovie = { movie ->
                MovieCollectionStore.remove(context, movie.id)
                onCollectionChanged()
            },
            isCompressingCollectionMovie = collectionCompressionJob != null,
            collectionCompressionMovieTitle = collectionCompressionMovieTitle,
            collectionCompressionProgress = collectionCompressionProgress,
            onRequestCollectionCompression = { collectionCompressionCandidate = it },
            onRequestBulkCollectionCompression = ::beginCollectionBulkCompression,
            onCancelCollectionCompression = { collectionCompressionJob?.cancel() },
            collectionColumnCount = collectionColumnCount
        )
        item(key = "home-bottom-space") {
            Spacer(Modifier.height(84.dp))
        }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
                .size(48.dp)
                .clearAndSetSemantics {
                    contentDescription = "카피라이터 설정, 길게 눌러 음악 브라우저 열기"
                    onClick(label = "카피라이터 설정 열기") {
                        showSettingsInfo = true
                        true
                    }
                    onLongClick(label = "음악 브라우저 열기") {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenBrowser()
                        true
                    }
                }
                .combinedClickable(
                    onClick = { showSettingsInfo = true },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenBrowser()
                    },
                    onLongClickLabel = "음악 브라우저 열기"
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = palette.secondary.copy(alpha = 0.18f).compositeOver(palette.solidPanel),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.42f)),
                shadowElevation = 7.dp
            ) {
                Text(
                    "i",
                    modifier = Modifier.wrapContentSize(Alignment.Center),
                    color = palette.primary,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        }
        if (showSettingsInfo) {
            SettingsInfoScreen(
                palette = palette,
                sleepPreventionMode = sleepPreventionMode,
                watermarkSettings = watermarkSettings,
                onSleepPreventionModeChange = onSleepPreventionModeChange,
                onWatermarkSettingsChange = onWatermarkSettingsChange,
                onDismiss = { showSettingsInfo = false }
            )
        }
    }
    if (showThemeSelection) {
        ThemeSelectionDialog(
            selectedMode = themeMode,
            orderedModes = orderedThemeModes,
            onSelect = { mode ->
                themeMode = mode
                HanClipThemeStore.save(context, mode)
            },
            onMoveCustomTheme = { mode, direction ->
                val customOrder = orderedThemeModes
                    .filter { it in HanClipThemeMode.customModes }
                    .toMutableList()
                val fromIndex = customOrder.indexOf(mode)
                val toIndex = (fromIndex + direction).coerceIn(customOrder.indices)
                if (fromIndex >= 0 && fromIndex != toIndex) {
                    customOrder.removeAt(fromIndex)
                    customOrder.add(toIndex, mode)
                    orderedThemeModes = HanClipThemeMode.baseModes + customOrder
                    HanClipThemeStore.saveCustomOrder(context, customOrder)
                }
            },
            onDismiss = { showThemeSelection = false }
        )
    }
    removalCandidate?.let { summary ->
        AlertDialog(
            onDismissRequest = { removalCandidate = null },
            dismissButton = {
                OutlinedButton(
                    onClick = { removalCandidate = null },
                    border = BorderStroke(1.dp, HomeBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = HomeText
                    )
                ) {
                    Text("취소")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        removalCandidate = null
                        onRemoveExportedMovie(summary)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE45D42),
                        contentColor = Color.White
                    )
                ) {
                    Text("목록에서 제거")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            titleContentColor = HomeText,
            textContentColor = HomeSubText,
            title = { Text("목록에서 제거") },
            text = {
                Text(
                    "${homeProjectDateText(summary.updatedAtMillis)} · ${savedMovieDetailText(summary)}\n\n" +
                        "HanClip 목록에서만 제거합니다. 기본 사진첩이나 파일에 저장된 완성본 MP4는 삭제하지 않습니다."
                )
            }
        )
    }
    memoCandidate?.let { summary ->
        AlertDialog(
            onDismissRequest = { memoCandidate = null },
            dismissButton = {
                OutlinedButton(
                    onClick = { memoCandidate = null },
                    border = BorderStroke(1.dp, HomeBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = HomeText
                    )
                ) {
                    Text("취소")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateExportedMovieMemo(summary, memoText)
                        memoCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("저장")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = palette.solidPanel,
            titleContentColor = palette.text,
            textContentColor = palette.subText,
            title = { Text("메모 편집") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "저장된 완성본 목록에 표시할 짧은 메모를 남깁니다.",
                        color = palette.subText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        singleLine = true,
                        placeholder = { Text("메모 추가") }
                    )
                }
            }
        )
    }
    editableRemovalCandidate?.let { summary ->
        AlertDialog(
            onDismissRequest = { editableRemovalCandidate = null },
            dismissButton = {
                OutlinedButton(onClick = { editableRemovalCandidate = null }) { Text("취소") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveEditableProject(summary)
                        editableRemovalCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE45D42))
                ) { Text("프로젝트 제거") }
            },
            title = { Text("편집 프로젝트 제거") },
            text = { Text("목록과 저장 정보에서 제거합니다. 갤러리에 저장된 MP4는 삭제하지 않습니다.") }
        )
    }
    editableMemoCandidate?.let { summary ->
        AlertDialog(
            onDismissRequest = { editableMemoCandidate = null },
            dismissButton = {
                OutlinedButton(onClick = { editableMemoCandidate = null }) { Text("취소") }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateEditableProjectMemo(summary, memoText)
                    editableMemoCandidate = null
                }) { Text("저장") }
            },
            title = { Text("프로젝트 메모") },
            text = {
                TextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    singleLine = true,
                    placeholder = { Text("메모 추가") }
                )
            }
        )
    }
    if (showPinLimitAlert) {
        AlertDialog(
            onDismissRequest = { showPinLimitAlert = false },
            confirmButton = {
                Button(
                    onClick = { showPinLimitAlert = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("확인")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = palette.solidPanel,
            titleContentColor = palette.text,
            textContentColor = palette.subText,
            title = { Text("핀 고정 제한") },
            text = {
                Text("핀 고정은 최대 ${ExportHistoryStore.MaxPinnedItems}개까지 가능합니다. 다른 완성본을 해제한 뒤 다시 고정해 주세요.")
            }
        )
    }
    collectionError?.let { message ->
        AlertDialog(
            onDismissRequest = { collectionError = null },
            confirmButton = {
                Button(onClick = { collectionError = null }) { Text("확인") }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = palette.solidPanel,
            title = { Text("컬렉션") },
            text = { Text(message) }
        )
    }
    if (showCollectionImportSource) {
        AlertDialog(
            onDismissRequest = { showCollectionImportSource = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = palette.solidPanel,
            title = { Text("컬렉션에 영화 추가") },
            text = { Text("사진 앱에서 영상을 고르거나 파일에서 동영상을 가져옵니다. 원본은 변경하지 않고 HanClip 컬렉션에 별도로 보관합니다.") },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCollectionImportSource = false
                        collectionFilePicker.launch(collectionVideoFileIntent(context))
                    }
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("파일")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCollectionImportSource = false
                        collectionPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("사진")
                }
            }
        )
    }
    collectionCompressionCandidate?.let { movie ->
        CollectionVideoSizeOptionsDialog(
            movie = movie,
            palette = palette,
            onSelect = { option ->
                collectionCompressionCandidate = null
                beginCollectionCompression(movie, option)
            },
            onDismiss = { collectionCompressionCandidate = null }
        )
    }
    if (sharedInboxCount > 0) {
        SharedInboxDialog(
            count = sharedInboxCount,
            copyCurrent = sharedInboxCopyCurrent,
            copyTotal = sharedInboxCopyTotal,
            palette = palette,
            onCreateMovie = onCreateMovieFromSharedInbox,
            onAddToMovie = onAddSharedInboxToMovie,
            onClear = onClearSharedInbox,
            onCancelCopy = onCancelSharedInboxCopy
        )
    }
}

data class DraftProjectSummary(
    val projectId: String,
    val preset: MoviePreset,
    val presetTitle: String,
    val clipCount: Int,
    val totalDurationSeconds: Double,
    val outputText: String,
    val savedAtMillis: Long,
    val isPinned: Boolean = false,
    val memo: String = "",
    val thumbnailUriString: String? = null,
    val thumbnailUriStrings: List<String> = emptyList(),
    val displayByteCount: Long = 0L
)

private val HomePrimary = Color(0xFF0B7A4E)
private val HomeText = Color(0xFF14221A)
private val HomeSubText = Color(0xFF46564C)
private val HomeBorder = Color(0xFFD4DDD7)
private const val HomeSavedMovieSlotCount = 10
private const val HomeThumbnailCacheSizeKb = 8 * 1024
private const val HomeStripCacheSizeKb = 8 * 1024
private const val HomeFrameLoadDelayMillis = 100L
private const val HomeFrameDecodeScale = 2
private const val HomeThumbnailMinLongEdgePx = 192
private const val HomeThumbnailMaxLongEdgePx = 640
private const val HomeStripMinLongEdgePx = 64
private const val HomeStripMaxLongEdgePx = 192

private object HomeMovieFrameCache {
    private val thumbnails = object : LruCache<String, Bitmap>(HomeThumbnailCacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.sizeInKilobytes()
    }
    private val strips = object : LruCache<String, List<Bitmap>>(HomeStripCacheSizeKb) {
        override fun sizeOf(key: String, value: List<Bitmap>): Int {
            return value.fold(0L) { total, bitmap ->
                total + bitmap.sizeInKilobytes()
            }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
    }

    fun thumbnail(key: String): Bitmap? = thumbnails.get(key)

    fun putThumbnail(key: String, bitmap: Bitmap?) {
        if (bitmap == null) return
        thumbnails.put(key, bitmap)
    }

    fun strip(key: String): List<Bitmap>? = strips.get(key)

    fun putStrip(key: String, bitmaps: List<Bitmap>) {
        if (bitmaps.isEmpty()) return
        strips.put(key, bitmaps)
    }
}

private fun Bitmap.sizeInKilobytes(): Int {
    return ((allocationByteCount.toLong() + 1023L) / 1024L)
        .coerceIn(1L, Int.MAX_VALUE.toLong())
        .toInt()
}

private fun MediaMetadataRetriever.loadScaledFrameAtTime(
    positionUs: Long,
    targetLongEdgePx: Int
): Bitmap? {
    val targetSize = videoFrameTargetSize(targetLongEdgePx)
    val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && targetSize != null) {
        getScaledFrameAtTime(
            positionUs,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            targetSize.first,
            targetSize.second
        ) ?: getFrameAtTime(positionUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } else {
        getFrameAtTime(positionUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
    return decoded?.scaledDownToLongEdge(targetLongEdgePx)
}

private fun MediaMetadataRetriever.videoFrameTargetSize(targetLongEdgePx: Int): Pair<Int, Int>? {
    val sourceWidth = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        ?.toIntOrNull()
        ?.takeIf { it > 0 }
        ?: return null
    val sourceHeight = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        ?.toIntOrNull()
        ?.takeIf { it > 0 }
        ?: return null
    val scale = min(
        1.0,
        targetLongEdgePx.toDouble() / max(sourceWidth, sourceHeight).toDouble()
    )
    return max(1, (sourceWidth * scale).roundToInt()) to
        max(1, (sourceHeight * scale).roundToInt())
}

private fun Bitmap.scaledDownToLongEdge(targetLongEdgePx: Int): Bitmap {
    val longEdge = max(width, height)
    if (longEdge <= targetLongEdgePx) return this
    val scale = targetLongEdgePx.toDouble() / longEdge.toDouble()
    val scaled = Bitmap.createScaledBitmap(
        this,
        max(1, (width * scale).roundToInt()),
        max(1, (height * scale).roundToInt()),
        true
    )
    if (scaled !== this) recycle()
    return scaled
}

@Composable
private fun ThemeSelectionDialog(
    selectedMode: HanClipThemeMode,
    orderedModes: List<HanClipThemeMode>,
    onSelect: (HanClipThemeMode) -> Unit,
    onMoveCustomTheme: (HanClipThemeMode, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedPalette = selectedMode.currentPalette
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 28.dp),
            contentAlignment = Alignment.TopCenter
        ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).widthIn(max = 620.dp),
            shape = RoundedCornerShape(24.dp),
            color = selectedPalette.solidPanel,
            border = BorderStroke(1.dp, selectedPalette.border),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "SELECT THEME",
                    color = selectedPalette.text,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                ThemePaletteSummary(selectedMode)
                val reorderableModes = orderedModes.filter { it in HanClipThemeMode.customModes }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    orderedModes.forEach { mode ->
                        val reorderIndex = reorderableModes.indexOf(mode)
                        ThemeSelectionRow(
                            mode = mode,
                            selected = mode == selectedMode,
                            textColor = selectedPalette.text,
                            onClick = { onSelect(mode) },
                            canReorder = mode in HanClipThemeMode.customModes,
                            canMoveUp = reorderIndex > 0,
                            canMoveDown = reorderIndex in 0 until reorderableModes.lastIndex,
                            onMove = { direction -> onMoveCustomTheme(mode, direction) }
                        )
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedPalette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
            }
        }
        }
    }
}

@Composable
private fun ThemePaletteSummary(mode: HanClipThemeMode) {
    val palette = mode.currentPalette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "COLOR SYSTEM",
                    color = palette.subText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    mode.displayName,
                    color = palette.subText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePaletteChip("Main", "선택/실행", palette.primary, palette)
                ThemePaletteChip("Sub", "구조/그룹", palette.secondary, palette)
                ThemePaletteChip("BG", "배경", palette.chip, palette)
                ThemePaletteChip("Text", "정보", palette.text, palette)
            }
        }
    }
}

@Composable
private fun RowScope.ThemePaletteChip(
    title: String,
    description: String,
    color: Color,
    palette: HanClipPalette
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(color)
        )
        Text(
            title,
            color = palette.text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
        Text(
            description,
            color = palette.subText,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemeSelectionRow(
    mode: HanClipThemeMode,
    selected: Boolean,
    textColor: Color,
    onClick: () -> Unit,
    canReorder: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (Int) -> Unit
) {
    val palette = mode.currentPalette
    val hapticFeedback = LocalHapticFeedback.current
    val dragThreshold = with(LocalDensity.current) { 28.dp.toPx() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .then(
                if (canReorder) {
                    Modifier.semantics {
                        customActions = listOf(
                            if (canMoveUp) {
                                CustomAccessibilityAction("테마 위로 이동") {
                                    onMove(-1)
                                    true
                                }
                            } else {
                                null
                            },
                            if (canMoveDown) {
                                CustomAccessibilityAction("테마 아래로 이동") {
                                    onMove(1)
                                    true
                                }
                            } else {
                                null
                            }
                        ).filterNotNull()
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = CircleShape,
            color = if (selected) palette.primary else Color.Transparent,
            border = BorderStroke(2.dp, palette.primary)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier.padding(5.dp).background(Color.White, CircleShape)
                )
            }
        }
        Text(
            mode.displayName,
            modifier = Modifier.weight(1f),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (mode != HanClipThemeMode.Automatic) Row(
            modifier = Modifier
                .then(
                    if (canReorder) {
                        Modifier.pointerInput(mode, dragThreshold) {
                            var accumulatedY = 0f
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    accumulatedY = 0f
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragEnd = { accumulatedY = 0f },
                                onDragCancel = { accumulatedY = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedY += dragAmount.y
                                    if (abs(accumulatedY) >= dragThreshold) {
                                        onMove(if (accumulatedY > 0) 1 else -1)
                                        accumulatedY = 0f
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeColorSwatch(palette.primary)
            ThemeColorSwatch(palette.secondary)
            if (canReorder) {
                Icon(
                    Icons.Outlined.DragIndicator,
                    contentDescription = "길게 눌러 테마 순서 변경",
                    tint = textColor.copy(alpha = 0.58f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeHeader(
    palette: HanClipPalette,
    onCycleTheme: () -> Unit,
    onOpenThemeSelection: () -> Unit,
    onOpenAiShot: () -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenFiles: () -> Unit
) {
    var showMediaMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clearAndSetSemantics {
                    contentDescription = "HanClip 로고, 눌러 테마 변경, 길게 눌러 테마 선택"
                    onClick(label = "테마 변경") {
                        onCycleTheme()
                        true
                    }
                    onLongClick(label = "테마 선택 열기") {
                        onOpenThemeSelection()
                        true
                    }
                }
                .combinedClickable(
                    onClick = onCycleTheme,
                    onLongClick = onOpenThemeSelection,
                    onLongClickLabel = "테마 선택 열기"
                )
        ) {
            HanClipBrandCapsule(palette)
        }
        Box {
            Surface(
                modifier = Modifier
                    .size(58.dp)
                    .clickable { showMediaMenu = true }
                    .semantics(mergeDescendants = true) {
                        contentDescription = "미디어 추가"
                    },
                shape = CircleShape,
                color = palette.panel.copy(alpha = palette.panel.alpha * 0.72f),
                border = BorderStroke(1.dp, palette.border.copy(alpha = palette.border.alpha * 0.62f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.padding(15.dp)
                )
            }
            DropdownMenu(
                expanded = showMediaMenu,
                onDismissRequest = { showMediaMenu = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = palette.solidPanel,
                shadowElevation = 12.dp
            ) {
                HomeMediaMenuItem("AiShot", null, palette) {
                    showMediaMenu = false
                    onOpenAiShot()
                }
                HomeMediaMenuItem("사진", Icons.Outlined.Collections, palette) {
                    showMediaMenu = false
                    onOpenPhotos()
                }
                HomeMediaMenuItem("달력", Icons.Outlined.CalendarMonth, palette) {
                    showMediaMenu = false
                    onOpenCalendar()
                }
                HomeMediaMenuItem("파일", Icons.Outlined.FolderOpen, palette) {
                    showMediaMenu = false
                    onOpenFiles()
                }
            }
        }
    }
}

@Composable
private fun HomeMediaMenuItem(
    label: String,
    icon: ImageVector?,
    palette: HanClipPalette,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label, color = palette.text, fontWeight = FontWeight.SemiBold) },
        leadingIcon = {
            if (label == "AiShot") {
                Image(
                    painter = painterResource(R.drawable.aishot_icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(palette.primary)
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = palette.primary)
            }
        },
        onClick = onClick,
        modifier = Modifier.width(190.dp)
    )
}

@Composable
fun HanClipBrandCapsule(palette: HanClipPalette? = null) {
    val brandColor = palette?.primary ?: Color(0xFF07323A)
    Surface(
        shape = RoundedCornerShape(34.dp),
        color = palette?.panel?.copy(alpha = palette.panel.alpha * 0.72f)
            ?: Color(0xFFF7FAF8),
        border = BorderStroke(
            1.dp,
            palette?.border?.copy(alpha = palette.border.alpha * 0.62f)
                ?: Color(0xFFD6E1DE)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .width(154.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_mark),
                contentDescription = null,
                modifier = Modifier.size(35.2.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(brandColor)
            )
            Text(
                text = "HanClip",
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                fontWeight = FontWeight.Black,
                color = brandColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsInfoScreen(
    palette: HanClipPalette,
    sleepPreventionMode: SleepPreventionMode,
    watermarkSettings: WatermarkSettings,
    onSleepPreventionModeChange: (SleepPreventionMode) -> Unit,
    onWatermarkSettingsChange: (WatermarkSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var watermarkExpanded by remember { mutableStateOf(false) }
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HanClipBrandCapsule(palette)
                        Surface(
                            shape = RoundedCornerShape(34.dp),
                            color = palette.panel.copy(alpha = palette.panel.alpha * 0.72f),
                            border = BorderStroke(1.dp, palette.border.copy(alpha = 0.62f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Outlined.Close, "카피라이터 설정 닫기", tint = palette.primary)
                                }
                                IconButton(onClick = {
                                    onWatermarkSettingsChange(WatermarkSettings())
                                    onSleepPreventionModeChange(SleepPreventionMode.Automatic)
                                }) {
                                    Icon(Icons.AutoMirrored.Outlined.Undo, "카피라이터 설정 초기화", tint = palette.primary)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = palette.secondary.copy(alpha = 0.14f)
                        ) {
                            Icon(Icons.Outlined.Badge, null, tint = palette.primary, modifier = Modifier.padding(5.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "카피라이터 설정",
                            color = palette.text.copy(alpha = 0.78f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                item {
                    CopyrightWatermarkCard(
                        palette = palette,
                        settings = watermarkSettings,
                        expanded = watermarkExpanded,
                        onExpandedChange = { watermarkExpanded = it },
                        onChange = onWatermarkSettingsChange
                    )
                }
                item {
                    SleepPreventionInfoCard(
                        palette = palette,
                        mode = sleepPreventionMode,
                        onChange = onSleepPreventionModeChange
                    )
                }
                item { SpecialThanksCard(palette) }
                importantInfoItems().forEach { item ->
                    item {
                        if (item.first == "내장 서체 저작권") {
                            EmbeddedFontCopyrightRow(body = item.second, palette = palette)
                        } else {
                            ImportantInfoRow(
                                title = item.first,
                                body = item.second,
                                palette = palette
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
    }
}

@Composable
private fun CopyrightWatermarkCard(
    palette: HanClipPalette,
    settings: WatermarkSettings,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onChange: (WatermarkSettings) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = palette.secondary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mark),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    colorFilter = ColorFilter.tint(palette.primary)
                )
                Text(
                    "워터마크",
                    modifier = Modifier.weight(1f),
                    color = palette.subText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                CopyrightSegment(
                    text = "사용",
                    selected = settings.logoEnabled,
                    palette = palette,
                    onClick = { onChange(settings.copy(logoEnabled = true)) }
                )
                CopyrightSegment(
                    text = "안함",
                    selected = !settings.logoEnabled,
                    palette = palette,
                    onClick = { onChange(settings.copy(logoEnabled = false)) }
                )
                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        if (expanded) "워터마크 설정 접기" else "워터마크 설정 펼치기",
                        tint = palette.primary
                    )
                }
            }
            if (expanded) {
                Text("로고", color = palette.subText, fontWeight = FontWeight.Bold)
                WatermarkPlatform.entries.chunked(5).forEach { platforms ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        platforms.forEach { platform ->
                            CopyrightPlatformCell(
                                modifier = Modifier.weight(1f),
                                platform = platform,
                                selected = settings.platform == platform,
                                palette = palette,
                                onClick = { onChange(settings.copy(platform = platform, logoEnabled = true)) }
                            )
                        }
                    }
                }
                Text(
                    if (settings.platform == WatermarkPlatform.HanClip) {
                        "HanClip 로고는 완성본의 출처와 앱 브랜드를 표시합니다. 테스트 기간에는 사용 여부와 위치를 제한 없이 선택할 수 있습니다."
                    } else {
                        "${settings.platform.title} 로고를 완성본 저작권 표시에 사용합니다."
                    },
                    color = palette.subText,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("위치", color = palette.subText, fontWeight = FontWeight.Bold)
                WatermarkPosition.entries.chunked(5).forEach { positions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        positions.forEach { position ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .selectable(
                                        selected = settings.copyrightPosition == position,
                                        role = Role.RadioButton,
                                        onClick = { onChange(settings.copy(copyrightPosition = position)) }
                                    )
                                    .semantics {
                                        contentDescription =
                                            "카피라이터 로고 위치, 위에서 ${position.gridRow + 1}번째, 왼쪽에서 ${position.gridColumn + 1}번째"
                                    }
                                    .padding(vertical = 7.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (settings.copyrightPosition == position) palette.secondary.copy(alpha = 0.28f)
                                        else palette.panel.copy(alpha = 0.68f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(16.dp),
                                    shape = CircleShape,
                                    color = if (settings.copyrightPosition == position) palette.primary else Color.Transparent,
                                    border = BorderStroke(2.dp, if (settings.copyrightPosition == position) palette.primary else palette.secondary)
                                ) {}
                            }
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = palette.secondary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, palette.border.copy(alpha = 0.62f))
            ) {
                Text(
                    "테스트 기간 · 전체 기능 사용 중",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = palette.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CopyrightSegment(
    text: String,
    selected: Boolean,
    palette: HanClipPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(18.dp),
            color = if (selected) palette.primary else palette.secondary.copy(alpha = 0.14f)
        ) {
            Text(
                text,
                modifier = Modifier.wrapContentSize(Alignment.Center).padding(horizontal = 14.dp),
                color = if (selected) Color.White else palette.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CopyrightPlatformCell(
    modifier: Modifier,
    platform: WatermarkPlatform,
    selected: Boolean,
    palette: HanClipPalette,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) palette.primary.copy(alpha = 0.18f) else palette.panel.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (selected) palette.primary else palette.border)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(platform.drawableResId),
                contentDescription = platform.title,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun SleepPreventionInfoCard(
    palette: HanClipPalette,
    mode: SleepPreventionMode,
    onChange: (SleepPreventionMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.LightMode, contentDescription = null, tint = palette.primary)
                Text("화면 꺼짐 방지", color = palette.subText, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.secondary.copy(alpha = 0.13f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                SleepPreventionMode.entries.forEach { option ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .selectable(
                                selected = mode == option,
                                role = Role.RadioButton,
                                onClick = { onChange(option) }
                            )
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(19.dp),
                            color = if (mode == option) palette.panel else Color.Transparent
                        ) {
                            Text(
                                option.title,
                                modifier = Modifier.wrapContentSize(Alignment.Center),
                                color = if (mode == option) palette.text else palette.subText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Text(
                mode.detail,
                color = palette.subText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SpecialThanksCard(palette: HanClipPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = palette.secondary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.Outlined.Favorite, null, tint = palette.primary, modifier = Modifier.size(20.dp))
                Text("Special Thanks", color = palette.text, fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
            Text("(주)한통, 한병기, 송기원, 한지우", color = palette.subText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ImportantInfoRow(
    title: String,
    body: String,
    palette: HanClipPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (title == "로고") {
                Image(
                    painter = painterResource(R.drawable.logo_mark),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = importantInfoIcon(title),
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(title, color = palette.text, fontWeight = FontWeight.Bold)
                Text(
                    body,
                    color = palette.subText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private data class EmbeddedFontSizeRow(
    val name: String,
    val size: String,
    val fontId: String
)

private val EmbeddedFontSizeRows = listOf(
    EmbeddedFontSizeRow("고운바탕", "8.0 MB", "gowun_batang"),
    EmbeddedFontSizeRow("마루부리", "7.6 MB", "maruburi"),
    EmbeddedFontSizeRow("고운돋움", "6.9 MB", "gowun_dodum"),
    EmbeddedFontSizeRow("써라운드", "3.7 MB", "cafe24_ssurround"),
    EmbeddedFontSizeRow("프리텐다드B", "2.5 MB", "pretendard_bold"),
    EmbeddedFontSizeRow("넥슨 Lv.1 고딕", "1.8 MB", "nexon_lv1_gothic"),
    EmbeddedFontSizeRow("나눔고딕", "2.0 MB", "nanum_gothic"),
    EmbeddedFontSizeRow("프리텐다드 Regular", "1.5 MB", "pretendard"),
    EmbeddedFontSizeRow("카카오", "1.5 MB", "kakao_big_sans"),
    EmbeddedFontSizeRow("페이퍼로지 Bold", "1.2 MB", "paperlogy_bold"),
    EmbeddedFontSizeRow("젠틀고딕", "1.1 MB", "puradak_gentle_gothic"),
    EmbeddedFontSizeRow("검은고딕", "975 KB", "black_han_sans"),
    EmbeddedFontSizeRow("태나다", "973 KB", "tenada"),
    EmbeddedFontSizeRow("도현", "859 KB", "do_hyeon"),
    EmbeddedFontSizeRow("둘기마요", "743 KB", "ddulgi_mayo"),
    EmbeddedFontSizeRow("Poppins", "157 KB", "poppins")
)

@Composable
private fun EmbeddedFontCopyrightRow(
    body: String,
    palette: HanClipPalette
) {
    val context = LocalContext.current
    val paragraphs = remember(body) {
        body.split("\n\n").filterNot { it.trim() == "[[embedded_font_size_table]]" }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.TextFields,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(22.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("내장 서체 저작권", color = palette.text, fontWeight = FontWeight.Bold)
                paragraphs.forEachIndexed { index, paragraph ->
                    Text(paragraph, color = palette.subText, style = MaterialTheme.typography.bodySmall)
                    if (index == 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.chip)
                                .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(Modifier.fillMaxWidth()) {
                                EmbeddedFontTableText("서체명", Modifier.weight(1.25f), palette, true)
                                EmbeddedFontTableText("파일크기", Modifier.weight(0.72f), palette, true)
                                EmbeddedFontTableText("샘플", Modifier.weight(1f), palette, true)
                            }
                            EmbeddedFontSizeRows.forEach { row ->
                                val sampleFontFamily = remember(row.fontId) {
                                    com.hanclip.android.feature.editor.fontFamilyForName(context, row.fontId)
                                }
                                Box(Modifier.fillMaxWidth().height(1.dp).background(palette.border))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    EmbeddedFontTableText(row.name, Modifier.weight(1.25f), palette)
                                    EmbeddedFontTableText(row.size, Modifier.weight(0.72f), palette)
                                    Text(
                                        "안녕하세요",
                                        modifier = Modifier.weight(1f),
                                        color = palette.primary,
                                        fontSize = 12.sp,
                                        fontFamily = sampleFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmbeddedFontTableText(
    text: String,
    modifier: Modifier,
    palette: HanClipPalette,
    header: Boolean = false
) {
    Text(
        text,
        modifier = modifier,
        color = palette.subText,
        fontSize = if (header) 11.sp else 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun importantInfoIcon(title: String): ImageVector = when (title) {
    "카피라이터" -> Icons.Outlined.Info
    "워터마크" -> Icons.Outlined.Badge
    "첫 화면" -> Icons.Outlined.Home
    "iPad 지원" -> Icons.Outlined.GridView
    "영화 프리셋" -> Icons.Outlined.GridView
    "영상 시간 필터" -> Icons.Outlined.Timelapse
    "사진 정렬" -> Icons.Outlined.SwapHoriz
    "퀵모드" -> Icons.Outlined.Bolt
    "여행 영화" -> Icons.Outlined.Flight
    "Quick" -> Icons.Outlined.Bolt
    "인생 영화" -> Icons.Outlined.Favorite
    "Ai" -> Icons.Outlined.AutoFixHigh
    "AiShot" -> Icons.Outlined.AddPhotoAlternate
    "영화 목록" -> Icons.Outlined.Collections
    "컬렉션" -> Icons.AutoMirrored.Outlined.LibraryBooks
    "컬렉션 포스터" -> Icons.Outlined.Movie
    "영화 화면" -> Icons.Outlined.Movie
    "영화 설정" -> Icons.Outlined.Tune
    "클립목록" -> Icons.Outlined.VideoFile
    "클립 리스트" -> Icons.Outlined.VideoFile
    "묶음사진" -> Icons.Outlined.Collections
    "자동 / 수동 / 전체" -> Icons.Outlined.SwapHoriz
    "사용 / 제외" -> Icons.Outlined.SwapHoriz
    "사진 / 영상" -> Icons.Outlined.PlayCircle
    "순서변경 상태" -> Icons.Outlined.DragIndicator
    "한컷 / 분할" -> Icons.Outlined.MovieCreation
    "자사진" -> Icons.Outlined.AddPhotoAlternate
    "편집 영역 / 편집 모드" -> Icons.Outlined.Tune
    "시사회" -> Icons.Outlined.PlayCircle
    "한클립 전체화면 플레이어" -> Icons.Outlined.PlayCircle
    "만들기" -> Icons.Outlined.AutoFixHigh
    "영상 생성 진행창" -> Icons.Outlined.Timelapse
    "개봉하기 창" -> Icons.Outlined.IosShare
    "테마 선택창" -> Icons.Outlined.Palette
    "첫 화면 이동 팝업" -> Icons.Outlined.Home
    "로고" -> Icons.Outlined.Favorite
    "카피라이터 입력" -> Icons.Outlined.TextFields
    "세그먼트 컨트롤" -> Icons.Outlined.SwapHoriz
    "단일 / 다중" -> Icons.Outlined.Collections
    "모클립" -> Icons.Outlined.Collections
    "자클립" -> Icons.Outlined.MovieCreation
    "웨이브 / 웨이브 인디케이터" -> Icons.Outlined.GraphicEq
    "선택바" -> Icons.Outlined.SwapHoriz
    "자동 진행" -> Icons.Outlined.Repeat
    "달력 썸네일 버튼" -> Icons.Outlined.CalendarMonth
    "브라우저" -> Icons.Outlined.Public
    "자막" -> Icons.Outlined.TextFields
    "촬영 기간 삽입" -> Icons.Outlined.CalendarMonth
    "엔딩" -> Icons.Outlined.TravelExplore
    "엔딩 카드 테마" -> Icons.Outlined.Palette
    "외부 호출 주소" -> Icons.Outlined.TravelExplore
    "샘플 음악" -> Icons.Outlined.LibraryMusic
    "외부 음악" -> Icons.Outlined.Public
    "내장 서체 저작권" -> Icons.Outlined.TextFields
    else -> Icons.Outlined.Info
}

private fun importantInfoItems(): List<Pair<String, String>> = listOf(
    "카피라이터" to """
        첫 화면 하단의 i 원형 유리 버튼입니다. 카피라이터 설정과 설정 정보를 보여주는 창입니다.
    """.trimIndent(),
    "로고" to """
        상단의 앱 심볼과 HanClip 글자 부분입니다. 화면에 따라 닫기, 첫 화면 이동, 테마 선택 같은 동작의 기준점이 됩니다.
    """.trimIndent(),
    "첫 화면" to """
        앱 실행 후 영화 프리셋과 저장된 영화 목록이 보이는 홈 화면입니다.
    """.trimIndent(),
    "iPad 지원" to """
        iPad에서 세로·가로 방향과 분할 화면 크기에 맞춰 사용할 수 있습니다. 넓은 화면에서는 편집 콘텐츠의 읽기 좋은 폭을 유지하며 사진 선택, 공유 확장과 잠금 화면 위젯도 함께 사용할 수 있습니다.
    """.trimIndent(),
    "영화 프리셋" to """
        첫 화면 상단에서 새 영화, 퀵모드, AiShot, 여행 영화, 인생 영화, 골프 영화 중 원하는 설정으로 영화 제작을 시작하는 영역입니다.
    """.trimIndent(),
    "퀵모드" to """
        새 영화의 기본 설정에 음악을 켠 빠른 제작 기능입니다. 미디어를 고르면 30초, 45초, 1분, 2분, 3분, 5분, 추천시간 또는 최소시간을 고릅니다. 추천시간은 미디어당 1초, 최소시간은 미디어당 0.2초로 계산합니다. 선택한 미디어가 많으면 정해진 시간보다 최소시간이 길 때 가능한 최소 시간으로 자동 보정하며, −와 +로 5초씩 조절할 수 있습니다. 시간 화면에서 영화 제작과 같은 자막·음악 패널을 사용할 수 있습니다. 확정하면 선택 시간÷원본 미디어 수로 기본시간을 정해 편집 화면을 거치지 않고 영화를 만들며, 시사회에서 다시 편집을 누르면 퀵모드 영상 길이 화면으로 돌아갑니다. 외부 주소 hanclip://quick으로 바로 실행할 수 있습니다.
    """.trimIndent(),
    "여행 영화" to """
        기본시간 1초, 라이브포토 영상, 영상 분할, 묶음사진 1/6 자동, 여행 서체와 여행의 설렘 음악을 적용합니다. 촬영 기간과 많이 촬영한 지역 최대 두 곳을 자막에 넣고, 마지막 엔딩 카드는 보물지도를 기본으로 사용합니다.
    """.trimIndent(),
    "인생 영화" to """
        기본시간 2초, 라이브포토 영상, 영상 분할, 묶음사진 1/3 자동과 오늘 날짜 자막을 적용해 삶의 기록을 영화로 만드는 프리셋입니다.
    """.trimIndent(),
    "Ai" to """
        한클립 안에서 가장 행복하고, 가장 흥분되고, 꼭 기억하고 싶은 순간을 더 잘 찾기 위해 계속 개발하고 있는 판단 능력입니다.

        Ai는 AiShot 촬영뿐 아니라 여러 영상의 자클립 선택, 사진 묶음의 대표 컷 선택처럼 한클립 안에서 자동으로 좋은 순간을 고르는 기능들에 함께 쓰입니다. 골프 영상에만 머물지 않고 생활 영상과 여행 영상에서도 더 자연스럽게 좋은 장면을 찾는 방향으로 키우고 있습니다.

        현재 Ai 버전은 0.2.1입니다.

        0.1.0 - 소리를 중심으로 티샷, 박수, 갑자기 좋아지는 순간과 이어지는 반응을 찾아내는 첫 기준입니다.

        0.2.0 - 소리를 중심으로 보면서 AiShot 촬영 중 화면의 움직임과 밝기 변화도 함께 참고합니다.

        0.2.1 - 맥에 있는 798개 영상 공부 결과를 반영한 보정판입니다. 큰 소리 자체보다 그 뒤에 이어지는 반응과 화면 변화를 더 차분하게 함께 봅니다.

        새 Ai가 마음에 들지 않으면 이전 Ai 버전으로 되돌릴 수 있도록 버전별 특징을 남겨 둡니다.
    """.trimIndent(),
    "AiShot" to """
        필요한 순간을 자동으로 찾아 클립에 담는 실시간 촬영 기능입니다. 촬영을 닫을 때까지 계속 살피며, 만들어진 클립은 Ai 영화에 차례로 추가됩니다.

        감지 중, 감지 됨, 저장 중으로 촬영 상태를 보여줍니다. 주변 환경에 맞춰 시끄러움, 일반, 조용함, 자동 감도를 선택할 수 있으며 기본값인 자동은 주변 상황에 맞춰 감도를 조절합니다. 샷 시간은 짧게(앞뒤 2초), 일반(앞 2초·뒤 3초), 길게(앞뒤 5초) 중에서 선택하며 촬영 중 변경하면 다음 촬영부터 적용됩니다.

        전면 또는 후면 카메라와 줌 배율을 선택해 3:4 화면 비율로 촬영합니다. 필요한 순간에는 촬영 버튼을 눌러 수동으로 클립을 남길 수 있습니다.
    """.trimIndent(),
    "영화 목록" to """
        첫 화면에 저장된 일반 영화와 AiShot 영화가 한 목록에 표시됩니다. 왼쪽의 숫자는 최대 10개 중 현재 저장된 영화 수이며, 각 행의 시간 앞 아이콘은 영화를 시작할 때 사용한 프리셋 종류를 보여줍니다.
    """.trimIndent(),
    "컬렉션" to """
        완성된 영화를 포스터 형태로 최대 30개까지 보관합니다. 기기 내 Vision AI가 영상 여러 구간의 얼굴·주목 영역·구도·밝기·대비·선명도를 비교해 가장 좋은 순간을 포스터로 선택합니다. 기존 포스터도 새 AI 기준으로 한 번 자동 재생성합니다. 포스터 롱터치 메뉴의 썸네일 AI 재선택은 전체화면에서 디바이스 AI 후보 8개와 한클립 AI 후보 8개를 좌우로 비교합니다. 모든 후보에는 실제 제목·핀·제작·촬영·위치·재생시간이 적용됩니다. 재생성을 누르면 지금까지 거절한 후보의 프레임 시간과 이미지 특징을 제외하고 다른 느낌의 후보 16개를 다시 찾습니다. 핀이 꽂힌 포스터는 길게 누른 채 다른 핀 포스터로 끌어 놓아 순서를 바꿀 수 있습니다. 컬렉션 선반 아래의 숨김 메뉴에서는 전체 영상을 720p 또는 540p로 일괄 변환하며 이미 해당 해상도 이하인 영상은 유지합니다. 포스터를 누르면 한클립 전용 전체화면 플레이어로 재생하며, 실제 기기 방향에 맞춰 앱 화면 방향 자체와 안전영역을 세로·가로로 함께 전환합니다. 한 손가락을 위로 밀어 시작하면 시스템 볼륨을 조절하며 손을 떼지 않고 아래로 되돌리면 음량이 낮아지고, 아래로 밀어 시작하면 플레이어를 닫습니다. 세로와 가로 모두 핀치로 0.5배부터 4배까지 확대·축소하고 확대 상태에서 한 손가락으로 화면을 이동하며 더블 탭하면 100% 크기로 돌아갑니다.
    """.trimIndent(),
    "한클립 전체화면 플레이어" to """
        컬렉션 재생, 웹에서 찾은 동영상 보기, 완성본 시사회가 함께 사용하는 공통 동영상 플레이어입니다. 실제 기기 방향에 맞춰 앱 화면과 안전영역을 전환하며 가로 화면에서는 화면 맞춤과 채우기를 선택할 수 있습니다. 화면을 한 번 누르면 재생하거나 멈추고, 가로 밀기로 재생 위치를 찾습니다. 위로 밀어 시작하면 손을 떼기 전까지 시스템 볼륨 조절로 유지되어 아래로 되돌릴 때 바로 음량이 낮아지며, 아래로 밀어 시작하면 플레이어를 닫습니다. 핀치로 0.5배부터 4배까지 확대·축소하고 확대 상태에서 화면을 이동하며 더블 탭하면 100% 크기로 돌아갑니다.
    """.trimIndent(),
    "테마 선택창" to """
        로고를 길게 눌렀을 때 테마를 선택하는 창입니다.
    """.trimIndent(),
    "첫 화면 이동 팝업" to """
        편집 중 로고를 눌렀을 때 홈 + 저장, 홈으로를 선택하는 창입니다.
    """.trimIndent(),
    "영화 화면" to """
        미디어를 선택한 후 기본 재생 시간, 화면 비율, 클립목록 등을 편집하는 화면입니다.
    """.trimIndent(),
    "영상 시간 필터" to """
        사진 화면의 필터에서 설정한 시간 이상 또는 이하인 영상을 찾는 기능입니다. 시간 필터를 적용하는 동안에는 사진과 라이브포토를 숨기고 영상만 표시합니다. 1분, 3분, 5분, 10분을 빠르게 고르거나 분과 초를 직접 선택할 수 있으며, 필터를 해제하면 이전에 선택했던 미디어 종류가 복원됩니다.
    """.trimIndent(),
    "사진 정렬" to """
        사진 화면의 필터에서 날짜순 또는 추가순을 선택합니다. 선택된 정렬을 다시 누르면 글자 옆 화살표가 바뀌며 오름차순과 내림차순이 전환됩니다. 날짜순은 촬영일을 사용하고, 추가순은 사진 보관함의 추가·변경 시각을 사용합니다. 영화 제작, 퀵모드와 컬렉션의 공용 사진 화면에 동일하게 적용됩니다.
    """.trimIndent(),
    "영화 설정" to """
        영화 화면의 로고 아래에 있는 클립 설정 패널입니다. 처음에는 제목 행만 보이도록 접혀 있으며, 클립 설정 행 어디를 눌러도 펼치거나 다시 접을 수 있습니다. 오른쪽 표시판은 이 영화가 새 영화, 퀵모드, AiShot, 여행 영화, 인생 영화 또는 골프 영화 중 어떤 프리셋으로 시작했는지 보여주며 프로젝트를 다시 불러와도 유지됩니다. 기존 버전에서 저장해 시작 프리셋 정보가 없는 영화는 기존 영화로 표시합니다. 영상 길이, 기본시간, 라이브포토, 영상 분할, 묶음사진, 자막, 음악과 엔딩을 설정합니다.
    """.trimIndent(),
    "클립목록" to """
        선택한 사진, 라이브포토, 영상이 순서대로 표시되는 목록입니다. 묶음사진은 실제 사진이 아니라 비슷한 사진들을 담는 행으로 표시되며, 아래에 들어 있는 자사진에서 실제 사용할 컷을 확인합니다.
    """.trimIndent(),
    "묶음사진" to """
        연속으로 촬영된 사진과 라이브포토 중 촬영 시각, 화면 비율, 밝기와 화면 구도가 모두 비슷한 장면을 하나로 담아 중복을 줄이는 기능입니다. 묶음 행의 숫자는 후보 수가 아니라 영상에 사용하기로 선택된 자사진 수입니다. 클립설정의 ‘1/6’은 6장마다 1장을 자동으로 고른다는 뜻이며, −와 +로 비율을 조절할 수 있습니다. 수동은 묶음을 펼쳐 사용할 사진을 직접 고르며, 전체는 모든 사진을 사용합니다.
    """.trimIndent(),
    "자동 / 수동 / 전체" to """
        묶음사진에서 사용할 사진을 Ai가 고르게 할지, 사용자가 직접 고를지, 모든 사진을 사용할지 정하는 선택입니다.
    """.trimIndent(),
    "사용 / 제외" to """
        수동으로 펼친 자사진 행에서 해당 사진 또는 라이브포토를 영상에 넣을지 뺄지 정하는 상태 버튼입니다.
    """.trimIndent(),
    "사진 / 영상" to """
        라이브포토를 일반 사진으로 쓸지, 짧은 영상으로 쓸지 정하는 선택입니다.
    """.trimIndent(),
    "순서변경 상태" to """
        큰 단위의 순서를 바꾸는 화면입니다. 묶음사진은 안의 자사진을 흩어 놓지 않고 하나의 묶음 타일로 이동하며, 숫자는 선택된 자사진 수를 뜻합니다.
    """.trimIndent(),
    "세그먼트 컨트롤" to """
        자동 / 수동 / 전체, 사진 / 영상, 한컷 / 분할처럼 사용 방식을 고르는 스위치형 컨트롤입니다.
    """.trimIndent(),
    "한컷 / 분할" to """
        영상 클립을 하나의 구간으로 쓸지, Ai가 찾은 피크 기준으로 여러 자클립으로 나눌지 정하는 선택입니다.
    """.trimIndent(),
    "모클립" to """
        다중 분할을 만들 때 원본 역할로 남는 부모 클립입니다.
    """.trimIndent(),
    "자클립" to """
        모클립에서 Ai가 찾은 피크 기준으로 만들어진 하위 클립입니다. 삭제는 원본 삭제가 아니라 비선택으로 처리되며, 비선택된 자클립은 클립목록에서 다시 확인하고 선택할 수 있습니다.
    """.trimIndent(),
    "자사진" to """
        묶음사진 안에 들어 있는 실제 사진 또는 라이브포토입니다. 수동 모드에서 사용 또는 제외 상태를 고릅니다.
    """.trimIndent(),
    "편집 영역 / 편집 모드" to """
        개별 클립을 누르면 열리는 구간 선택 및 재생 화면입니다.
    """.trimIndent(),
    "웨이브 / 웨이브 인디케이터" to """
        영상/Live Photo 편집에서 소리 파형을 보여주는 영역입니다.
    """.trimIndent(),
    "선택바" to """
        웨이브 인디케이터의 좌우 끝에 있는 드래그 바입니다.
    """.trimIndent(),
    "자동 진행" to """
        편집에서 클립 재생이 끝나면 다음 클립으로 이어지고, 마지막 클립 뒤에는 처음 클립으로 계속 이어지는 기능입니다.
    """.trimIndent(),
    "달력 썸네일 버튼" to """
        달력에서 미디어를 고르는 화면에 있는 위/아래 이동 버튼입니다.
    """.trimIndent(),
    "만들기" to """
        전체 클립을 하나의 영상으로 생성하는 액션과 버튼입니다.
    """.trimIndent(),
    "영상 생성 진행창" to """
        영상을 만드는 동안 썸네일, 진행바, 진행률, 취소 버튼이 표시되는 창입니다.
    """.trimIndent(),
    "시사회" to """
        만들기 완료 후, 저장 또는 개봉하기 직전에 제작된 전체 영화를 확인하는 화면입니다.
    """.trimIndent(),
    "개봉하기 창" to """
        시사회에서 사진 앱 또는 파일 앱 개봉 방식을 선택하는 창입니다.
    """.trimIndent(),
    "브라우저" to """
        외부 웹페이지를 이용하는 화면입니다. 웹페이지에서 영상이 감지되면 다운, 보기, 닫기 버튼이 나타납니다. 다운은 영상을 가져오고, 보기는 감지된 영상을 전체 화면으로 재생하며, 닫기는 영상 감지 알림만 닫습니다. 즐겨찾기 패널의 파비콘을 누르면 삭제하고, 길게 누르면 첫 홈페이지로 지정합니다. 즐겨찾기 편집에서는 현재 목록을 파일로 저장할 수 있습니다. 저장한 즐겨찾기 파일을 한클립으로 공유해 불러오면 같은 주소는 가져온 값으로 덮어쓰고 새 주소만 추가합니다.
    """.trimIndent(),
    "자막" to """
        영화 화면의 미디어 추가 메뉴에서 여는 설정창입니다. 결과 영상 위에 문구를 합성할지, 문구와 색상, 서체, 그림자, 위치를 설정합니다. 자막 문구가 비어 있어도 사용을 선택할 수 있어 마지막 엔딩 카드만 넣는 방식으로도 사용할 수 있습니다.
    """.trimIndent(),
    "촬영 기간 삽입" to """
        선택한 미디어의 첫 촬영일부터 마지막 촬영일까지를 자막에 넣는 기능입니다. 기본 자막이면 기존 문구를 바꾸고, 사용자가 편집한 자막이면 현재 커서 위치에 삽입합니다.
    """.trimIndent(),
    "엔딩" to """
        클립 설정의 음악 아래에 독립된 행으로 표시되며 기본값은 안함입니다. 지도 아이콘과 현재 테마명, 표시 시간 조절, 사용·안함 상태를 한 행에서 설정합니다. 현재 위치 정보가 없어도 사용과 테마를 미리 설정할 수 있고, 이후 위치 정보가 있는 미디어를 추가하면 저장된 설정이 적용됩니다. 촬영 날짜와 위치 정보가 있는 영화의 마지막에 촬영기간과 도시 이동 경로를 여행 기록 카드로 넣습니다. 같은 도시라도 촬영 날짜가 바뀌면 새 일정으로 표시하며, 지역 이동은 차량, 국가 이동은 비행기 아이콘으로 연결합니다. 대한민국은 도시만 표시하고 해외는 국가가 처음 나오거나 바뀔 때만 국가명을 표시합니다. 도시 이름은 줄을 바꾸지 않고 한 줄로 표시합니다. 엔딩 카드 시간은 1~10초 범위에서 0.5초 단위로 조절하며 자막, 보물지도, 여행일정, 랜드마크, 오피스 테마를 선택할 수 있습니다. 퀵모드에서도 같은 행과 설정 화면을 사용합니다.
    """.trimIndent(),
    "엔딩 카드 테마" to """
        영화 마지막 여행 기록 카드의 디자인입니다. 설정 위쪽에서 테마와 표시 시간을 고르고 현재 영화 화면 비율 그대로 실제 결과를 미리 봅니다. 자막은 현재 자막 서체·글자색·그림자를 이어받습니다. 보물지도는 고전 서체와 점선 경로를 사용하며 선택된 보물지도를 다시 누르면 새 경로로 재생성합니다. 여행일정은 DAY 번호 대신 각 지역의 실제 촬영 날짜를 표시합니다. 랜드마크는 국내외 주요 도시의 대표 명소와 iPhone 기본 그림문자를 자동 조합하고 미등록 지역에는 대표 여행 아이콘을 사용합니다. 오피스는 문서번호, 촬영기간, 날짜·지역·이동수단 표가 있는 정형 보고서입니다.
    """.trimIndent(),
    "컬렉션 포스터" to """
        컬렉션은 영화 포스터를 세로로 이어지는 2열 배열로 보여주며 영화 추가 포스터는 목록의 마지막에 배치합니다. 사진은 영화 제작과 같은 사진·달력 전환 화면을 사용하되 완성 영화를 가져오는 용도이므로 영상만 표시하고 선택합니다. 파일에서도 동영상만 가져옵니다. 가져오는 동안 진행바와 완료 개수를 표시합니다. 포스터를 길게 눌러 제목 수정, 공유, 컬렉션 제거를 사용하며 제목 수정 입력창은 글의 줄 수에 맞춰 커지고 키보드 위 가용 높이를 넘으면 내부에서 스크롤합니다.
    """.trimIndent(),
    "워터마크" to """
        카피라이터에서 설정하는 기능입니다. 한클립 로고 또는 사용자가 선택한 표시를 결과 영상에 합성할지 결정합니다.
    """.trimIndent(),
    "외부 호출 주소" to """
        Ai  hanclip://aishot
        퀵모드  hanclip://quick
        파일  hanclip://files
        달력  hanclip://calendar
        사진  hanclip://photo
        검색  hanclip://search
        첫 화면  hanclip://open
    """.trimIndent(),
    "샘플 음악" to """
        HanClip에 포함된 샘플 음악 햇살 한 컷은 앱 기능 검증과 사용자의 일상 영상 배경음악을 위해 인공지능 생성 및 합성 방식으로 만든 샘플 음악입니다.

        이 샘플 음악은 외부 음원, 기존 곡, 상용 음악 라이브러리, 사람의 실연 녹음 파일을 가져와 사용하지 않았으며, HanClip 앱 안에서 제공되는 기본 샘플 자산입니다. 사용자는 이 샘플 음악을 HanClip으로 만든 영상 결과물의 배경음악으로 사용할 수 있습니다.

        샘플 음악 중 '지우에게 첫눈이란'은 앱 제작자의 가족이 직접 만든 개인 창작 음악을 원 저작자의 허락을 받아 HanClip 앱 안에 샘플 음악으로 포함한 곡입니다. '베이비 워킹'은 이 곡에서 느껴지는 첫눈의 감정과 경쾌한 분위기를 참고하되, 원곡 음원이나 멜로디를 직접 사용하지 않고 HanClip 샘플용으로 새롭게 생성한 음악입니다.

        영화 프리셋의 '여행의 설렘'과 '골프치러 가자'도 HanClip에 포함된 샘플 음악이며 각 프리셋에서 자동으로 선택됩니다.
    """.trimIndent(),
    "외부 음악" to """
        음악 설정 화면의 '브라우저'는 사용자가 외부 무료 음원 사이트에서 직접 음악을 찾고 다운로드할 수 있도록 Pixabay Music과 Mixkit Music 같은 공식 웹페이지를 여는 기능입니다. HanClip은 이 외부 사이트의 음원을 앱에 내장하거나 샘플 음악으로 재배포하지 않으며, 사용자가 직접 다운로드한 파일을 사용자의 영화 배경음악으로 불러와 합성하는 방식으로 동작합니다.

        Pixabay Music과 Mixkit Music에서 다운로드한 음악은 HanClip 내장 샘플 음악이 아니며, 각 음원의 권리와 이용 조건은 해당 사이트의 라이선스와 곡별 안내를 따릅니다. 사용자는 다운로드 시점의 Pixabay Content License, Mixkit License, 곡별 안내, 다운로드 기록을 확인하고 보관한 뒤 자신이 만든 영상에 사용할 책임이 있습니다.

        HanClip은 외부 음원 파일을 독립 음원으로 판매, 배포, 재라이선스하거나 음악 라이브러리 형태로 제공하지 않습니다. 외부 음원은 사용자가 선택한 영상 결과물 안에 배경음악으로 합성될 때만 사용되며, TV/라디오 방송, 게임, CD/DVD, 음원 단독 배포 등 각 사이트가 제한하는 용도에는 사용자가 별도 라이선스 확인 또는 권리자의 허락을 받아야 합니다.
    """.trimIndent(),
    "내장 서체 저작권" to """
        HanClip에는 사용자가 영상 위에 짧은 문구나 자막을 넣을 때 선택할 수 있도록 Kakao Big Sans, Nanum Gothic, Pretendard, MaruBuri, Puradak Gentle Gothic, Tenada, Cafe24 Ssurround, Ddulgi Mayo, Gowun Dodum, Gowun Batang, Black Han Sans, Do Hyeon, Paperlogy, NEXON Lv.1 Gothic, Poppins 서체가 포함되어 있습니다. 이 서체들은 앱 전체 UI 기본 서체가 아니라, 자막 편집 미리보기와 최종 영상 렌더링 과정에서만 선택적으로 사용됩니다.

        [[embedded_font_size_table]]

        내장 자막 서체 파일의 원본 크기 합계는 약 41.5 MB입니다. 앱 번들, 압축, App Store 처리 방식에 따라 최종 설치 크기와 다운로드 크기는 달라질 수 있습니다.

        Kakao Big Sans, Nanum Gothic, Pretendard, Tenada, Gowun Dodum, Gowun Batang, Black Han Sans, Do Hyeon, Paperlogy, Poppins는 SIL Open Font License 1.1로 제공되는 서체입니다. OFL은 서체 파일을 단독으로 판매하지 않는 조건에서 사용, 복사, 앱 또는 소프트웨어 번들, 임베딩, 재배포를 허용합니다. 또한 이 서체를 사용해 만든 영상, 이미지, 문서 같은 결과물 자체는 서체 라이선스의 적용 대상이 아니므로 HanClip으로 만든 영상 결과물의 저작권이나 이용 조건은 사용자가 정한 조건을 따릅니다.

        MaruBuri의 저작권은 NAVER 및 NAVER Cultural Foundation에 있습니다. NAVER 안내에 따라 개인과 기업을 포함한 모든 사용자가 무료로 사용할 수 있고 상업적 사용이 가능하며, 글꼴 자체를 유료로 판매하는 행위를 제외하고 저작권 안내와 라이선스 전문을 포함해 다른 소프트웨어와 번들하거나 재배포할 수 있다고 설명합니다.

        Pretendard는 Kil Hyung-jin 및 원 기반 서체 저작권자의 저작권 고지와 함께 SIL Open Font License 1.1로 제공됩니다. Pretendard, Source, Inter, M PLUS 1 등 예약된 서체명은 수정본에 임의로 사용할 수 없습니다. HanClip은 공식 배포 파일을 수정하지 않고 앱에 포함합니다.

        Gowun Dodum, Gowun Batang, Black Han Sans, Do Hyeon은 Google Fonts의 공식 google/fonts 저장소에서 제공되는 SIL Open Font License 1.1 서체입니다. Google Fonts 안내에 따라 상업적 제품, 앱, 웹사이트, 인쇄물, 영상 등에서 사용할 수 있으며, HanClip은 공식 저장소의 원본 TTF 파일과 OFL 라이선스 전문을 함께 포함합니다. 수정본을 배포하는 경우에는 OFL 조건과 예약 서체명 제한을 별도로 확인해야 합니다.

        Tenada는 공식 배포 페이지에서 SIL Open Font License 1.1로 제공됩니다. 앱에 포함된 Tenada.ttf는 공식 배포본의 원본 파일이며, HanClip에서는 골프 기록, 홀 정보, 스코어 같은 제목형 자막에 사용할 수 있도록 제공합니다.

        Paperlogy는 제작자의 공식 저장소에서 배포한 1.001 버전의 Bold 원본 파일이며, Poppins는 Google Fonts 공식 저장소의 Regular 원본 파일입니다. 두 파일 모두 SIL Open Font License 1.1 전문과 저작권 고지를 함께 포함합니다.

        NEXON Lv.1 Gothic의 저작권은 NEXON Korea에 있습니다. 넥슨의 공식 이용 조건에 따라 원본 파일을 수정하지 않고 저작권 안내와 함께 앱에 번들했으며, 글꼴 파일 자체를 단독 판매하지 않습니다.

        Cafe24 Ssurround는 Cafe24 공식 안내에 따라 개인 및 기업 사용자를 포함한 모든 사용자에게 무료로 제공되며 상업적 사용이 가능합니다. Cafe24는 영상 제작 및 자막, 소프트웨어 번들, 특정 프로그램 임베드 등 사용 범위 제한 없이 이용할 수 있다고 안내합니다. 단, 글꼴 파일 자체를 유료로 판매하는 행위는 금지됩니다.

        Puradak Gentle Gothic은 Puradak Chicken 공식 폰트 페이지에서 무료로 배포되는 서체입니다. 공개 사용 안내에 따라 상업적, 비상업적 사용과 영상 자막, 앱 사용, 소프트웨어 번들이 가능하며, HanClip은 공식 TTF 파일을 수정하지 않고 포함합니다. 서체 파일 자체를 단독 판매하거나 저작권 고지를 제거해서 재배포해서는 안 됩니다.

        Ddulgi Mayo는 제작자 공식 블로그에서 개인 및 기업의 상업적 이용이 가능하고 자유롭게 사용할 수 있다고 안내된 서체입니다. HanClip은 제작자가 공개한 원본 OTF 파일을 수정하지 않고 포함합니다. 다만 OFL처럼 세부 재배포 조건이 긴 전문 형태로 제공된 서체는 아니므로, HanClip에서는 원본 파일과 저작권 고지를 함께 보관하고 서체 파일 자체를 단독 판매하지 않습니다. 향후 라이선스 정책이 바뀌거나 앱 번들/재배포 조건이 더 엄격하게 확인될 경우에는 우선 검토 또는 제거 대상입니다.

        모든 내장 서체의 라이선스 전문, 저작권 고지, 확인한 공식 배포처 정보, 파일 크기 정리는 앱 번들에 포함된 font-licenses 파일을 기준으로 보관합니다. 서체 파일을 수정하거나 별도 재배포하는 경우에는 각 서체의 원 라이선스와 저작권 고지를 유지해야 하며, 예약된 서체명이 있는 경우 수정본에 원래 이름을 사용할 수 없습니다.
    """.trimIndent(),
)

@Composable
private fun SharedInboxDialog(
    count: Int,
    copyCurrent: Int,
    copyTotal: Int,
    palette: HanClipPalette,
    onCreateMovie: () -> Unit,
    onAddToMovie: () -> Unit,
    onClear: () -> Unit,
    onCancelCopy: () -> Unit
) {
    val isCopying = copyTotal > 0 && copyCurrent < copyTotal
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(palette.primary, palette.secondary, palette.primary)
                        )
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.95f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Collections,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Text(
                        text = if (isCopying) "공유 파일을 옮기는 중" else "공유파일 ${count}개 발견",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                if (isCopying) {
                    val progress = copyCurrent.toFloat() / copyTotal.coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f)
                    )
                    Text(
                        "$copyCurrent/${copyTotal}개 파일을 복사하는 중입니다.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onCancelCopy,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                    ) { Text("닫기", color = Color.White) }
                } else {
                    Text(
                        "새 영화를 만들거나 저장된 영화에 추가할 수 있습니다.",
                        color = Color.White.copy(alpha = 0.86f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SharedInboxActionButton(
                            title = "새 영화 제작",
                            modifier = Modifier.weight(1f),
                            onClick = onCreateMovie
                        )
                        SharedInboxActionButton(
                            title = "영화에 추가",
                            modifier = Modifier.weight(1f),
                            onClick = onAddToMovie
                        )
                        SharedInboxActionButton(
                            title = "비우기",
                            modifier = Modifier.weight(1f),
                            onClick = onClear
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedInboxActionButton(
    title: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f)),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PresetGrid(
    onStartPreset: (MoviePreset) -> Unit,
    palette: HanClipPalette,
    columnCount: Int
) {
    val orderedPresets = listOf(
        MoviePreset.NewMovie,
        MoviePreset.Quick,
        MoviePreset.AiShot,
        MoviePreset.Travel,
        MoviePreset.Life,
        MoviePreset.Golf
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeSectionTitle("영화 프리셋", Icons.Outlined.Collections, palette)
        orderedPresets.chunked(columnCount).forEach { rowPresets ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowPresets.forEach { preset ->
                    PresetTile(
                        modifier = Modifier.weight(1f),
                        preset = preset,
                        icon = when (preset) {
                            MoviePreset.NewMovie -> Icons.Outlined.VideoLibrary
                            MoviePreset.Quick -> Icons.Outlined.Bolt
                            MoviePreset.AiShot -> null
                            MoviePreset.Travel -> Icons.Outlined.Flight
                            MoviePreset.Life -> Icons.Outlined.Favorite
                            MoviePreset.Golf -> Icons.Outlined.SportsGolf
                        },
                        palette = palette,
                        onClick = { onStartPreset(preset) }
                    )
                }
                repeat(columnCount - rowPresets.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PresetTile(
    modifier: Modifier,
    preset: MoviePreset,
    icon: ImageVector?,
    palette: HanClipPalette,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(108.dp)
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.solidPanel.copy(alpha = 0.96f),
                        palette.panel,
                        palette.secondary.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, palette.border, cardShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(palette.primary, palette.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (preset == MoviePreset.AiShot) {
                    Image(
                        painter = painterResource(R.drawable.aishot_icon),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = preset.title,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.detail,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                color = palette.subText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeSectionTitle(
    title: String,
    icon: ImageVector,
    palette: HanClipPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(18.dp),
            shape = RoundedCornerShape(10.dp),
            color = palette.secondary.copy(alpha = 0.10f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = palette.primary.copy(alpha = 0.72f),
                modifier = Modifier.padding(4.dp)
            )
        }
        Spacer(Modifier.size(7.dp))
        Text(
            title,
            color = palette.text.copy(alpha = 0.76f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PresetRecommendationBadge(palette: HanClipPalette) {
    Surface(
        shape = RoundedCornerShape(50),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.26f))
    ) {
        Text(
            text = "대표",
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            color = palette.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

private fun LazyListScope.savedProjectItems(
    palette: HanClipPalette,
    summaries: List<ExportedMovieSummary>,
    recentlySavedMovieUriString: String?,
    hasDraftProject: Boolean,
    editableProjectSummaries: List<DraftProjectSummary>,
    standardProjectColumnCount: Int,
    onOpenProject: () -> Unit,
    onOpenEditableProject: (DraftProjectSummary) -> Unit,
    onRemoveEditableProject: (DraftProjectSummary) -> Unit,
    onToggleEditableProjectPin: (DraftProjectSummary) -> Unit,
    onEditEditableProjectMemo: (DraftProjectSummary) -> Unit,
    onOpenExportedMovie: (ExportedMovieSummary) -> Unit,
    onRemoveExportedMovie: (ExportedMovieSummary) -> Unit,
    onToggleExportedMoviePin: (ExportedMovieSummary) -> Unit,
    onEditExportedMovieMemo: (ExportedMovieSummary) -> Unit,
    collectionMovies: List<CollectedMovie>,
    isImportingCollection: Boolean,
    collectionImportCompleted: Int,
    collectionImportTotal: Int,
    onImportCollection: () -> Unit,
    onCancelCollectionImport: () -> Unit,
    collectionPosterRepairCompleted: Int,
    collectionPosterRepairTotal: Int,
    onOpenCollectionMovie: (CollectedMovie) -> Unit,
    onToggleCollectionMoviePin: (CollectedMovie) -> Unit,
    onMovePinnedCollectionMovie: (CollectedMovie, CollectedMovie) -> Unit,
    onRenameCollectionMovie: (CollectedMovie, String) -> Unit,
    onRemoveCollectionMovie: (CollectedMovie) -> Unit,
    isCompressingCollectionMovie: Boolean,
    collectionCompressionMovieTitle: String,
    collectionCompressionProgress: Double,
    onRequestCollectionCompression: (CollectedMovie) -> Unit,
    onRequestBulkCollectionCompression: (CollectionVideoSizeOption) -> Unit,
    onCancelCollectionCompression: () -> Unit,
    collectionColumnCount: Int
) {
    item(
        key = "saved-project-header",
        contentType = "saved-project-header"
    ) {
        SavedProjectHeader(
            palette = palette,
            count = editableProjectSummaries.size
        )
    }
    val projectCells = buildList<DraftProjectSummary?> {
        addAll(editableProjectSummaries.take(HomeSavedMovieSlotCount))
        repeat((2 - size).coerceAtLeast(0)) { add(null) }
    }
    itemsIndexed(
        items = projectCells.chunked(standardProjectColumnCount),
        key = { rowIndex, row ->
            "saved-project-row:$rowIndex:" + row.joinToString("|") { it?.projectId ?: "empty" }
        },
        contentType = { _, _ -> "saved-project-row" }
    ) { rowIndex, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEachIndexed { index, project ->
                Box(Modifier.weight(1f)) {
                    if (project == null) {
                        key("empty-$rowIndex-$index") {
                            EmptyStandardMovieRow(palette)
                        }
                    } else {
                        key(project.projectId) {
                            DraftProjectRow(
                                palette = palette,
                                summary = project,
                                onClick = { onOpenEditableProject(project) },
                                onRemove = { onRemoveEditableProject(project) },
                                onTogglePin = { onToggleEditableProjectPin(project) },
                                onEditMemo = { onEditEditableProjectMemo(project) }
                            )
                        }
                    }
                }
            }
        }
    }
    movieCollectionItems(
        palette = palette,
        movies = collectionMovies,
        isImporting = isImportingCollection,
        importCompletedCount = collectionImportCompleted,
        importTotalCount = collectionImportTotal,
        onImport = onImportCollection,
        onCancelImport = onCancelCollectionImport,
        posterRepairCompleted = collectionPosterRepairCompleted,
        posterRepairTotal = collectionPosterRepairTotal,
        onOpen = onOpenCollectionMovie,
        onTogglePin = onToggleCollectionMoviePin,
        onMovePinned = onMovePinnedCollectionMovie,
        onRename = onRenameCollectionMovie,
        onRemove = onRemoveCollectionMovie,
        isCompressing = isCompressingCollectionMovie,
        compressionMovieTitle = collectionCompressionMovieTitle,
        compressionProgress = collectionCompressionProgress,
        onRequestCompression = onRequestCollectionCompression,
        onRequestBulkCompression = onRequestBulkCollectionCompression,
        onCancelCompression = onCancelCollectionCompression,
        columnCount = collectionColumnCount
    )
}

@Composable
private fun SavedProjectHeader(
    palette: HanClipPalette,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            "$count/$HomeSavedMovieSlotCount",
            color = palette.subText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Surface(
            modifier = Modifier.size(22.dp),
            shape = RoundedCornerShape(6.dp),
            color = palette.secondary.copy(alpha = 0.10f)
        ) {
            Icon(
                Icons.Outlined.VideoLibrary,
                contentDescription = null,
                tint = palette.primary.copy(alpha = 0.72f),
                modifier = Modifier.padding(5.dp)
            )
        }
        Text(
            "영화 목록",
            color = palette.text.copy(alpha = 0.76f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AiShotMovieGrid(
    palette: HanClipPalette,
    summaries: List<ExportedMovieSummary>,
    recentlySavedMovieUriString: String?,
    onOpenExportedMovie: (ExportedMovieSummary) -> Unit,
    onRemoveExportedMovie: (ExportedMovieSummary) -> Unit,
    onToggleExportedMoviePin: (ExportedMovieSummary) -> Unit,
    onEditExportedMovieMemo: (ExportedMovieSummary) -> Unit
) {
    val cells = summaries.take(2).map { it as ExportedMovieSummary? } +
        List((2 - summaries.size).coerceAtLeast(0)) { null }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cells.forEach { summary ->
            if (summary == null) {
                EmptyAiShotMovieCard(palette, Modifier.weight(1f))
            } else {
                AiShotMovieCard(
                    summary = summary,
                    isRecentlySaved = summary.uriString == recentlySavedMovieUriString,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenExportedMovie(summary) },
                    onRemove = { onRemoveExportedMovie(summary) },
                    onTogglePin = { onToggleExportedMoviePin(summary) },
                    onEditMemo = { onEditExportedMovieMemo(summary) }
                )
            }
        }
    }
}

@Composable
private fun AiShotProjectGrid(
    palette: HanClipPalette,
    summaries: List<DraftProjectSummary>,
    onOpenProject: (DraftProjectSummary) -> Unit,
    onRemoveProject: (DraftProjectSummary) -> Unit,
    onTogglePin: (DraftProjectSummary) -> Unit,
    onEditMemo: (DraftProjectSummary) -> Unit
) {
    val cells = summaries.take(2).map { it as DraftProjectSummary? } +
        List((2 - summaries.size).coerceAtLeast(0)) { null }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.forEach { summary ->
            if (summary == null) {
                EmptyAiShotMovieCard(palette, Modifier.weight(1f))
            } else {
                AiShotProjectCard(
                    palette = palette,
                    summary = summary,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenProject(summary) },
                    onRemove = { onRemoveProject(summary) },
                    onTogglePin = { onTogglePin(summary) },
                    onEditMemo = { onEditMemo(summary) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AiShotProjectCard(
    palette: HanClipPalette,
    summary: DraftProjectSummary,
    modifier: Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onTogglePin: () -> Unit,
    onEditMemo: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .height(76.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showActions = true },
                onLongClickLabel = "AiShot 프로젝트 작업 열기"
            ),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditableProjectThumbnail(summary, Modifier.size(58.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        homeAiShotDateText(summary.savedAtMillis),
                        color = palette.text.copy(alpha = 0.88f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    EditableProjectDetail(
                        summary = summary,
                        includeByteCount = false,
                        palette = palette
                    )
                    EditableProjectThumbnailStrip(summary, maxFrames = 2, frameWidth = 24, frameHeight = 20)
                }
            }
            DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                DropdownMenuItem(
                    text = { Text(if (summary.memo.isBlank()) "메모 추가" else "메모 편집") },
                    onClick = { showActions = false; onEditMemo() },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(if (summary.isPinned) "핀 해제" else "핀 고정") },
                    onClick = { showActions = false; onTogglePin() },
                    leadingIcon = { Icon(Icons.Outlined.PushPin, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("프로젝트 삭제") },
                    onClick = { showActions = false; onRemove() },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AiShotMovieCard(
    summary: ExportedMovieSummary,
    isRecentlySaved: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onTogglePin: () -> Unit,
    onEditMemo: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .height(76.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showActions = true },
                onLongClickLabel = "AiShot 완성본 작업 열기"
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (isRecentlySaved) 1.5.dp else 1.dp,
            color = if (isRecentlySaved) HomePrimary.copy(alpha = 0.52f) else HomeBorder
        )
    ) {
        Box {
            Row(
                modifier = Modifier.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportedMovieThumbnail(
                    summary = summary,
                    displayLongEdgeDp = 58,
                    modifier = Modifier.size(58.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        homeAiShotDateText(summary.updatedAtMillis),
                        modifier = Modifier.weight(1f, fill = false),
                        color = HomeText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isRecentlySaved) {
                        NewSavedMovieBadge()
                    }
                    if (summary.isPinned) {
                        PinnedSavedMovieBadge()
                    }
                }
                Text(
                    compactSavedMovieDetailText(summary, includeByteCount = false),
                    color = HomeSubText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ExportedMovieThumbnailStrip(
                    summary = summary,
                    maxFrames = 2,
                    frameWidth = 24,
                    frameHeight = 20,
                    rowHeight = 20
                )
                }
            }
            DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                DropdownMenuItem(
                    text = { Text(if (summary.memo.isBlank()) "메모 추가" else "메모 편집") },
                    onClick = { showActions = false; onEditMemo() },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(if (summary.isPinned) "핀 해제" else "핀 고정") },
                    onClick = { showActions = false; onTogglePin() },
                    leadingIcon = { Icon(Icons.Outlined.PushPin, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("목록에서 제거") },
                    onClick = { showActions = false; onRemove() },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun EmptyAiShotMovieCard(
    palette: HanClipPalette,
    modifier: Modifier = Modifier
) {
    val placeholder = palette.secondary.copy(alpha = 0.12f)
    Surface(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel.copy(alpha = palette.panel.alpha * 0.72f),
        border = BorderStroke(
            1.dp,
            palette.border.copy(alpha = palette.border.alpha * 0.68f)
        )
    ) {
        Row(
            modifier = Modifier.padding(9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(placeholder)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(placeholder)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.secondary.copy(alpha = 0.086f))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 20.dp, height = 18.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.secondary.copy(alpha = 0.078f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniAiShotAction(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(26.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEAF5F0)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun SavedProjectCategoryHeader(
    title: String,
    count: Int,
    icon: ImageVector?,
    palette: HanClipPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.background(
                    Brush.linearGradient(listOf(palette.primary, palette.secondary))
                ),
                contentAlignment = Alignment.Center
            ) {
                if (title == "AiShot") {
                    Image(
                        painter = painterResource(R.drawable.aishot_icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.padding(5.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
        Text(
            text = title,
            color = palette.text.copy(alpha = 0.88f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(palette.secondary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = palette.secondary,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraftProjectRow(
    palette: HanClipPalette,
    summary: DraftProjectSummary,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onTogglePin: () -> Unit,
    onEditMemo: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRemove,
                onLongClickLabel = "프로젝트 삭제 확인"
            ),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            EditableProjectThumbnail(summary, Modifier.size(56.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    homeProjectDateText(summary.savedAtMillis),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                EditableProjectDetail(
                    summary = summary,
                    includeByteCount = true,
                    palette = palette
                )
                EditableProjectThumbnailStrip(summary, maxFrames = 8, frameWidth = 18, frameHeight = 18)
            }
            if (summary.preset == MoviePreset.AiShot) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Brush.linearGradient(listOf(palette.primary, palette.secondary))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.aishot_icon),
                                contentDescription = "AiShot 계속",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            } else {
                CompactSavedMovieIconButton(
                    onClick = onEditMemo
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = if (summary.memo.isBlank()) "메모 추가" else "메모 편집",
                        tint = palette.subText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                CompactSavedMovieIconButton(
                    onClick = onTogglePin
                ) {
                    Icon(
                        Icons.Outlined.PushPin,
                        contentDescription = if (summary.isPinned) "핀 해제" else "핀 고정",
                        tint = if (summary.isPinned) palette.primary else palette.subText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableProjectThumbnail(
    summary: DraftProjectSummary,
    modifier: Modifier = Modifier.size(54.dp)
) {
    val cacheKey = remember(summary.projectId, summary.savedAtMillis, summary.thumbnailUriString) {
        "editable|${summary.projectId}|${summary.savedAtMillis}|${summary.thumbnailUriString}"
    }
    var bitmap by remember(cacheKey) { mutableStateOf(HomeMovieFrameCache.thumbnail(cacheKey)) }
    LaunchedEffect(cacheKey) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            summary.thumbnailUriString
                ?.let(Uri::parse)
                ?.path
                ?.let(BitmapFactory::decodeFile)
                ?.scaledDownToLongEdge(160)
        }.also { HomeMovieFrameCache.putThumbnail(cacheKey, it) }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Icon(
            imageVector = Icons.Outlined.Movie,
            contentDescription = null,
            tint = HomePrimary
        )
    }
}

@Composable
private fun EditableProjectThumbnailStrip(
    summary: DraftProjectSummary,
    maxFrames: Int,
    frameWidth: Int,
    frameHeight: Int
) {
    val frameUris = summary.thumbnailUriStrings.drop(1).take(maxFrames)
    if (frameUris.isEmpty()) return
    Row(
        modifier = Modifier.height(frameHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        frameUris.forEach { uriString ->
            EditableProjectFrame(uriString, Modifier.size(frameWidth.dp, frameHeight.dp))
        }
        if (summary.clipCount > frameUris.size + 1) {
            Text(
                "·",
                color = HomeSubText,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EditableProjectFrame(uriString: String, modifier: Modifier) {
    val cacheKey = remember(uriString) { "editable-frame|$uriString" }
    var bitmap by remember(cacheKey) { mutableStateOf(HomeMovieFrameCache.thumbnail(cacheKey)) }
    LaunchedEffect(cacheKey) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            Uri.parse(uriString).path?.let(BitmapFactory::decodeFile)?.scaledDownToLongEdge(96)
        }.also { HomeMovieFrameCache.putThumbnail(cacheKey, it) }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HomePrimary.copy(alpha = 0.10f))
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun DraftInfoPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        border = BorderStroke(1.dp, HomeBorder)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = HomePrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun draftSummaryText(summary: DraftProjectSummary): String {
    return "${homeProjectDateText(summary.savedAtMillis)} 저장 · 완성본 만들기 전 상태 보관 · 사진/영상, 순서, 자막, 음악 · ${summary.outputText}"
}

@Composable
private fun EditableProjectDetail(
    summary: DraftProjectSummary,
    includeByteCount: Boolean,
    palette: HanClipPalette
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "클립 ${summary.clipCount}개 ·",
            color = palette.subText,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
        Icon(
            summary.preset.homePresetIcon(),
            contentDescription = summary.preset.title,
            tint = palette.primary.copy(alpha = 0.78f),
            modifier = Modifier.size(12.dp)
        )
        Text(
            buildString {
                append(movieDurationText(summary.totalDurationSeconds))
                if (includeByteCount && summary.displayByteCount > 0L) {
                    append(" · ")
                    append(fileSizeText(summary.displayByteCount))
                }
            },
            color = palette.subText,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun MoviePreset.homePresetIcon(): ImageVector = when (this) {
    MoviePreset.NewMovie -> Icons.Outlined.VideoLibrary
    MoviePreset.Quick -> Icons.Outlined.Bolt
    MoviePreset.AiShot -> Icons.Outlined.AddPhotoAlternate
    MoviePreset.Travel -> Icons.Outlined.Flight
    MoviePreset.Life -> Icons.Outlined.Favorite
    MoviePreset.Golf -> Icons.Outlined.SportsGolf
}

@Composable
private fun EmptySavedProjectRow() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HomeBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = HomeSubText
            )
            Column(Modifier.weight(1f)) {
                Text("최근 항목 없음", fontWeight = FontWeight.SemiBold, color = HomeText)
                Text(
                    "사진과 영상을 골라 완성본을 만들면 HanClip 앨범 저장 이력이 여기에 표시됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HomeSubText
                )
            }
        }
    }
}

@Composable
private fun EmptyStandardMovieRow(palette: HanClipPalette) {
    val placeholder = palette.secondary.copy(alpha = 0.085f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel.copy(alpha = palette.panel.alpha * 0.72f),
        border = BorderStroke(
            1.dp,
            palette.border.copy(alpha = palette.border.alpha * 0.68f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(placeholder)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 112.dp, height = 13.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(placeholder)
                )
                Box(
                    modifier = Modifier
                        .size(width = 154.dp, height = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.secondary.copy(alpha = 0.066f))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 18.dp, height = 18.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.secondary.copy(alpha = 0.058f))
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = palette.subText.copy(alpha = palette.subText.alpha * 0.42f),
                modifier = Modifier.size(22.dp)
            )
            Icon(
                imageVector = Icons.Outlined.PushPin,
                contentDescription = null,
                tint = palette.subText.copy(alpha = palette.subText.alpha * 0.38f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedProjectRow(
    palette: HanClipPalette,
    summary: ExportedMovieSummary,
    isRecentlySaved: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onTogglePin: () -> Unit,
    onEditMemo: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRemove,
                onLongClickLabel = "완성본 삭제 확인"
            ),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(
            width = if (isRecentlySaved) 1.5.dp else 1.dp,
            color = if (isRecentlySaved) palette.primary.copy(alpha = 0.52f) else palette.border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExportedMovieThumbnail(
                summary = summary,
                displayLongEdgeDp = 64,
                modifier = Modifier.size(64.dp)
            )
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        homeProjectDateText(summary.updatedAtMillis),
                        modifier = Modifier.weight(1f, fill = false),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.text.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isRecentlySaved) {
                        NewSavedMovieBadge()
                    }
                    if (summary.isPinned) {
                        PinnedSavedMovieBadge()
                    }
                }
                Text(
                    compactSavedMovieDetailText(summary, includeByteCount = true),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.subText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ExportedMovieThumbnailStrip(
                    summary = summary,
                    maxFrames = 8,
                    frameWidth = 18,
                    frameHeight = 18,
                    rowHeight = 19
                )
                if (summary.memo.isNotBlank()) {
                    Text(
                        summary.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (isRecentlySaved) {
                    Text(
                        "방금 만든 완성본 · HanClip 앨범 저장 · 탭해서 시사회 열기",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CompactSavedMovieIconButton(onClick = onEditMemo) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = if (summary.memo.isBlank()) "메모 추가" else "메모 편집",
                    tint = if (summary.memo.isBlank()) palette.subText else palette.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
            CompactSavedMovieIconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = if (summary.isPinned) "핀 해제" else "핀 고정",
                    tint = if (summary.isPinned) palette.primary else palette.subText,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactSavedMovieIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        icon()
    }
}

@Composable
private fun NewSavedMovieBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFEB3B45).copy(alpha = 0.13f),
        border = BorderStroke(1.dp, Color(0xFFEB3B45).copy(alpha = 0.32f))
    ) {
        Text(
            text = "방금 완성",
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            color = Color(0xFFC8212C),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun PinnedSavedMovieBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = HomePrimary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, HomePrimary.copy(alpha = 0.28f))
    ) {
        Text(
            text = "고정",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            color = HomePrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun ExportedMovieThumbnail(
    summary: ExportedMovieSummary,
    displayLongEdgeDp: Int = 88,
    modifier: Modifier = Modifier.size(width = 88.dp, height = 54.dp)
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val targetLongEdgePx = remember(density.density, displayLongEdgeDp) {
        with(density) { displayLongEdgeDp.dp.roundToPx() }
            .times(HomeFrameDecodeScale)
            .coerceIn(HomeThumbnailMinLongEdgePx, HomeThumbnailMaxLongEdgePx)
    }
    val cacheKey = remember(summary.uriString, summary.updatedAtMillis, targetLongEdgePx) {
        "thumbnail|${summary.uriString}|${summary.updatedAtMillis}|$targetLongEdgePx"
    }
    var bitmap by remember(cacheKey) {
        mutableStateOf(HomeMovieFrameCache.thumbnail(cacheKey))
    }
    LaunchedEffect(cacheKey) {
        HomeMovieFrameCache.thumbnail(cacheKey)?.let { cached ->
            bitmap = cached
            return@LaunchedEffect
        }
        delay(HomeFrameLoadDelayMillis)
        HomeMovieFrameCache.thumbnail(cacheKey)?.let { cached ->
            bitmap = cached
            return@LaunchedEffect
        }
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, Uri.parse(summary.uriString))
                    retriever.loadScaledFrameAtTime(
                        positionUs = 0L,
                        targetLongEdgePx = targetLongEdgePx
                    )
                } finally {
                    retriever.release()
                }
            }.getOrNull()
        }.also { loaded ->
            HomeMovieFrameCache.putThumbnail(cacheKey, loaded)
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFEAF5F0)),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = null,
                tint = HomePrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "HanClip",
                color = HomePrimary,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ExportedMovieThumbnailStrip(
    summary: ExportedMovieSummary,
    maxFrames: Int = 4,
    frameWidth: Int = 20,
    frameHeight: Int = 20,
    rowHeight: Int = 22
) {
    if (summary.clipCount <= 1) return
    val context = LocalContext.current
    val density = LocalDensity.current
    val targetLongEdgePx = remember(density.density, frameWidth, frameHeight) {
        with(density) { max(frameWidth, frameHeight).dp.roundToPx() }
            .times(HomeFrameDecodeScale)
            .coerceIn(HomeStripMinLongEdgePx, HomeStripMaxLongEdgePx)
    }
    val cacheKey = remember(
        summary.uriString,
        summary.updatedAtMillis,
        summary.clipCount,
        summary.totalDurationSeconds,
        maxFrames,
        targetLongEdgePx
    ) {
        "strip|${summary.uriString}|${summary.updatedAtMillis}|${summary.clipCount}|" +
            "${summary.totalDurationSeconds}|$maxFrames|$targetLongEdgePx"
    }
    var bitmaps by remember(cacheKey) {
        mutableStateOf(HomeMovieFrameCache.strip(cacheKey).orEmpty())
    }
    LaunchedEffect(cacheKey) {
        HomeMovieFrameCache.strip(cacheKey)?.let { cached ->
            bitmaps = cached
            return@LaunchedEffect
        }
        delay(HomeFrameLoadDelayMillis)
        HomeMovieFrameCache.strip(cacheKey)?.let { cached ->
            bitmaps = cached
            return@LaunchedEffect
        }
        bitmaps = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, Uri.parse(summary.uriString))
                    val count = summary.clipCount.coerceAtMost(maxFrames)
                    val safeDurationUs = (summary.totalDurationSeconds.coerceAtLeast(0.1) * 1_000_000L).toLong()
                    List(count) { index ->
                        val positionUs = (((index + 0.5) / count) * safeDurationUs).toLong()
                        retriever.loadScaledFrameAtTime(
                            positionUs = positionUs,
                            targetLongEdgePx = targetLongEdgePx
                        )
                    }.filterNotNull()
                } finally {
                    retriever.release()
                }
            }.getOrDefault(emptyList())
        }.also { loaded ->
            HomeMovieFrameCache.putStrip(cacheKey, loaded)
        }
    }
    if (bitmaps.isEmpty()) return
    Row(
        modifier = Modifier.height(rowHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bitmaps.forEach { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(width = frameWidth.dp, height = frameHeight.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
        if (summary.clipCount > bitmaps.size) {
            Text(
                "....",
                color = HomeSubText.copy(alpha = 0.62f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

private fun homeProjectDateText(updatedAtMillis: Long): String {
    if (updatedAtMillis <= 0L) return "방금"
    return SimpleDateFormat("M월 d일 a h:mm", Locale.KOREAN).format(Date(updatedAtMillis))
}

private fun homeAiShotDateText(updatedAtMillis: Long): String {
    if (updatedAtMillis <= 0L) return "방금"
    return SimpleDateFormat("M/d a h:mm", Locale.KOREAN).format(Date(updatedAtMillis))
}

private fun compactSavedMovieDetailText(
    summary: ExportedMovieSummary,
    includeByteCount: Boolean
): String {
    return buildList {
        add("클립 ${summary.clipCount}개")
        add(movieDurationText(summary.totalDurationSeconds))
        if (includeByteCount && summary.byteCount > 0L) {
            add(fileSizeText(summary.byteCount))
        }
    }.joinToString(" · ")
}

private fun fileSizeText(byteCount: Long): String {
    val megabytes = byteCount.coerceAtLeast(0L) / (1024.0 * 1024.0)
    return when {
        megabytes < 0.1 -> "${(byteCount / 1024.0).coerceAtLeast(0.0).roundToInt()} KB"
        megabytes < 10.0 -> "%.1f MB".format(megabytes)
        else -> "%.0f MB".format(megabytes)
    }
}

private fun savedMovieDetailText(summary: ExportedMovieSummary): String {
    val parts = buildList {
        hanClipCompletionTitle(summary.title)
            .takeIf { it.isNotBlank() }
            ?.let(::add)
        add("${summary.clipCount}개")
        add(movieDurationText(summary.totalDurationSeconds))
        summary.outputAspectRatio?.let { add(it.title) }
        summary.outputQualityPreset?.let { add(it.chipTitle) }
        add("HanClip MP4")
        if (summary.hasBackgroundMusic == true) add("음악")
        overlayDetailParts(summary).forEach(::add)
    }
    return parts.joinToString(" · ")
}

private fun overlayDetailParts(summary: ExportedMovieSummary): List<String> {
    val hasText = summary.hasTextOverlay
    val hasLogo = summary.hasLogoOverlay
    return when {
        hasText == true && hasLogo == true -> listOf("자막", "로고")
        hasText == true -> listOf("자막")
        hasLogo == true -> listOf("로고")
        hasText == null && hasLogo == null && summary.hasWatermark == true -> listOf("자막/로고")
        else -> emptyList()
    }
}

private fun movieDurationText(durationSeconds: Double): String {
    val totalSeconds = durationSeconds.coerceAtLeast(0.0)
    val minutes = (totalSeconds / 60).toInt()
    val seconds = totalSeconds - minutes * 60
    return if (minutes > 0) {
        "%d분 %.1f초".format(minutes, seconds)
    } else {
        "%.1f초".format(seconds)
    }
}

private const val CollectionCxFileExplorerPackage = "com.cxinventor.file.explorer"

private fun collectionVideoFileIntent(context: Context): Intent {
    val videoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
    return Intent.createChooser(videoIntent, "동영상 앱 또는 파일 선택").apply {
        val cxIntent = Intent(videoIntent).setPackage(CollectionCxFileExplorerPackage)
        if (cxIntent.resolveActivity(context.packageManager) != null) {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cxIntent))
        }
    }
}
