package com.hanclip.android.feature.editor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.MotionPhotosOn
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hanclip.android.core.media.MediaImportReader
import com.hanclip.android.core.media.MediaSelectionContract
import com.hanclip.android.R
import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.ClipMediaKind
import com.hanclip.android.core.model.MoviePreset
import com.hanclip.android.core.model.OutputAspectRatio
import com.hanclip.android.core.model.OutputQualityPreset
import com.hanclip.android.core.model.VideoSegmentMode
import com.hanclip.android.core.model.WatermarkPosition
import com.hanclip.android.core.model.WatermarkSettings
import com.hanclip.android.core.safety.steppedDefaultDuration
import com.hanclip.android.core.settings.SleepPreventionMode
import com.hanclip.android.core.theme.HanClipPalette
import com.hanclip.android.core.theme.HanClipThemeStore
import com.hanclip.android.core.theme.HanClipSystemBars
import com.hanclip.android.core.theme.currentPalette
import com.hanclip.android.feature.home.HanClipBrandCapsule
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class MusicSettingsSnapshot(
    val uri: Uri?,
    val title: String?,
    val sampleId: String?,
    val enabled: Boolean,
    val musicVolume: Double,
    val originalAudioVolume: Double,
    val loopsToFillVideo: Boolean,
    val fadeInEnabled: Boolean,
    val fadeOutEnabled: Boolean
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditorRoute(
    preset: MoviePreset,
    onBackHome: () -> Unit,
    onPreview: () -> Unit,
    onOpenAiShot: () -> Unit = {},
    onOpenBrowser: () -> Unit = {},
    sleepPreventionMode: SleepPreventionMode = SleepPreventionMode.Default,
    onSleepPreventionModeChange: (SleepPreventionMode) -> Unit = {},
    initialImportAction: EditorImportAction? = null,
    onInitialImportActionConsumed: () -> Unit = {},
    viewModel: EditorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val editorColumnCount = if (LocalConfiguration.current.screenWidthDp >= 600) 2 else 1
    val palette = HanClipThemeStore.load(context).currentPalette
    HanClipSystemBars(palette.solidPanel)
    DisposableEffect(context) {
        val activity = context.findEditorActivity()
        val isPhone = context.resources.configuration.smallestScreenWidthDp < 600
        val previousOrientation = activity?.requestedOrientation
        if (activity != null && isPhone) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        onDispose {
            if (activity != null && isPhone && previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }
    var trimmingClipID by rememberSaveable { mutableStateOf<String?>(null) }
    var previewClipID by rememberSaveable { mutableStateOf<String?>(null) }
    var isTextOverlaySheetVisible by rememberSaveable { mutableStateOf(false) }
    var isEndingInfoSettingsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isMusicSettingsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var musicSettingsSnapshot by remember { mutableStateOf<MusicSettingsSnapshot?>(null) }
    var isCalendarPickerVisible by rememberSaveable { mutableStateOf(false) }
    var mediaPickerTitle by rememberSaveable { mutableStateOf("날짜별") }
    var isReorderMode by rememberSaveable { mutableStateOf(false) }
    var isAdvancedSettingsExpanded by rememberSaveable { mutableStateOf(false) }
    var isClipSettingsExpanded by rememberSaveable(state.activeProjectId) {
        mutableStateOf(DefaultClipSettingsExpanded)
    }
    var isImportMenuVisible by remember { mutableStateOf(false) }
    var isResetConfirmationVisible by remember { mutableStateOf(false) }
    var pendingDeleteClipID by remember { mutableStateOf<String?>(null) }
    var isExitConfirmationVisible by remember { mutableStateOf(false) }
    var isQuickDurationVisible by rememberSaveable { mutableStateOf(false) }
    var reopenQuickAfterPicker by rememberSaveable { mutableStateOf(false) }
    var reopenQuickAfterSettings by rememberSaveable { mutableStateOf(false) }
    var showPermissionSettingsAction by remember { mutableStateOf(false) }
    var resumeCalendarAfterSettings by rememberSaveable { mutableStateOf(false) }
    var resumeMusicAfterBrowser by rememberSaveable { mutableStateOf(false) }
    var quickDurationShownProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var quickTargetDurationSeconds by rememberSaveable { mutableStateOf(1.0) }
    var pendingExportAfterNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var autoAdvancePreviewOnOpen by rememberSaveable { mutableStateOf(false) }
    var clipPreviewAutoAdvanceEnabled by rememberSaveable { mutableStateOf(false) }
    val trimmingClip = state.clips.firstOrNull { it.id == trimmingClipID }
    val previewClip = state.clips.firstOrNull { it.id == previewClipID }
    val pendingDeleteClip = state.clips.firstOrNull { it.id == pendingDeleteClipID }
    val previewClips = state.renderableClips
    val previewClipIndex = previewClips.indexOfFirst { it.id == previewClipID }
    val trimmingClipIndex = previewClips.indexOfFirst { it.id == trimmingClipID }
    fun openClipFromTrim(target: ClipItem, autoAdvance: Boolean = false) {
        autoAdvancePreviewOnOpen = autoAdvance
        if (
            target.mediaKind == ClipMediaKind.Video ||
            (target.mediaKind == ClipMediaKind.LivePhoto &&
                target.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion)
        ) {
            previewClipID = null
            trimmingClipID = target.id
        } else {
            trimmingClipID = null
            previewClipID = target.id
        }
    }
    fun openMusicSettings() {
        if (musicSettingsSnapshot == null) {
            musicSettingsSnapshot = MusicSettingsSnapshot(
                uri = state.backgroundMusicUri,
                title = state.backgroundMusicTitle,
                sampleId = state.backgroundMusicSampleId,
                enabled = state.backgroundMusicEnabled,
                musicVolume = state.backgroundMusicVolume,
                originalAudioVolume = state.originalAudioVolume,
                loopsToFillVideo = state.backgroundMusicLoopsToFillVideo,
                fadeInEnabled = state.backgroundMusicFadeInEnabled,
                fadeOutEnabled = state.backgroundMusicFadeOutEnabled
            )
            if (!state.backgroundMusicEnabled &&
                (state.backgroundMusicUri != null || state.backgroundMusicSampleId != null)
            ) {
                viewModel.updateBackgroundMusicEnabled(true)
            }
        }
        isMusicSettingsSheetVisible = true
    }
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.addPickedMedia(
                context,
                MediaSelectionContract.fromResultIntent(context, result.data).uris
            )
        } else {
            viewModel.showAlert("미디어 선택을 취소했습니다. 기본 사진첩이나 다른 앱·파일에서 다시 가져올 수 있습니다.")
        }
        if (reopenQuickAfterPicker) {
            reopenQuickAfterPicker = false
            isQuickDurationVisible = true
        }
    }
    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.setBackgroundMusic(
                context,
                MediaSelectionContract.fromResultIntent(context, result.data).uris.firstOrNull()
            )
        } else {
            viewModel.showAlert("음악 선택을 취소했습니다. 음악 설정에서 다시 선택하거나 샘플 음악을 사용할 수 있습니다.")
        }
        if (musicSettingsSnapshot != null) {
            isMusicSettingsSheetVisible = true
        } else if (reopenQuickAfterSettings) {
            reopenQuickAfterSettings = false
            isQuickDurationVisible = true
        }
    }
    fun startSavedMovieExport() {
        saveBeforePreviewExport(
            save = { viewModel.saveEditingSession(context) },
            startExport = { viewModel.exportMovie(context, onPreview) },
            onSaveFailure = {
                viewModel.showAlert("영화를 저장하지 못해 만들기를 시작하지 않았습니다. 저장 공간을 확인한 뒤 다시 시도해 주세요.")
            }
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (pendingExportAfterNotificationPermission) {
            pendingExportAfterNotificationPermission = false
            startSavedMovieExport()
        }
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (context.hasFullGalleryAccess()) {
            showPermissionSettingsAction = false
            isCalendarPickerVisible = true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            grants[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
        ) {
            showPermissionSettingsAction = true
            viewModel.showAlert("선택한 사진/영상만 허용되어 날짜별 기본 사진첩을 열 수 없습니다. Android 설정 > HanClip 권한에서 사진 및 동영상을 모두 허용하거나, 파일 선택으로 직접 가져와 주세요.")
        } else {
            showPermissionSettingsAction = true
            viewModel.showAlert("Android 기본 사진첩을 보려면 사진 및 동영상 권한이 필요합니다. Android 설정 > HanClip 권한에서 허용하거나, 파일 선택으로 직접 가져와 주세요.")
        }
        if (reopenQuickAfterPicker && !context.hasFullGalleryAccess()) {
            reopenQuickAfterPicker = false
            isQuickDurationVisible = true
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (resumeCalendarAfterSettings) {
                    resumeCalendarAfterSettings = false
                    if (context.hasFullGalleryAccess()) {
                        showPermissionSettingsAction = false
                        isCalendarPickerVisible = true
                    }
                }
                if (resumeMusicAfterBrowser) {
                    resumeMusicAfterBrowser = false
                    if (musicSettingsSnapshot != null) {
                        isMusicSettingsSheetVisible = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openCalendarPicker(title: String = "사진첩 날짜별") {
        mediaPickerTitle = title
        if (context.hasFullGalleryAccess()) {
            showPermissionSettingsAction = false
            isCalendarPickerVisible = true
            return
        }
        val missingPermissions = calendarMediaPermissions()
            .filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        if (missingPermissions.isEmpty()) {
            isCalendarPickerVisible = true
        } else {
            calendarPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    fun beginMovieExport() {
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsNotificationPermission) {
            pendingExportAfterNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startSavedMovieExport()
        }
    }

    LaunchedEffect(preset) {
        viewModel.openPreset(context, preset)
    }

    LaunchedEffect(initialImportAction) {
        when (initialImportAction) {
            EditorImportAction.Photo -> {
                onInitialImportActionConsumed()
                openCalendarPicker("기본 사진첩")
            }
            EditorImportAction.Calendar -> {
                onInitialImportActionConsumed()
                openCalendarPicker()
            }
            EditorImportAction.Files -> {
                onInitialImportActionConsumed()
                galleryPicker.launch(mediaFileIntent(context))
            }
            null -> Unit
        }
    }

    LaunchedEffect(
        state.activeProjectId,
        state.preset,
        state.clips,
        state.isImportingMedia
    ) {
        if (state.preset == MoviePreset.Quick &&
            quickContentClips(state.clips).isNotEmpty() &&
            !state.isImportingMedia &&
            viewModel.isNewEditingSession() &&
            quickDurationShownProjectId != state.activeProjectId
        ) {
            quickDurationShownProjectId = state.activeProjectId
            quickTargetDurationSeconds = quickContentClips(state.clips).size
                .coerceAtLeast(1).toDouble()
            isQuickDurationVisible = true
        }
    }

    fun requestBackHome() {
        if (state.clips.isEmpty()) {
            viewModel.discardPersistedProjectIfEmpty(context)
            onBackHome()
        } else {
            isExitConfirmationVisible = true
        }
    }

    BackHandler(enabled = state.clips.isNotEmpty()) {
        requestBackHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(editorColumnCount),
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(6.dp))
                EditorHeader(
                    palette = palette,
                    onBackHome = ::requestBackHome,
                    onAddMedia = { isImportMenuVisible = true }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProjectControls(
                    defaultDuration = state.defaultDurationSeconds,
                    preset = state.preset,
                    defaultVideoSegmentMode = state.defaultVideoSegmentMode,
                    usesFullVideoRange = state.renderableClips
                        .filter { it.mediaKind == ClipMediaKind.Video }
                        .let { videos ->
                            videos.isNotEmpty() && videos.all { clip ->
                                val sourceDuration = clip.sourceDurationSeconds ?: clip.durationSeconds
                                kotlin.math.abs(sourceDuration - clip.durationSeconds) < 0.05
                            }
                        },
                    hasLivePhotos = state.clips.any { it.mediaKind == ClipMediaKind.LivePhoto },
                    livePhotosUseMotion = state.clips
                        .filter { it.mediaKind == ClipMediaKind.LivePhoto }
                        .any { it.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion },
                    selectedRatio = state.outputAspectRatio,
                    selectedQuality = state.outputQualityPreset,
                    palette = palette,
                    hasTextOverlay = state.watermarkSettings.isEnabled,
                    captionText = state.watermarkSettings.text,
                    captionAppearance = buildCaptionAppearanceSummary(state.watermarkSettings.fontSize.title, state.watermarkSettings.position, state.watermarkSettings.shadowEnabled),
                    hasLogoOverlay = state.watermarkSettings.logoEnabled,
                    hasMusic = state.backgroundMusicEnabled &&
                        (state.backgroundMusicUri != null || state.backgroundMusicSampleId != null),
                    hasEnding = state.watermarkSettings.includesEndingInfoCard,
                    endingDuration = state.watermarkSettings.normalizedEndingInfoCardDuration,
                    endingThemeTitle = state.watermarkSettings.endingInfoCardTheme.title,
                    musicTitle = state.backgroundMusicTitle,
                    musicVolume = state.backgroundMusicVolume,
                    originalAudioVolume = state.originalAudioVolume,
                    hasSimilarPhotoGroups = state.clips.any { it.isSimilarPhotoGroupParent },
                    similarPhotoRepresentativeInterval = state.similarPhotoRepresentativeInterval,
                    similarPhotoGroupMode = state.clips
                        .filter { it.isSimilarPhotoGroupParent }
                        .map { it.videoSegmentMode }
                        .distinct()
                        .singleOrNull() ?: VideoSegmentMode.Single,
                    isReorderMode = isReorderMode,
                    isAdvancedSettingsExpanded = isAdvancedSettingsExpanded,
                    isClipSettingsExpanded = isClipSettingsExpanded,
                    sleepPreventionMode = sleepPreventionMode,
                    hasClips = state.clips.isNotEmpty(),
                    onSelectRatio = { ratio -> viewModel.selectAspectRatio(context, ratio) },
                    onSelectQuality = { quality -> viewModel.selectOutputQualityPreset(context, quality) },
                    onSetDuration = { seconds -> viewModel.setDefaultDuration(context, seconds) },
                    onApplyDuration = viewModel::applyDefaultDurationToAll,
                    onUseSelectedVideoRanges = viewModel::selectDefaultRangeForAllVideoClips,
                    onUseFullVideoRanges = viewModel::selectFullRangeForAllVideoClips,
                    onSetVideoSegmentMode = viewModel::setVideoSegmentModeForAll,
                    onSetLivePhotoMotion = viewModel::setLivePhotoMotionForAll,
                    onOpenTextOverlay = {
                        isTextOverlaySheetVisible = true
                    },
                    onToggleTextOverlay = { enabled ->
                        viewModel.updateWatermarkSilently(state.watermarkSettings.copy(isEnabled = enabled))
                    },
                    onOpenMusicSettings = ::openMusicSettings,
                    onToggleMusic = { enabled ->
                        if (enabled && state.backgroundMusicUri == null && state.backgroundMusicSampleId == null) {
                            openMusicSettings()
                        } else {
                            viewModel.updateBackgroundMusicEnabled(enabled)
                        }
                    },
                    onToggleEnding = { enabled ->
                        viewModel.updateWatermarkSilently(
                            state.watermarkSettings.copy(includesEndingInfoCard = enabled)
                        )
                    },
                    onDecreaseEndingDuration = {
                        viewModel.updateWatermarkSilently(
                            state.watermarkSettings.copy(
                                endingInfoCardDuration = state.watermarkSettings.normalizedEndingInfoCardDuration - 0.5
                            )
                        )
                    },
                    onIncreaseEndingDuration = {
                        viewModel.updateWatermarkSilently(
                            state.watermarkSettings.copy(
                                endingInfoCardDuration = state.watermarkSettings.normalizedEndingInfoCardDuration + 0.5
                            )
                        )
                    },
                    onOpenEndingSettings = { isEndingInfoSettingsSheetVisible = true },
                    onSetSimilarPhotoInterval = { value ->
                        viewModel.setSimilarPhotoRepresentativeInterval(context, value)
                    },
                    onSetSimilarPhotoMode = viewModel::applySimilarPhotoGroupModeToAll,
                    onToggleReorder = { isReorderMode = !isReorderMode },
                    onToggleAdvancedSettings = {
                        isAdvancedSettingsExpanded = !isAdvancedSettingsExpanded
                    },
                    onToggleClipSettings = {
                        isClipSettingsExpanded = !isClipSettingsExpanded
                    },
                    onOpenQuickDuration = {
                        val endingDuration = if (state.watermarkSettings.includesEndingInfoCard) {
                            state.watermarkSettings.normalizedEndingInfoCardDuration
                        } else {
                            0.0
                        }
                        quickTargetDurationSeconds =
                            (state.totalDurationSeconds - endingDuration).coerceAtLeast(0.1)
                        isQuickDurationVisible = true
                    },
                    onCycleSleepPrevention = {
                        onSleepPreventionModeChange(sleepPreventionMode.next())
                    },
                    onResetProject = { isResetConfirmationVisible = true }
                )
            }
            if (isReorderMode && state.clips.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ReorderStrip(
                        clips = state.visibleClips,
                        palette = palette,
                        onMoveUp = { id -> viewModel.moveClipUp(id) },
                        onMoveDown = { id -> viewModel.moveClipDown(id) },
                        onDelete = { id -> pendingDeleteClipID = id },
                        onDone = { isReorderMode = false }
                    )
                }
            }
            if (state.clips.any { it.isVideoSegmentParent }) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AutoSegmentStatusPanel(
                        sourceCount = state.clips.count { it.isVideoSegmentParent },
                        segmentCount = state.clips.count { it.isVideoSegmentChild },
                        defaultDuration = state.defaultDurationSeconds,
                        palette = palette
                    )
                }
            }
            if (state.clips.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "클립 ${state.renderableClips.size}개",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.text
                        )
                        AssistChip(
                            onClick = { isReorderMode = !isReorderMode },
                            leadingIcon = { Icon(Icons.Outlined.DragIndicator, contentDescription = null) },
                            label = {
                                Text(
                                    if (isReorderMode) "완료" else "순서 변경",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = clearAssistChipColors(isReorderMode, palette),
                            border = BorderStroke(1.dp, if (isReorderMode) palette.primary else palette.border)
                        )
                    }
                }
            }
            gridItemsIndexed(state.visibleClips, key = { _, clip -> clip.id }) { _, clip ->
                val durationStepSeconds = clipDurationStepSeconds(clip)
                val displayPosition = state.clips
                    .takeWhile { it.id != clip.id }
                    .count { it.isRenderableClip }
                    .let { if (clip.isRenderableClip) it + 1 else it }
                    .takeIf { !clip.isVideoSegmentParent && !clip.isSimilarPhotoGroupChild }
                val childSegmentCount = state.clips.count { it.videoSegmentParentId == clip.id }
                CompactClipRow(
                    palette = palette,
                    position = displayPosition,
                    clip = clip,
                    childSegmentCount = childSegmentCount,
                    isSimilarPhotoGroupExpanded = clip.similarPhotoGroupId in state.expandedSimilarPhotoGroupIds,
                    onClick = {
                        if (
                            clip.mediaKind == ClipMediaKind.Video ||
                            clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion
                        ) {
                            trimmingClipID = clip.id
                        } else if (clip.isRenderableClip) {
                            previewClipID = clip.id
                        }
                    },
                    onDecreaseDuration = {
                        viewModel.adjustClipDuration(clip.id, -durationStepSeconds)
                    },
                    onIncreaseDuration = {
                        viewModel.adjustClipDuration(clip.id, durationStepSeconds)
                    },
                    onMoveUp = { viewModel.moveClipUp(clip.id) },
                    onMoveDown = { viewModel.moveClipDown(clip.id) },
                    onDelete = { pendingDeleteClipID = clip.id },
                    onToggleSegmentMode = { viewModel.toggleVideoSegmentMode(clip.id) },
                    onToggleLivePhotoMode = { viewModel.toggleLivePhotoMode(clip.id) },
                    onResetSegments = { viewModel.resetVideoSegments(clip.id) },
                    onPreviewClip = {
                        if (clip.isVideoSegmentParent) {
                            previewClipID = clip.id
                        } else {
                            trimmingClipID = clip.id
                        }
                    },
                    onToggleSimilarPhotoGroup = { viewModel.toggleSimilarPhotoGroup(clip.id) },
                    onIncludeSimilarPhoto = { viewModel.includeSimilarPhoto(clip.id) },
                    onToggleVideoSegmentSelection = { viewModel.toggleVideoSegmentSelection(clip.id) },
                    isReorderMode = isReorderMode
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(if (isReorderMode) 24.dp else 88.dp))
            }
        }
        if (!isReorderMode) {
            if (state.renderableClips.isNotEmpty()) BottomMakeBar(
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 920.dp),
                palette = palette,
                isExporting = state.isExporting,
                clipCount = state.renderableClips.size,
                photoCount = state.renderableClips.count { it.mediaKind != ClipMediaKind.Video },
                videoCount = state.renderableClips.count { it.mediaKind == ClipMediaKind.Video },
                totalSeconds = state.totalDurationSeconds,
                qualityTitle = state.outputQualityPreset.displayTitle,
                hasTextOverlay = state.watermarkSettings.shouldRenderText,
                hasLogoOverlay = state.watermarkSettings.logoEnabled,
                hasMusic = state.backgroundMusicEnabled && (
                    state.backgroundMusicUri != null || state.backgroundMusicSampleId != null
                ),
                selectedRatio = state.outputAspectRatio,
                onSelectRatio = { ratio -> viewModel.selectAspectRatio(context, ratio) },
                onClose = ::requestBackHome,
                onMakeMovie = ::beginMovieExport
            ) else BottomEmptyEditorBar(
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 920.dp),
                palette = palette,
                onAdd = { isImportMenuVisible = true },
                onClose = ::requestBackHome
            )
        }
        if (isImportMenuVisible) {
            EditorImportMenu(
                modifier = Modifier.fillMaxSize(),
                palette = palette,
                onAiShot = {
                    isImportMenuVisible = false
                    onOpenAiShot()
                },
                onPhoto = {
                    isImportMenuVisible = false
                    openCalendarPicker("기본 사진첩")
                },
                onCalendar = {
                    isImportMenuVisible = false
                    openCalendarPicker("사진첩 날짜별")
                },
                onFiles = {
                    isImportMenuVisible = false
                    galleryPicker.launch(mediaFileIntent(context))
                },
                onDismiss = { isImportMenuVisible = false }
            )
        }
        if (state.isImportingMedia || state.isExporting) {
            WorkProgressOverlay(
                palette = palette,
                message = state.progressMessage.ifBlank {
                    if (state.isExporting) "완성본을 만드는 중..." else "사진/영상을 클립으로 준비하는 중..."
                },
                progress = state.workProgress,
                current = state.workCurrent,
                total = state.workTotal,
                previewClip = state.renderableClips.firstOrNull(),
                previewAspectRatio = state.outputAspectRatio?.let { it.width.toFloat() / it.height }
                    ?: state.renderableClips.firstOrNull()?.let {
                        it.sourceWidth.toFloat() / it.sourceHeight.coerceAtLeast(1)
                    }
                    ?: 1f,
                watermarkSettings = state.watermarkSettings,
                isExporting = state.isExporting,
                isCancelling = state.isCancellingExport,
                onCancel = if (state.isCancellingExport) {
                    null
                } else if (state.isExporting) {
                    viewModel::cancelExport
                } else {
                    viewModel::cancelMediaImport
                }
            )
        }
        state.alertMessage?.let { message ->
            AlertDialog(
                onDismissRequest = viewModel::clearAlert,
                dismissButton = if (showPermissionSettingsAction) {
                    {
                        OutlinedButton(
                            onClick = {
                                resumeCalendarAfterSettings = true
                                showPermissionSettingsAction = false
                                viewModel.clearAlert()
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }
                        ) {
                            Text("앱 설정 열기")
                        }
                    }
                } else if (state.undoDeleteMessage != null) {
                    {
                        OutlinedButton(
                            onClick = viewModel::undoLastEditorAction,
                            border = BorderStroke(1.dp, palette.border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = palette.solidPanel,
                                contentColor = palette.text
                            )
                        ) {
                            Text("되돌리기")
                        }
                    }
                } else {
                    null
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionSettingsAction = false
                            viewModel.clearAlert()
                        },
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
                title = { Text("HanClip") },
                text = { Text(message) }
            )
        }
        if (isResetConfirmationVisible) {
            AlertDialog(
                onDismissRequest = { isResetConfirmationVisible = false },
                dismissButton = {
                    OutlinedButton(
                        onClick = { isResetConfirmationVisible = false },
                        border = BorderStroke(1.dp, palette.border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = palette.solidPanel,
                            contentColor = palette.text
                        )
                    ) {
                        Text("취소")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isResetConfirmationVisible = false
                            viewModel.resetProject(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE45D42),
                            contentColor = Color.White
                        )
                    ) {
                        Text("초기화")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.solidPanel,
                titleContentColor = palette.text,
                textContentColor = palette.subText,
                title = { Text("현재 완성본 초기화") },
                text = { Text("가져온 클립과 편집 설정을 모두 비우고 처음부터 다시 시작할까요? 초기화 직후에는 되돌릴 수 있습니다.") }
            )
        }
        pendingDeleteClip?.let { clip ->
            val removalKind = when {
                clip.isVideoSegmentChild -> ClipRemovalKind.SegmentChild
                clip.isVideoSegmentParent -> ClipRemovalKind.VideoWithSegments
                clip.isSimilarPhotoGroupParent -> ClipRemovalKind.SimilarPhotoRepresentative
                else -> ClipRemovalKind.Standard
            }
            AlertDialog(
                onDismissRequest = { pendingDeleteClipID = null },
                dismissButton = {
                    OutlinedButton(onClick = { pendingDeleteClipID = null }) {
                        Text("취소")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetId = clip.id
                            pendingDeleteClipID = null
                            viewModel.removeClip(targetId)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE45D42),
                            contentColor = Color.White
                        )
                    ) {
                        Text("제외")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.solidPanel,
                titleContentColor = palette.text,
                textContentColor = palette.subText,
                title = { Text("HanClip") },
                text = { Text(clipRemovalConfirmationMessage(removalKind)) }
            )
        }
        if (isExitConfirmationVisible) {
            AlertDialog(
                onDismissRequest = { isExitConfirmationVisible = false },
                dismissButton = {
                    OutlinedButton(
                        onClick = { isExitConfirmationVisible = false },
                        border = BorderStroke(1.dp, palette.border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = palette.solidPanel,
                            contentColor = palette.text
                        )
                    ) {
                        Text("계속 편집")
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                isExitConfirmationVisible = false
                                viewModel.discardEditingSessionChanges(context)
                                onBackHome()
                            },
                            border = BorderStroke(1.dp, palette.border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = palette.solidPanel,
                                contentColor = palette.text
                            )
                        ) {
                            Text("홈")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.saveEditingSession(context)
                                isExitConfirmationVisible = false
                            },
                            border = BorderStroke(1.dp, palette.border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = palette.solidPanel,
                                contentColor = palette.text
                            )
                        ) {
                            Text("저장")
                        }
                        Button(
                            onClick = {
                                isExitConfirmationVisible = false
                                viewModel.saveEditingSession(context)
                                onBackHome()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("저장 후 홈")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.solidPanel,
                titleContentColor = palette.text,
                textContentColor = palette.subText,
                title = { Text("편집을 닫을까요?") },
                text = { Text("저장 후 홈은 현재 작업을 저장하고 이동합니다. 저장은 편집 화면에 남고, 홈은 이번에 바꾼 내용을 되돌린 뒤 이동합니다.") }
            )
        }
        if (isQuickDurationVisible) {
            val contentSceneCount = quickContentClips(state.clips).size.coerceAtLeast(1)
            val endingDuration = if (state.watermarkSettings.includesEndingInfoCard) {
                state.watermarkSettings.normalizedEndingInfoCardDuration
            } else {
                0.0
            }
            QuickDurationDialog(
                contentSceneCount = contentSceneCount,
                sceneCount = contentSceneCount + if (endingDuration > 0.0) 1 else 0,
                targetDurationSeconds = quickTargetDurationSeconds,
                estimatedTotalDuration = quickEstimatedTotalDuration(
                    clips = state.clips,
                    targetDurationSeconds = quickTargetDurationSeconds,
                    endingDurationSeconds = endingDuration
                ),
                watermarkEnabled = state.watermarkSettings.isEnabled,
                endingInfoEnabled = state.watermarkSettings.includesEndingInfoCard,
                endingThemeTitle = state.watermarkSettings.endingInfoCardTheme.title,
                musicTitle = state.backgroundMusicTitle,
                captionText = state.watermarkSettings.text,
                musicEnabled = state.backgroundMusicEnabled &&
                    (state.backgroundMusicUri != null || state.backgroundMusicSampleId != null),
                selectedRatio = state.outputAspectRatio,
                palette = palette,
                onTargetDurationChange = { quickTargetDurationSeconds = it.coerceAtLeast(0.1) },
                onToggleWatermark = { enabled ->
                    viewModel.updateWatermarkSilently(state.watermarkSettings.copy(isEnabled = enabled))
                },
                onToggleEnding = { enabled ->
                    viewModel.updateWatermarkSilently(state.watermarkSettings.copy(includesEndingInfoCard = enabled))
                },
                onToggleMusic = { enabled ->
                    if (enabled && state.backgroundMusicUri == null && state.backgroundMusicSampleId == null) {
                        isQuickDurationVisible = false
                        reopenQuickAfterSettings = true
                        openMusicSettings()
                    } else {
                        viewModel.updateBackgroundMusicEnabled(enabled)
                    }
                },
                onSelectRatio = { viewModel.selectAspectRatio(context, it) },
                onOpenText = {
                    isQuickDurationVisible = false
                    reopenQuickAfterSettings = true
                    isTextOverlaySheetVisible = true
                },
                onOpenEnding = {
                    isQuickDurationVisible = false
                    reopenQuickAfterSettings = true
                    isEndingInfoSettingsSheetVisible = true
                },
                onOpenMusic = {
                    isQuickDurationVisible = false
                    reopenQuickAfterSettings = true
                    openMusicSettings()
                },
                onAddPhoto = {
                    isQuickDurationVisible = false
                    reopenQuickAfterPicker = true
                    openCalendarPicker("기본 사진첩")
                },
                onAddFile = {
                    isQuickDurationVisible = false
                    reopenQuickAfterPicker = true
                    galleryPicker.launch(mediaFileIntent(context))
                },
                onDismiss = { isQuickDurationVisible = false },
                onConfirm = {
                    viewModel.applyQuickTargetDuration(quickTargetDurationSeconds)
                    isQuickDurationVisible = false
                    beginMovieExport()
                }
            )
        }
        trimmingClip?.let { clip ->
            val automaticNextClip = if (previewClips.size > 1) {
                previewClips.getOrNull(trimmingClipIndex + 1) ?: previewClips.first()
            } else null
            Dialog(
                onDismissRequest = { trimmingClipID = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                VideoTrimSheet(
                    clip = clip,
                    palette = palette,
                    autoAdvanceOnLoad = clipPreviewAutoAdvanceEnabled || autoAdvancePreviewOnOpen,
                    onAutoAdvanceConsumed = { autoAdvancePreviewOnOpen = false },
                    onAutoAdvanceChanged = { clipPreviewAutoAdvanceEnabled = it },
                    onDismiss = {
                        trimmingClipID = null
                        clipPreviewAutoAdvanceEnabled = false
                    },
                    onFirst = previewClips.firstOrNull()
                        ?.takeIf { first -> first.id != clip.id && previewClips.size > 1 }
                        ?.let { first ->
                            { startSeconds, durationSeconds ->
                                viewModel.updateVideoTrim(clip.id, startSeconds, durationSeconds)
                                openClipFromTrim(first)
                            }
                        },
                    onPrevious = previewClips.getOrNull(trimmingClipIndex - 1)?.let { previous ->
                        { startSeconds, durationSeconds ->
                            viewModel.updateVideoTrim(clip.id, startSeconds, durationSeconds)
                            openClipFromTrim(previous)
                        }
                    },
                    onNext = previewClips.getOrNull(trimmingClipIndex + 1)?.let { next ->
                        { startSeconds, durationSeconds ->
                            viewModel.updateVideoTrim(clip.id, startSeconds, durationSeconds)
                            openClipFromTrim(next)
                        }
                    },
                    onAutoNext = automaticNextClip?.let { next ->
                        { startSeconds, durationSeconds ->
                            viewModel.updateVideoTrim(clip.id, startSeconds, durationSeconds)
                            clipPreviewAutoAdvanceEnabled = true
                            openClipFromTrim(next, autoAdvance = true)
                        }
                    },
                    onDelete = {
                        trimmingClipID = null
                        clipPreviewAutoAdvanceEnabled = false
                        pendingDeleteClipID = clip.id
                    },
                    bottomThumbnailStrip = { startSeconds, durationSeconds ->
                        VideoTrimThumbnailStrip(
                            clips = previewClips,
                            selectedClipId = clip.id,
                            palette = palette,
                            onSelect = { selectedId ->
                                viewModel.updateVideoTrim(clip.id, startSeconds, durationSeconds)
                                previewClips.firstOrNull { it.id == selectedId }
                                    ?.let(::openClipFromTrim)
                            }
                        )
                    },
                    onMakeMovie = {
                        trimmingClipID = null
                        clipPreviewAutoAdvanceEnabled = false
                        beginMovieExport()
                    },
                    onApplyTrim = { startSeconds, durationSeconds ->
                        viewModel.updateVideoTrim(
                            id = clip.id,
                            startSeconds = startSeconds,
                            durationSeconds = durationSeconds
                        )
                    }
                )
            }
        }
        previewClip?.let { clip ->
            ClipPreviewDialog(
                clip = clip,
                clips = previewClips,
                palette = palette,
                childSegmentCount = state.clips.count { it.videoSegmentParentId == clip.id },
                position = previewClipIndex + 1,
                total = previewClips.size,
                autoplayOnLoad = autoAdvancePreviewOnOpen,
                onAutoplayConsumed = { autoAdvancePreviewOnOpen = false },
                autoAdvanceEnabled = clipPreviewAutoAdvanceEnabled,
                onAutoAdvanceChange = { clipPreviewAutoAdvanceEnabled = it },
                onFirst = if (previewClipIndex > 0) {
                    { previewClipID = previewClips.first().id }
                } else null,
                onPrevious = if (previewClipIndex > 0) {
                    { previewClipID = previewClips[previewClipIndex - 1].id }
                } else null,
                onNext = if (previewClipIndex in 0 until previewClips.lastIndex) {
                    { previewClipID = previewClips[previewClipIndex + 1].id }
                } else null,
                onSelectClip = { selectedID -> previewClipID = selectedID },
                onEdit = if (
                        clip.mediaKind == ClipMediaKind.Video ||
                        clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion
                    ) {
                    {
                        previewClipID = null
                        trimmingClipID = clip.id
                    }
                } else null,
                onDelete = {
                    val nextClipId = previewClips.getOrNull(previewClipIndex + 1)?.id
                        ?: previewClips.getOrNull(previewClipIndex - 1)?.id
                    viewModel.removeClip(clip.id)
                    previewClipID = nextClipId
                    if (nextClipId == null) clipPreviewAutoAdvanceEnabled = false
                },
                onMakeMovie = {
                    previewClipID = null
                    clipPreviewAutoAdvanceEnabled = false
                    beginMovieExport()
                },
                onDismiss = {
                    previewClipID = null
                    clipPreviewAutoAdvanceEnabled = false
                }
            )
        }
        if (isTextOverlaySheetVisible) {
            fun closeTextOverlay() {
                isTextOverlaySheetVisible = false
                if (reopenQuickAfterSettings) {
                    reopenQuickAfterSettings = false
                    isQuickDurationVisible = true
                }
            }
            Dialog(
                onDismissRequest = ::closeTextOverlay,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                TextOverlaySheet(
                    settings = state.watermarkSettings,
                    palette = palette,
                    fullScreen = true,
                    mediaCreatedAtMillis = state.renderableClips.mapNotNull { it.sourceCreatedAtMillis },
                    onDismiss = ::closeTextOverlay,
                    onApply = viewModel::updateWatermark
                )
            }
        }
        if (isEndingInfoSettingsSheetVisible) {
            fun closeEndingSettings() {
                isEndingInfoSettingsSheetVisible = false
                if (reopenQuickAfterSettings) {
                    reopenQuickAfterSettings = false
                    isQuickDurationVisible = true
                }
            }
            Dialog(
                onDismissRequest = ::closeEndingSettings,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                EndingInfoSettingsSheet(
                    settings = state.watermarkSettings,
                    stops = state.endingInfoStops(),
                    palette = palette,
                    onDismiss = ::closeEndingSettings,
                    onApply = viewModel::updateEndingInfo
                )
            }
        }
        if (isMusicSettingsSheetVisible) {
            fun closeMusicSettings(save: Boolean) {
                if (!save) {
                    musicSettingsSnapshot?.let { snapshot ->
                        viewModel.restoreBackgroundMusicSettings(
                            uri = snapshot.uri,
                            title = snapshot.title,
                            sampleId = snapshot.sampleId,
                            enabled = snapshot.enabled,
                            musicVolume = snapshot.musicVolume,
                            originalAudioVolume = snapshot.originalAudioVolume,
                            loopsToFillVideo = snapshot.loopsToFillVideo,
                            fadeInEnabled = snapshot.fadeInEnabled,
                            fadeOutEnabled = snapshot.fadeOutEnabled
                        )
                    }
                }
                musicSettingsSnapshot = null
                isMusicSettingsSheetVisible = false
                if (reopenQuickAfterSettings) {
                    reopenQuickAfterSettings = false
                    isQuickDurationVisible = true
                }
            }
            Dialog(
                onDismissRequest = { closeMusicSettings(save = false) },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                MusicSettingsSheet(
                    currentTitle = state.backgroundMusicTitle,
                    currentUri = state.backgroundMusicUri,
                    currentSampleId = state.backgroundMusicSampleId,
                    musicEnabled = state.backgroundMusicEnabled,
                    musicVolume = state.backgroundMusicVolume,
                    originalAudioVolume = state.originalAudioVolume,
                    loopsToFillVideo = state.backgroundMusicLoopsToFillVideo,
                    fadeInEnabled = state.backgroundMusicFadeInEnabled,
                    fadeOutEnabled = state.backgroundMusicFadeOutEnabled,
                    hasSessionChanges = musicSettingsSnapshot?.let { snapshot ->
                        snapshot.uri != state.backgroundMusicUri ||
                            snapshot.title != state.backgroundMusicTitle ||
                            snapshot.sampleId != state.backgroundMusicSampleId ||
                            snapshot.enabled != state.backgroundMusicEnabled ||
                            kotlin.math.abs(snapshot.musicVolume - state.backgroundMusicVolume) > 0.001 ||
                            kotlin.math.abs(snapshot.originalAudioVolume - state.originalAudioVolume) > 0.001 ||
                            snapshot.loopsToFillVideo != state.backgroundMusicLoopsToFillVideo ||
                            snapshot.fadeInEnabled != state.backgroundMusicFadeInEnabled ||
                            snapshot.fadeOutEnabled != state.backgroundMusicFadeOutEnabled
                    } == true,
                    palette = palette,
                    fullScreen = true,
                    onUseSample = { sample ->
                        viewModel.useSampleBackgroundMusic(context, sample)
                    },
                    onPickFile = {
                        isMusicSettingsSheetVisible = false
                        musicPicker.launch(backgroundMusicIntent(context))
                    },
                    onOpenBrowser = {
                        isMusicSettingsSheetVisible = false
                        resumeMusicAfterBrowser = true
                        onOpenBrowser()
                    },
                    onMusicEnabledChange = viewModel::updateBackgroundMusicEnabled,
                    onMusicVolumeChange = viewModel::updateBackgroundMusicVolume,
                    onOriginalAudioVolumeChange = viewModel::updateOriginalAudioVolume,
                    onLoopingChange = viewModel::updateBackgroundMusicLooping,
                    onFadeInChange = viewModel::updateBackgroundMusicFadeIn,
                    onFadeOutChange = viewModel::updateBackgroundMusicFadeOut,
                    onSave = { closeMusicSettings(save = true) },
                    onDismiss = { closeMusicSettings(save = false) }
                )
            }
        }
        if (isCalendarPickerVisible) {
            fun closeCalendarPicker() {
                isCalendarPickerVisible = false
                if (reopenQuickAfterPicker) {
                    reopenQuickAfterPicker = false
                    isQuickDurationVisible = true
                }
            }
            Dialog(
                onDismissRequest = ::closeCalendarPicker,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                CalendarMediaPickerSheet(
                    title = mediaPickerTitle,
                    palette = palette,
                    initialSelectedUris = state.clips
                        .filterNot { it.isVideoSegmentChild }
                        .mapNotNull { clip ->
                            clip.originalSourceUriString
                                ?.let(Uri::parse)
                                ?: clip.sourceUri.takeIf { it.scheme == "content" }
                        }
                        .distinctBy(Uri::toString),
                    onDismiss = ::closeCalendarPicker,
                    onImport = { uris, deselectionScopeUris ->
                        closeCalendarPicker()
                        viewModel.synchronizePickedMedia(
                            context = context,
                            selectedUris = MediaSelectionContract.normalize(uris).uris,
                            deselectionScopeUris = deselectionScopeUris
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun VideoTrimThumbnailStrip(
    clips: List<ClipItem>,
    selectedClipId: String,
    palette: HanClipPalette,
    onSelect: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val selectedIndex = clips.indexOfFirst { it.id == selectedClipId }
    LaunchedEffect(selectedClipId, clips) {
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        itemsIndexed(clips, key = { _, item -> item.id }) { index, item ->
            val selected = item.id == selectedClipId
            Box(
                modifier = Modifier
                    .size(if (selected) 62.dp else 58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (selected) Modifier.border(3.dp, palette.primary, RoundedCornerShape(10.dp))
                        else Modifier.border(1.dp, palette.border, RoundedCornerShape(10.dp))
                    )
                    .clickable(onClickLabel = "${index + 1}번째 영상 편집") { onSelect(item.id) }
            ) {
                ClipThumbnail(item, Modifier.matchParentSize())
                Text(
                    text = "${index + 1}",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.68f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class EditorImportAction {
    Photo,
    Calendar,
    Files
}

private fun calendarMediaPermissions(): List<String> {
    val galleryPermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
        else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        galleryPermissions + Manifest.permission.ACCESS_MEDIA_LOCATION
    } else {
        galleryPermissions
    }
}

private fun Context.hasFullGalleryAccess(): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        else -> ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

private const val CxFileExplorerPackage = "com.cxinventor.file.explorer"

private fun backgroundMusicIntent(context: Context): Intent {
    val audioIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "audio/*"
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
    return fileChooserWithCx(context, audioIntent, "음악 앱 또는 파일 선택")
}

private fun mediaFileIntent(context: Context): Intent {
    val mediaIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return fileChooserWithCx(context, mediaIntent, "사진 앱 또는 파일 선택")
}

private fun fileChooserWithCx(
    context: Context,
    baseIntent: Intent,
    title: String
): Intent = Intent.createChooser(baseIntent, title).apply {
    val cxIntent = Intent(baseIntent).setPackage(CxFileExplorerPackage)
    if (cxIntent.resolveActivity(context.packageManager) != null) {
        putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cxIntent))
    }
}

private val EditorBackground = Brush.verticalGradient(
    listOf(
        Color(0xFFFAFCFA),
        Color(0xFFF4F7F5)
    )
)

private val HanPrimary = Color(0xFF0B7A4E)
private val HanText = Color(0xFF14221A)
private val HanSubText = Color(0xFF46564C)
private val HanBorder = Color(0xFFD4DDD7)
private val HanChipSurface = Color.White

@Composable
private fun EditorHeader(
    palette: HanClipPalette,
    onBackHome: () -> Unit,
    onAddMedia: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable(onClickLabel = "홈으로", onClick = onBackHome)) {
                HanClipBrandCapsule(palette)
            }
            Surface(
                modifier = Modifier
                    .size(58.dp)
                    .clickable(onClick = onAddMedia),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = palette.panel.copy(alpha = palette.panel.alpha * 0.72f),
                border = BorderStroke(
                    1.dp,
                    palette.border.copy(alpha = palette.border.alpha * 0.62f)
                )
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "미디어 추가",
                    tint = palette.primary,
                    modifier = Modifier.padding(16.5.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(18.dp),
                shape = RoundedCornerShape(5.dp),
                color = palette.secondary.copy(alpha = 0.10f)
            ) {
                Icon(
                    Icons.Outlined.MovieCreation,
                    contentDescription = null,
                    tint = palette.primary.copy(alpha = 0.72f),
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(Modifier.width(7.dp))
            Text(
                "영화 제작",
                color = palette.text.copy(alpha = 0.76f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun EditorImportMenu(
    modifier: Modifier,
    palette: HanClipPalette,
    onAiShot: () -> Unit,
    onPhoto: () -> Unit,
    onCalendar: () -> Unit,
    onFiles: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.08f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 74.dp, end = 16.dp)
                .width(300.dp)
                .clickable(enabled = true, onClick = {}),
            shape = RoundedCornerShape(32.dp),
            color = palette.solidPanel,
            border = BorderStroke(1.dp, palette.border),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorImportMenuRow(Icons.Outlined.PhotoCamera, "AiShot", palette, onAiShot)
                EditorImportMenuRow(Icons.Outlined.PhotoLibrary, "사진", palette, onPhoto)
                EditorImportMenuRow(Icons.Outlined.CalendarMonth, "달력", palette, onCalendar)
                EditorImportMenuRow(Icons.Outlined.FolderOpen, "파일", palette, onFiles)
            }
        }
    }
}

@Composable
private fun EditorImportMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    palette: HanClipPalette,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(28.dp))
        Text(
            text = title,
            color = palette.text,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryPanel(
    clipCount: Int,
    photoCount: Int,
    videoCount: Int,
    autoSegmentCount: Int,
    totalSeconds: Double,
    defaultDuration: Double,
    segmentMode: VideoSegmentMode,
    palette: HanClipPalette
) {
    val compositionText = mediaCountSummary(photoCount, videoCount, clipCount)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryMetric("클립", "${clipCount}개", palette, Modifier.weight(1f))
                SummaryMetric("전체", formatSummaryDuration(totalSeconds), palette, Modifier.weight(1f))
                SummaryMetric("기본", "%.1f초".format(defaultDuration), palette, Modifier.weight(1f))
                SummaryMetric("분할", segmentMode.title, palette, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryReadinessPill(
                    text = if (clipCount > 0) "번호순 연결 준비됨" else "사진/영상을 선택하세요",
                    active = clipCount > 0,
                    palette = palette,
                    modifier = Modifier.weight(1.35f)
                )
                SummaryReadinessPill(
                    text = compositionText,
                    active = clipCount > 0,
                    palette = palette,
                    modifier = Modifier.weight(1.15f)
                )
                SummaryReadinessPill(
                    text = if (autoSegmentCount > 0) "자동 컷 ${autoSegmentCount}개" else "자동 컷 대기",
                    active = autoSegmentCount > 0,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    palette: HanClipPalette,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.subText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = palette.text,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryReadinessPill(
    text: String,
    active: Boolean,
    palette: HanClipPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (active) palette.primary.copy(alpha = 0.10f) else palette.panel,
        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.28f) else palette.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = if (active) palette.primary else palette.subText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatSummaryDuration(seconds: Double): String {
    val safeSeconds = seconds.coerceAtLeast(0.0)
    val minutes = (safeSeconds / 60).toInt()
    val remainingSeconds = safeSeconds - minutes * 60
    return if (minutes > 0) {
        "%d분 %.1f초".format(minutes, remainingSeconds)
    } else {
        "%.1f초".format(remainingSeconds)
    }
}

@Composable
private fun ExportConfirmationDialog(
    state: EditorUiState,
    palette: HanClipPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val renderableClipCount = state.renderableClips.size
    val photoCount = state.renderableClips.count { it.mediaKind != ClipMediaKind.Video }
    val videoCount = state.renderableClips.count { it.mediaKind == ClipMediaKind.Video }
    val autoSegmentCount = state.renderableClips.count { it.isVideoSegmentChild }
    val estimatedRenderSize = estimatedRenderSize(state)
    val aspectRatioText = state.outputAspectRatio?.let(::outputRatioChipText)
        ?: "첫 사진 비율 ${estimatedRenderSize.first}x${estimatedRenderSize.second}"
    val musicText = when {
        state.backgroundMusicEnabled &&
            (state.backgroundMusicUri != null || state.backgroundMusicSampleId != null) ->
            state.backgroundMusicTitle ?: "음악 적용"
        else -> "음악 없음"
    }
    val captionText = when {
        state.watermarkSettings.shouldRenderText && state.watermarkSettings.logoEnabled -> "자막 · HanClip 로고"
        state.watermarkSettings.shouldRenderText -> "자막 적용"
        state.watermarkSettings.logoEnabled -> "HanClip 로고"
        else -> "자막 없음"
    }
    val audioMixText = when {
        state.backgroundMusicEnabled &&
            (state.backgroundMusicUri != null || state.backgroundMusicSampleId != null) ->
            "배경 ${percentText(state.backgroundMusicVolume)} · 원본 ${percentText(state.originalAudioVolume)}"
        state.renderableClips.any { it.mediaKind == ClipMediaKind.Video } ->
            "원본 ${percentText(state.originalAudioVolume)}"
        else -> "음악 없음"
    }
    val estimatedSizeText = estimatedMovieSizeText(
        seconds = state.totalDurationSeconds,
        quality = state.outputQualityPreset,
        renderWidth = estimatedRenderSize.first,
        renderHeight = estimatedRenderSize.second
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.text
                )
            ) {
                Text("설정 더 보기")
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onConfirm,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.MovieCreation, contentDescription = null)
                Text("완성본 만들기")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = palette.solidPanel,
        titleContentColor = palette.text,
        textContentColor = palette.subText,
        title = { Text("완성본을 만들까요?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "아래 설정으로 HanClip MP4 완성본을 만듭니다. 고른 번호순으로 이어 붙인 뒤 시사회에서 확인하고 기본 사진첩 저장과 공유를 이어갑니다.",
                    color = palette.subText,
                    style = MaterialTheme.typography.bodyMedium
                )
                ExportConfirmationHero(
                    clipText = "${renderableClipCount}개 클립",
                    durationText = formatSummaryDuration(state.totalDurationSeconds),
                    sizeText = "${estimatedRenderSize.first}x${estimatedRenderSize.second}",
                    formatText = OutputQualityPreset.ExportFormatTitle,
                    palette = palette
                )
                ExportConfirmationLine("완료 후", "시사회에서 확인 · 기본 사진첩 저장 · 공유", palette)
                ExportConfirmationLine("클립", "${renderableClipCount}개 · ${formatSummaryDuration(state.totalDurationSeconds)}", palette)
                ExportConfirmationLine("순서", "선택 번호순으로 이어붙임", palette)
                ExportConfirmationLine("구성", mediaCountSummary(photoCount, videoCount, renderableClipCount), palette)
                if (autoSegmentCount > 0) {
                    ExportConfirmationLine("자동 컷", "${autoSegmentCount}개 · 타격점 중심 자동 컷", palette)
                }
                ExportConfirmationLine("화면", aspectRatioText, palette)
                ExportConfirmationLine("품질", state.outputQualityPreset.chipTitle, palette)
                ExportConfirmationLine("파일", OutputQualityPreset.GallerySaveDetail, palette)
                ExportConfirmationLine("음악", musicText, palette)
                ExportConfirmationLine("오디오", audioMixText, palette)
                ExportConfirmationLine("자막", captionText, palette)
                ExportConfirmationLine("예상 용량", estimatedSizeText, palette)
            }
        }
    )
}

private fun estimatedRenderSize(state: EditorUiState): Pair<Int, Int> {
    state.outputAspectRatio?.let { return it.width to it.height }
    val firstClip = state.renderableClips.firstOrNull()
    return OutputAspectRatio.automaticSize(
        sourceWidth = firstClip?.sourceWidth ?: 1080,
        sourceHeight = firstClip?.sourceHeight ?: 1920
    )
}

private fun mediaCountSummary(photoCount: Int, videoCount: Int, fallbackClipCount: Int): String {
    return when {
        photoCount > 0 && videoCount > 0 -> "사진 ${photoCount}장 · 영상 ${videoCount}개"
        photoCount > 0 -> "사진 ${photoCount}장"
        videoCount > 0 -> "영상 ${videoCount}개"
        else -> "${fallbackClipCount}개 클립"
    }
}

private fun estimatedMovieSizeText(
    seconds: Double,
    quality: OutputQualityPreset,
    renderWidth: Int,
    renderHeight: Int
): String {
    if (seconds <= 0.0) return "계산 전"
    val pixelScale = (renderWidth * renderHeight).toDouble() / (1080.0 * 1920.0)
    val frameRateScale = quality.frameRate / 30.0
    val megabitsPerSecond = (8.0 * pixelScale * frameRateScale).coerceIn(4.0, 24.0)
    val megabytes = seconds * megabitsPerSecond / 8.0
    return when {
        megabytes < 10.0 -> "약 %.1fMB".format(megabytes)
        megabytes < 1024.0 -> "약 %.0fMB".format(megabytes)
        else -> "약 %.1fGB".format(megabytes / 1024.0)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportConfirmationHero(
    clipText: String,
    durationText: String,
    sizeText: String,
    formatText: String,
    palette: HanClipPalette
) {
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
            Text(
                text = "완성본 요약",
                color = palette.text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExportConfirmationPill(clipText, palette, active = true)
                ExportConfirmationPill(durationText, palette, active = true)
                ExportConfirmationPill("번호순 연결", palette, active = true)
                ExportConfirmationPill(sizeText, palette, active = true)
                ExportConfirmationPill("시사회 확인", palette, active = true)
                ExportConfirmationPill(formatText, palette, active = false)
            }
        }
    }
}

@Composable
private fun ExportConfirmationPill(
    text: String,
    palette: HanClipPalette,
    active: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) palette.panel else palette.panel.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.38f) else palette.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            color = if (active) palette.text else palette.subText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExportConfirmationLine(
    label: String,
    value: String,
    palette: HanClipPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = palette.subText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            value,
            color = palette.text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetStatusPanel(
    preset: MoviePreset,
    defaultDuration: Double,
    segmentMode: VideoSegmentMode,
    hasTextOverlay: Boolean,
    hasLogoOverlay: Boolean,
    hasMusic: Boolean,
    musicTitle: String?,
    selectedRatio: OutputAspectRatio?,
    selectedQuality: OutputQualityPreset,
    palette: HanClipPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = preset.title,
                        color = palette.text,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = presetStatusDescription(preset),
                        color = palette.subText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.chip,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = "%.1f초".format(defaultDuration),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        color = palette.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                PresetStatusPill(
                    text = if (segmentMode == VideoSegmentMode.Multiple) "자동 컷" else "단일 컷",
                    active = segmentMode == VideoSegmentMode.Multiple,
                    palette = palette
                )
                PresetStatusPill(
                    text = overlayStatusText(hasTextOverlay, hasLogoOverlay),
                    active = hasTextOverlay || hasLogoOverlay,
                    palette = palette
                )
                PresetStatusPill(
                    text = if (hasMusic) musicTitle ?: "음악 켬" else "음악 꺼짐",
                    active = hasMusic,
                    palette = palette
                )
                PresetStatusPill(
                    text = selectedRatio?.title ?: "첫 사진",
                    active = selectedRatio != null,
                    palette = palette
                )
                PresetStatusPill(
                    text = selectedQuality.chipTitle,
                    active = selectedQuality != OutputQualityPreset.Standard,
                    palette = palette
                )
                PresetStatusPill(
                    text = "HanClip ${OutputQualityPreset.GallerySaveDetail}",
                    active = false,
                    palette = palette
                )
            }
        }
    }
}

@Composable
private fun PresetStatusPill(text: String, active: Boolean, palette: HanClipPalette) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (active) palette.chip else palette.panel,
        border = BorderStroke(1.dp, if (active) palette.secondary else palette.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (active) palette.primary else palette.subText,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun presetStatusDescription(preset: MoviePreset): String {
    return when (preset) {
        MoviePreset.NewMovie -> "기본 사진첩에서 사진과 영상을 한 번에 골라 HanClip 완성본으로 만듭니다."
        MoviePreset.Quick -> "원본 개수를 기준으로 권장 길이를 계산해 빠르게 완성합니다."
        MoviePreset.AiShot -> "스윙 순간을 자동 촬영하고 바로 클립으로 편집합니다."
        MoviePreset.Travel -> "여행 사진과 영상을 순서대로 엮어 짧은 완성본으로 만듭니다."
        MoviePreset.Life -> "비슷한 사진은 세 장 간격으로 정리해 일상의 흐름을 담습니다."
        MoviePreset.Golf -> "타격점을 중심으로 골프 클립과 HanClip 로고를 자동 구성합니다."
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun QuickDurationDialog(
    contentSceneCount: Int,
    sceneCount: Int,
    targetDurationSeconds: Double,
    estimatedTotalDuration: Double,
    watermarkEnabled: Boolean,
    endingInfoEnabled: Boolean,
    endingThemeTitle: String,
    musicTitle: String?,
    musicEnabled: Boolean,
    captionText: String,
    selectedRatio: OutputAspectRatio?,
    palette: HanClipPalette,
    onTargetDurationChange: (Double) -> Unit,
    onToggleWatermark: (Boolean) -> Unit,
    onToggleEnding: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onSelectRatio: (OutputAspectRatio?) -> Unit,
    onOpenText: () -> Unit,
    onOpenEnding: () -> Unit,
    onOpenMusic: () -> Unit,
    onAddPhoto: () -> Unit,
    onAddFile: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val recommendedDuration = contentSceneCount.toDouble().coerceAtLeast(1.0)
    val minimumDuration = (contentSceneCount * 0.1).coerceAtLeast(0.1)
    val navigationBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val quickBottomSafePadding = maxOf(navigationBottomPadding, 72.dp)
    var mediaMenuExpanded by remember { mutableStateOf(false) }
    var usesRecommendedDuration by rememberSaveable {
        mutableStateOf(kotlin.math.abs(targetDurationSeconds - recommendedDuration) < 0.01)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        FullScreenDialogSystemBars(palette.solidPanel)
        Box(
            modifier = Modifier.fillMaxSize().background(palette.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 6.dp,
                    end = 20.dp,
                    bottom = 82.dp + quickBottomSafePadding
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                stickyHeader {
                    Surface(color = palette.solidPanel) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp).clickable(onClick = onDismiss),
                            shape = RoundedCornerShape(29.dp),
                            color = palette.panel.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, palette.border.copy(alpha = 0.62f)),
                            shadowElevation = 5.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "취소",
                                    tint = palette.primary,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                        Text(
                            "퀵모드 영상 길이",
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            color = palette.text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = RoundedCornerShape(29.dp),
                                color = palette.panel.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, palette.border.copy(alpha = 0.62f)),
                                shadowElevation = 5.dp,
                                onClick = { mediaMenuExpanded = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.AddPhotoAlternate,
                                        contentDescription = "미디어 추가",
                                        tint = palette.primary,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = mediaMenuExpanded,
                                onDismissRequest = { mediaMenuExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = palette.solidPanel
                            ) {
                                DropdownMenuItem(
                                    text = { Text("사진") },
                                    leadingIcon = { Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null) },
                                    onClick = {
                                        mediaMenuExpanded = false
                                        onAddPhoto()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("파일") },
                                    leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        mediaMenuExpanded = false
                                        onAddFile()
                                    }
                                )
                            }
                        }
                    }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = palette.secondary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .fillMaxHeight()
                                    .clickable {
                                        usesRecommendedDuration = false
                                        onTargetDurationChange(
                                            (targetDurationSeconds - 5.0)
                                                .coerceAtLeast(minimumDuration)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Remove, contentDescription = "5초 줄이기", tint = palette.primary)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("선택시간", color = palette.subText, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    formatQuickDuration(targetDurationSeconds),
                                    color = palette.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .fillMaxHeight()
                                    .clickable {
                                        usesRecommendedDuration = false
                                        onTargetDurationChange(
                                            (targetDurationSeconds + 5.0).coerceAtMost(3600.0)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "5초 늘리기", tint = palette.primary)
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
                        Text("시간 변경", color = palette.subText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
                    }
                    Spacer(Modifier.height(8.dp))
                    val choices = listOf(
                        "30초" to 30.0,
                        "45초" to 45.0,
                        "1분" to 60.0,
                        "2분" to 120.0,
                        "3분" to 180.0,
                        "5분" to 300.0,
                        "추천시간" to recommendedDuration,
                        "최소시간" to minimumDuration
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        choices.chunked(2).forEachIndexed { rowIndex, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEachIndexed { columnIndex, (label, rawSeconds) ->
                                    val choiceIndex = rowIndex * 2 + columnIndex
                                    val seconds = rawSeconds.coerceAtLeast(minimumDuration)
                                    val selected = when (choiceIndex) {
                                        6 -> usesRecommendedDuration &&
                                            kotlin.math.abs(targetDurationSeconds - seconds) < 0.01
                                        else -> !usesRecommendedDuration &&
                                            kotlin.math.abs(targetDurationSeconds - seconds) < 0.01
                                    }
                                    Button(
                                        modifier = Modifier.weight(1f).heightIn(min = 58.dp),
                                        onClick = {
                                            usesRecommendedDuration = choiceIndex == 6
                                            onTargetDurationChange(seconds)
                                        },
                                        shape = RoundedCornerShape(27.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected) {
                                                palette.primary
                                            } else {
                                                palette.secondary.copy(alpha = 0.08f)
                                            },
                                            contentColor = if (selected) Color.White else palette.text
                                        ),
                                        border = null
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(label, fontWeight = FontWeight.Bold)
                                            Text(
                                                if (choiceIndex < 6) {
                                                    if (seconds > rawSeconds) "최소 ${formatQuickDuration(seconds)}로 적용"
                                                    else "최대 ${(rawSeconds * 10).toInt()}개"
                                                } else {
                                                    formatQuickDuration(seconds)
                                                },
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                maxLines = 2,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = palette.panel,
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Column {
                            QuickSettingRow(
                                icon = Icons.Outlined.TextFields,
                                title = "자막",
                                detail = if (watermarkEnabled) captionText.ifBlank { "내용없음" } else "안함",
                                palette = palette,
                                onOpen = onOpenText
                            ) {
                                CompactChoice("사용", watermarkEnabled, palette, onClick = { onToggleWatermark(true) })
                                CompactChoice("안함", !watermarkEnabled, palette, onClick = { onToggleWatermark(false) })
                            }
                            SettingDivider(palette)
                            QuickSettingRow(
                                icon = Icons.Outlined.LibraryMusic,
                                title = "음악",
                                detail = if (musicEnabled) musicTitle.orEmpty() else "안함",
                                palette = palette,
                                onOpen = onOpenMusic
                            ) {
                                CompactChoice("사용", musicEnabled, palette, onClick = { onToggleMusic(true) }, enabled = musicTitle != null)
                                CompactChoice("안함", !musicEnabled, palette, onClick = { onToggleMusic(false) })
                            }
                            SettingDivider(palette)
                            QuickSettingRow(
                                icon = Icons.Outlined.AutoFixHigh,
                                title = "엔딩",
                                detail = if (endingInfoEnabled) endingThemeTitle else "안함",
                                palette = palette,
                                onOpen = onOpenEnding
                            ) {
                                CompactChoice("사용", endingInfoEnabled, palette, onClick = { onToggleEnding(true) })
                                CompactChoice("안함", !endingInfoEnabled, palette, onClick = { onToggleEnding(false) })
                            }
                        }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = palette.panel.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.32f)),
                        shadowElevation = 5.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            QuickRatioChoice(
                                label = "첫\n사진",
                                selected = selectedRatio == null,
                                palette = palette,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectRatio(null) }
                            )
                            OutputAspectRatio.entries.forEach { ratio ->
                                QuickRatioChoice(
                                    label = quickRatioLabel(ratio),
                                    selected = selectedRatio == ratio,
                                    palette = palette,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSelectRatio(ratio) }
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 12.dp + quickBottomSafePadding
                    )
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    onClick = onConfirm,
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Text(
                        "${sceneCount}개 화면을 ${formatQuickDuration(estimatedTotalDuration)}로 만들기",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickRatioChoice(
    label: String,
    selected: Boolean,
    palette: HanClipPalette,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
        ),
        shape = RoundedCornerShape(0.dp),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (selected) palette.primary else palette.solidPanel.copy(alpha = 0.82f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        if (selected) 2.dp else 1.dp,
                        if (selected) palette.primary else palette.text.copy(alpha = 0.68f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (label == "첫\n사진") {
                    Text(
                        label,
                        color = if (selected) Color.White else palette.text.copy(alpha = 0.88f),
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    val (iconWidth, iconHeight) = when (label) {
                        "1:1" -> 20.dp to 20.dp
                        "3:4" -> 18.dp to 24.dp
                        "4:3" -> 24.dp to 18.dp
                        "9:16" -> 13.5.dp to 24.dp
                        else -> 24.dp to 13.5.dp
                    }
                    Box(
                        modifier = Modifier
                            .size(width = iconWidth, height = iconHeight)
                            .border(
                                2.dp,
                                if (selected) Color.White else palette.text.copy(alpha = 0.88f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

private fun quickRatioLabel(ratio: OutputAspectRatio): String = when (ratio) {
    OutputAspectRatio.Square -> "1:1"
    OutputAspectRatio.Portrait3x4 -> "3:4"
    OutputAspectRatio.Landscape4x3 -> "4:3"
    OutputAspectRatio.Portrait9x16 -> "9:16"
    OutputAspectRatio.Landscape16x9 -> "16:9"
}

private fun formatQuickDuration(seconds: Double): String {
    if (seconds < 1.0) return "%.1f초".format(seconds)
    val rounded = seconds.roundToInt().coerceAtLeast(1)
    val minutes = rounded / 60
    val remainder = rounded % 60
    return when {
        minutes == 0 -> "${remainder}초"
        remainder == 0 -> "${minutes}분"
        else -> "${minutes}분 ${remainder}초"
    }
}

@Composable
private fun QuickSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    palette: HanClipPalette,
    onOpen: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(9.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$title :",
                color = palette.text,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.width(5.dp))
            Text(
                detail,
                modifier = Modifier.weight(1f),
                color = palette.subText,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), content = actions)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImportActionRow(
    palette: HanClipPalette,
    onPickMedia: () -> Unit,
    onPickFiles: () -> Unit,
    onPickCalendar: () -> Unit,
    onPickVideos: () -> Unit,
    onAiCut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            onClick = onPickMedia,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            ActionButtonText("기본 사진첩에서 선택")
        }
        Text(
            text = "기본 사진첩에서 사진과 영상을 한 번에 고르면 선택 번호순으로 완성본에 이어집니다.",
            color = palette.subText,
            style = MaterialTheme.typography.bodySmall
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PresetStatusPill("사진+영상 한 번에", active = true, palette = palette)
            PresetStatusPill("선택 번호순", active = false, palette = palette)
            PresetStatusPill("타격점 자동 컷", active = false, palette = palette)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onPickVideos,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.secondary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.MovieCreation, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                ActionButtonText("영상만 선택")
            }
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onPickCalendar,
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.text
                )
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                ActionButtonText("날짜별 사진첩")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onPickFiles,
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.text
                )
            ) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                ActionButtonText("다른 앱·파일")
            }
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onAiCut,
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.text
                )
            ) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                ActionButtonText("Ai컷")
            }
        }
    }
}

@Composable
private fun ActionButtonText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyClipPanel(
    preset: MoviePreset,
    palette: HanClipPalette,
    onPickMedia: () -> Unit,
    onPickFiles: () -> Unit,
    onPickCalendar: () -> Unit,
    onPickVideos: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (preset == MoviePreset.Golf || preset == MoviePreset.AiShot) {
                    "기본 사진첩에서 골프 사진과 영상을 고르세요"
                } else {
                    "기본 사진첩에서 사진과 영상을 선택하세요"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.text
            )
            Text(
                text = if (preset == MoviePreset.Golf || preset == MoviePreset.AiShot) {
                    "사진은 선택한 길이로 보여주고, 영상은 스윙 타격점 중심으로 자동 컷을 준비합니다. 고른 번호순 그대로 HanClip 완성본에 이어집니다."
                } else {
                    "사진과 영상을 같이 고르면 선택한 번호순으로 클립을 만들고 한 번에 이어 붙입니다."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.subText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetStatusPill("폰 기본 사진첩", active = true, palette = palette)
                PresetStatusPill("사진+영상 한 번에", active = false, palette = palette)
                PresetStatusPill("번호순 유지", active = false, palette = palette)
                if (preset == MoviePreset.Golf || preset == MoviePreset.AiShot) {
                    PresetStatusPill("타격점 자동 컷", active = false, palette = palette)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier.weight(1f).height(50.dp),
                    onClick = onPickMedia,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    ActionButtonText("기본 사진첩")
                }
                Button(
                    modifier = Modifier.weight(1f).height(50.dp),
                    onClick = onPickVideos,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.secondary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Outlined.MovieCreation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    ActionButtonText("영상만 선택")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(50.dp),
                    onClick = onPickCalendar,
                    border = BorderStroke(1.dp, palette.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = palette.solidPanel,
                        contentColor = palette.text
                    )
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    ActionButtonText("날짜별 사진첩")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(50.dp),
                    onClick = onPickFiles,
                    border = BorderStroke(1.dp, palette.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = palette.solidPanel,
                        contentColor = palette.text
                    )
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    ActionButtonText("다른 앱·파일")
                }
            }
        }
    }
}

@Composable
private fun ReorderStrip(
    clips: List<ClipItem>,
    palette: HanClipPalette,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDone: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "순서 변경 · 앞/뒤/삭제",
                    color = palette.text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Button(
                    onClick = onDone,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("완료")
                }
            }
            clips.chunked(4).forEachIndexed { rowIndex, rowClips ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    rowClips.forEachIndexed { columnIndex, clip ->
                        val index = rowIndex * 4 + columnIndex
                        ReorderTile(
                            modifier = Modifier.weight(1f),
                            clip = clip,
                            index = index,
                            palette = palette,
                            canMoveUp = index > 0,
                            canMoveDown = index < clips.lastIndex,
                            onMoveUp = { onMoveUp(clip.id) },
                            onMoveDown = { onMoveDown(clip.id) },
                            onDelete = { onDelete(clip.id) }
                        )
                    }
                    repeat(4 - rowClips.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderTile(
    modifier: Modifier = Modifier,
    clip: ClipItem,
    index: Int,
    palette: HanClipPalette,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = palette.panel,
        border = BorderStroke(1.dp, clipRowBorder(clip, palette))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(clipFallbackBrush(clip, palette)),
            contentAlignment = Alignment.Center
        ) {
            ClipThumbnail(clip = clip, modifier = Modifier.matchParentSize())
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(23.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.44f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.38f)
            ) {
                Text(
                    reorderTileTitle(clip),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = formatClipSeconds(clip.durationSeconds),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 25.dp)
                    .background(Color.Black.copy(alpha = 0.34f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.42f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "앞으로",
                        tint = if (canMoveUp) Color.White else Color.White.copy(alpha = 0.30f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "뒤로",
                        tint = if (canMoveDown) Color.White else Color.White.copy(alpha = 0.30f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun reorderTileTitle(clip: ClipItem): String {
    return when {
        clip.isSimilarPhotoGroupParent -> "묶음"
        clip.isVideoSegmentParent -> "분할"
        clip.mediaKind == ClipMediaKind.Video -> "영상"
        clip.isLivePhoto -> "모션"
        else -> "사진"
    }
}

@Composable
private fun ImportStatusPanel(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEAF5F0),
        border = BorderStroke(1.dp, Color(0xFFD4E8DD))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Color(0xFF1D7F55),
                trackColor = Color(0xFFCDE5D8)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = message.ifBlank { "사진/영상을 클립으로 준비하는 중..." },
                    color = Color(0xFF1D4F38),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "고른 번호순으로 배치하고, 영상은 스윙 타격점 기준 자동 컷을 준비합니다.",
                    color = Color(0xFF4F7B64),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AutoSegmentStatusPanel(
    sourceCount: Int,
    segmentCount: Int,
    defaultDuration: Double,
    palette: HanClipPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(16.dp),
                color = palette.primary
            ) {
                Icon(
                    Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "자동 타격점 분할 완료",
                    color = palette.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "스윙 소리 피크를 기준으로 ${segmentCount}개 자동 컷을 만들었습니다.",
                    color = palette.subText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "원본 ${sourceCount}개 · 클립당 약 ${autoSegmentAverageDurationText(segmentCount, defaultDuration)}",
                    color = palette.subText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "자동 컷 ${segmentCount}",
                color = palette.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun autoSegmentAverageDurationText(segmentCount: Int, defaultDuration: Double): String {
    return if (segmentCount <= 0) {
        "0.0초"
    } else {
        "%.1f초".format(defaultDuration)
    }
}

@Composable
private fun WorkProgressOverlay(
    palette: HanClipPalette,
    message: String,
    progress: Float?,
    current: Int,
    total: Int,
    previewClip: ClipItem?,
    previewAspectRatio: Float,
    watermarkSettings: WatermarkSettings,
    isExporting: Boolean,
    isCancelling: Boolean,
    onCancel: (() -> Unit)? = null
) {
    val title = progressTitle(message, isExporting)
    val detail = progressDetail(message, isExporting)
    val safeProgress = progress?.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.solidPanel.copy(alpha = 0.99f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(32.dp),
            color = palette.chip.compositeOver(palette.solidPanel),
            border = BorderStroke(1.dp, palette.border),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isExporting && previewClip != null) {
                    Surface(
                        modifier = Modifier
                            .sizeIn(maxWidth = 260.dp, maxHeight = 260.dp)
                            .width(220.dp)
                            .aspectRatio(previewAspectRatio.coerceIn(0.56f, 1.78f)),
                        shape = RoundedCornerShape(22.dp),
                        color = palette.solidPanel,
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            ClipThumbnail(previewClip, Modifier.matchParentSize())
                            ProgressWatermarkOverlay(
                                settings = watermarkSettings,
                                modifier = Modifier.matchParentSize().padding(10.dp)
                            )
                            if (safeProgress != null && safeProgress < 1f) {
                                Box(
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .fillMaxHeight()
                                        .fillMaxWidth(1f - safeProgress)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
                CircularProgressIndicator(
                    color = palette.primary,
                    trackColor = palette.solidPanel
                )
                Text(
                    text = when {
                        isCancelling -> "취소 중"
                        isExporting -> "개봉 준비 중"
                        else -> "준비하고 있습니다"
                    },
                    color = palette.text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        color = palette.subText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (safeProgress != null) {
                    LinearProgressIndicator(
                        progress = { safeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = palette.primary,
                        trackColor = palette.solidPanel
                    )
                    Text(
                        text = "${(safeProgress * 100).roundToInt()}%",
                        color = palette.primary,
                        fontSize = 42.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Text(
                        text = title,
                        color = palette.text,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!isExporting && total > 0) {
                    Text(
                        text = "선택한 미디어 ${current.coerceAtMost(total)}/${total}개를 불러오는 중…",
                        color = palette.subText,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = if (isExporting) {
                        "완성되면 시사회 화면으로 이동합니다."
                    } else {
                        "취소해도 기존 클립과 설정은 그대로 유지됩니다."
                    },
                    color = palette.subText,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        border = BorderStroke(1.dp, Color(0xFFF0C6BC)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = palette.solidPanel,
                            contentColor = Color(0xFFE45D42)
                        )
                    ) {
                        Text(if (isExporting) "만들기 취소" else "가져오기 취소", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressWatermarkOverlay(
    settings: WatermarkSettings,
    modifier: Modifier = Modifier
) {
    if (!settings.shouldRender) return
    val context = LocalContext.current
    Box(modifier) {
        if (settings.shouldRenderText) {
            Text(
                text = settings.text,
                modifier = Modifier.align(previewAlignment(settings.position)),
                color = parseHexColor(settings.textColorHex),
                fontFamily = fontFamilyForName(context, settings.fontName),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = (14 * settings.lineSpacingScale.coerceIn(0.5, 2.0)).sp,
                style = MaterialTheme.typography.bodySmall.copy(
                    shadow = previewTextShadow(settings)
                )
            )
        }
        if (settings.logoEnabled) {
            CopyrightLogoPreview(
                modifier = Modifier.align(previewAlignment(settings.copyrightPosition)),
                settings = settings,
                color = parseHexColor(settings.effectiveLogoColorHex)
            )
        }
    }
}

private fun progressTitle(message: String, isExporting: Boolean): String {
    val cleanMessage = message.trim()
    val percent = Regex("""\d+%""").find(cleanMessage)?.value
    val importStep = Regex("""\d+/\d+""").find(cleanMessage)?.value
    return when {
        isExporting && percent != null -> "완성본 만드는 중 $percent"
        isExporting -> "완성본을 만드는 중"
        importStep != null -> "클립 준비 중 $importStep"
        cleanMessage.contains("준비하는 중") || cleanMessage.contains("불러오는 중") -> "클립을 준비하는 중"
        cleanMessage.isNotBlank() -> cleanMessage.substringBefore(" · ").substringBefore("...")
        else -> "작업 중"
    }
}

private fun progressDetail(message: String, isExporting: Boolean): String {
    val cleanMessage = message.trim()
    return when {
        isExporting -> cleanMessage
            .substringAfter(" · ", missingDelimiterValue = cleanMessage)
            .removePrefix("완성본을 만드는 중...")
            .trim()
        cleanMessage.contains("불러오는 중") -> cleanMessage
        else -> ""
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectControls(
    defaultDuration: Double,
    preset: MoviePreset,
    defaultVideoSegmentMode: VideoSegmentMode,
    usesFullVideoRange: Boolean,
    hasLivePhotos: Boolean,
    livePhotosUseMotion: Boolean,
    selectedRatio: OutputAspectRatio?,
    selectedQuality: OutputQualityPreset,
    palette: HanClipPalette,
    hasTextOverlay: Boolean,
    captionText: String,
    captionAppearance: String,
    hasLogoOverlay: Boolean,
    hasMusic: Boolean,
    hasEnding: Boolean,
    endingDuration: Double,
    endingThemeTitle: String,
    musicTitle: String?,
    musicVolume: Double,
    originalAudioVolume: Double,
    hasSimilarPhotoGroups: Boolean,
    similarPhotoRepresentativeInterval: Int,
    similarPhotoGroupMode: VideoSegmentMode,
    isReorderMode: Boolean,
    isAdvancedSettingsExpanded: Boolean,
    isClipSettingsExpanded: Boolean,
    sleepPreventionMode: SleepPreventionMode,
    hasClips: Boolean,
    onSelectRatio: (OutputAspectRatio?) -> Unit,
    onSelectQuality: (OutputQualityPreset) -> Unit,
    onSetDuration: (Double) -> Unit,
    onApplyDuration: () -> Unit,
    onUseSelectedVideoRanges: () -> Unit,
    onUseFullVideoRanges: () -> Unit,
    onSetVideoSegmentMode: (VideoSegmentMode) -> Unit,
    onSetLivePhotoMotion: (Boolean) -> Unit,
    onOpenTextOverlay: () -> Unit,
    onToggleTextOverlay: (Boolean) -> Unit,
    onOpenMusicSettings: () -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleEnding: (Boolean) -> Unit,
    onDecreaseEndingDuration: () -> Unit,
    onIncreaseEndingDuration: () -> Unit,
    onOpenEndingSettings: () -> Unit,
    onSetSimilarPhotoInterval: (Int) -> Unit,
    onSetSimilarPhotoMode: (VideoSegmentMode) -> Unit,
    onToggleReorder: () -> Unit,
    onToggleAdvancedSettings: () -> Unit,
    onToggleClipSettings: () -> Unit,
    onOpenQuickDuration: () -> Unit,
    onCycleSleepPrevention: () -> Unit,
    onResetProject: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onToggleClipSettings)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.secondary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = palette.primary.copy(alpha = 0.86f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(9.dp))
            Text(
                "클립 설정",
                color = palette.text.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Surface(
                modifier = Modifier
                    .width(112.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(50),
                color = palette.chip,
                border = BorderStroke(1.dp, palette.border),
                onClick = if (preset == MoviePreset.Quick) onOpenQuickDuration else onToggleClipSettings
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        preset.settingIcon(),
                        contentDescription = null,
                        tint = palette.subText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        preset.title,
                        color = palette.subText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
            Icon(
                if (isClipSettingsExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (isClipSettingsExpanded) "클립 설정 접기" else "클립 설정 펼치기",
                tint = palette.subText,
                modifier = Modifier.size(24.dp)
            )
        }
        if (isClipSettingsExpanded) Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = palette.panel.compositeOver(palette.solidPanel),
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column {
                CompactSettingRow(Icons.Outlined.SwapHoriz, "영상 길이", palette) {
                    CompactChoice("선택구간", !usesFullVideoRange, palette, onUseSelectedVideoRanges)
                    CompactChoice("전체영상", usesFullVideoRange, palette, onUseFullVideoRanges)
                }
                SettingDivider(palette)
                CompactSettingRow(Icons.Outlined.Timer, "기본시간", palette) {
                    StepperPill(
                        value = "%.1f초".format(defaultDuration),
                        onDecrease = { onSetDuration(steppedDefaultDuration(defaultDuration, increase = false)) },
                        onIncrease = { onSetDuration(steppedDefaultDuration(defaultDuration, increase = true)) },
                        canDecrease = defaultDuration > 0.1,
                        canIncrease = defaultDuration < 30.0,
                        palette = palette
                    )
                    CompactChoice("적용", true, palette, onApplyDuration)
                }
                SettingDivider(palette)
                CompactSettingRow(Icons.Outlined.MotionPhotosOn, "모션포토", palette) {
                    CompactChoice(
                        "사진",
                        !livePhotosUseMotion,
                        palette,
                        onClick = { onSetLivePhotoMotion(false) }
                    )
                    CompactChoice(
                        "영상",
                        livePhotosUseMotion,
                        palette,
                        onClick = { onSetLivePhotoMotion(true) },
                        enabled = hasLivePhotos
                    )
                }
                SettingDivider(palette)
                CompactSettingRow(Icons.Outlined.MovieCreation, "영상", palette) {
                    CompactChoice(
                        "한컷",
                        defaultVideoSegmentMode != VideoSegmentMode.Multiple,
                        palette,
                        onClick = { onSetVideoSegmentMode(VideoSegmentMode.Single) }
                    )
                    CompactChoice(
                        "분할",
                        defaultVideoSegmentMode == VideoSegmentMode.Multiple,
                        palette,
                        onClick = { onSetVideoSegmentMode(VideoSegmentMode.Multiple) }
                    )
                }
                SettingDivider(palette)
                if (hasSimilarPhotoGroups) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Collections, contentDescription = null, tint = palette.subText, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(9.dp))
                            Text("묶음사진", color = palette.subText, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StepperPill(
                            value = "1/$similarPhotoRepresentativeInterval",
                            onDecrease = { onSetSimilarPhotoInterval(similarPhotoRepresentativeInterval - 1) },
                            onIncrease = { onSetSimilarPhotoInterval(similarPhotoRepresentativeInterval + 1) },
                            canDecrease = similarPhotoRepresentativeInterval > 1,
                            canIncrease = similarPhotoRepresentativeInterval < 20,
                            palette = palette
                        )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(start = 27.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("선택 방식", color = palette.subText, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            CompactChoice("자동", similarPhotoGroupMode == VideoSegmentMode.Single, palette, onClick = { onSetSimilarPhotoMode(VideoSegmentMode.Single) })
                            CompactChoice("수동", similarPhotoGroupMode == VideoSegmentMode.Multiple, palette, onClick = { onSetSimilarPhotoMode(VideoSegmentMode.Multiple) })
                            CompactChoice("전체", similarPhotoGroupMode == VideoSegmentMode.All, palette, onClick = { onSetSimilarPhotoMode(VideoSegmentMode.All) })
                        }
                    }
                } else {
                    CompactSettingRow(Icons.Outlined.Collections, "묶음사진", palette) {
                        Text("없음", color = palette.subText, style = MaterialTheme.typography.labelLarge)
                    }
                }
                SettingDivider(palette)
                SummarySettingRow(
                    icon = Icons.Outlined.TextFields,
                    label = "자막",
                    enabled = hasTextOverlay,
                    primaryDetail = captionText.ifBlank { "내용 없음" },
                    secondaryDetail = captionAppearance,
                    minHeight = 82.dp,
                    palette = palette,
                    onOpen = onOpenTextOverlay,
                    onToggle = onToggleTextOverlay
                )
                SettingDivider(palette)
                SummarySettingRow(
                    icon = Icons.Outlined.LibraryMusic,
                    label = "음악",
                    enabled = hasMusic,
                    primaryDetail = musicTitle ?: "선택된 음악 없음",
                    secondaryDetail = "음악 ${(musicVolume * 100).roundToInt()}% · 원본 ${(originalAudioVolume * 100).roundToInt()}%",
                    minHeight = 82.dp,
                    palette = palette,
                    onOpen = onOpenMusicSettings,
                    onToggle = onToggleMusic
                )
                SettingDivider(palette)
                EndingSettingRow(
                    enabled = hasEnding,
                    duration = endingDuration,
                    themeTitle = endingThemeTitle,
                    palette = palette,
                    onOpen = onOpenEndingSettings,
                    onToggle = onToggleEnding,
                    onDecreaseDuration = onDecreaseEndingDuration,
                    onIncreaseDuration = onIncreaseEndingDuration
                )
            }
        }
        if (isClipSettingsExpanded) Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleAdvancedSettings),
            shape = RoundedCornerShape(16.dp),
            color = palette.chip,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "화면비 · 화면 유지${if (hasClips) " · 초기화" else ""}",
                    color = palette.subText,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    if (isAdvancedSettingsExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (isAdvancedSettingsExpanded) "고급 설정 접기" else "고급 설정 펼치기",
                    tint = palette.secondary
                )
            }
        }
        if (isClipSettingsExpanded && isAdvancedSettingsExpanded) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedRatio == null,
                    onClick = { onSelectRatio(null) },
                    label = { Text("첫 사진") },
                    colors = clearFilterChipColors(palette),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedRatio == null,
                        borderColor = palette.border,
                        selectedBorderColor = palette.primary
                    )
                )
                OutputAspectRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = selectedRatio == ratio,
                        onClick = { onSelectRatio(ratio) },
                        label = { Text(outputRatioChipText(ratio)) },
                        colors = clearFilterChipColors(palette),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedRatio == ratio,
                            borderColor = palette.border,
                            selectedBorderColor = palette.primary
                        )
                    )
                }
                AssistChip(
                    onClick = onCycleSleepPrevention,
                    leadingIcon = { Icon(Icons.Outlined.LightMode, contentDescription = null) },
                    label = { Text(sleepPreventionMode.chipTitle) },
                    colors = clearAssistChipColors(sleepPreventionMode != SleepPreventionMode.AlwaysOff, palette),
                    border = BorderStroke(1.dp, palette.border)
                )
                if (hasClips) {
                    AssistChip(
                        onClick = onResetProject,
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        label = { Text("프로젝트 초기화") },
                        colors = clearAssistChipColors(false, palette),
                        border = BorderStroke(1.dp, palette.border)
                    )
                }
            }
        }
    }
}

private fun MoviePreset.settingIcon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    MoviePreset.NewMovie -> Icons.Outlined.MovieCreation
    MoviePreset.Quick -> Icons.Outlined.Bolt
    MoviePreset.AiShot -> Icons.Outlined.AddPhotoAlternate
    MoviePreset.Travel -> Icons.Outlined.Map
    MoviePreset.Life -> Icons.Outlined.Collections
    MoviePreset.Golf -> Icons.Outlined.PlayCircle
}

@Composable
private fun EndingSettingRow(
    enabled: Boolean,
    duration: Double,
    themeTitle: String,
    palette: HanClipPalette,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDecreaseDuration: () -> Unit,
    onIncreaseDuration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, tint = palette.subText, modifier = Modifier.size(18.dp))
            Text("엔딩", color = palette.subText, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).clickable(onClick = onOpen))
            CompactChoice("사용", enabled, palette, onClick = { onToggle(true) })
            CompactChoice("안함", !enabled, palette, onClick = { onToggle(false) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(start = 27.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(themeTitle, color = palette.subText, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).clickable(onClick = onOpen), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Surface(shape = RoundedCornerShape(50), color = palette.chip, border = BorderStroke(1.dp, palette.border)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecreaseDuration,
                    enabled = duration > 1.0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Remove, contentDescription = "엔딩 시간 줄이기", modifier = Modifier.size(13.dp))
                }
                Text(
                    "%.1f초".format(duration),
                    color = palette.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onIncreaseDuration,
                    enabled = duration < 10.0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "엔딩 시간 늘리기", modifier = Modifier.size(13.dp))
                }
                }
            }
        }
    }
}

@Composable
private fun SummarySettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    primaryDetail: String,
    secondaryDetail: String,
    minHeight: androidx.compose.ui.unit.Dp,
    palette: HanClipPalette,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).padding(horizontal = 14.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = palette.subText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text(label, color = palette.subText, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).clickable(onClick = onOpen))
            CompactChoice("사용", enabled, palette, onClick = { onToggle(true) })
            Spacer(Modifier.width(5.dp))
            CompactChoice("안함", !enabled, palette, onClick = { onToggle(false) })
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 27.dp).clickable(onClick = onOpen)) {
            Text(primaryDetail, color = palette.text, fontSize = 15.sp, lineHeight = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(secondaryDetail, color = palette.subText, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun buildCaptionAppearanceSummary(
    sizeTitle: String,
    position: WatermarkPosition,
    shadowEnabled: Boolean
): String {
    val vertical = listOf("위", "위쪽", "가운데", "아래쪽", "아래")[position.gridRow]
    val horizontal = listOf("왼쪽", "왼쪽 안", "가운데", "오른쪽 안", "오른쪽")[position.gridColumn]
    return "$sizeTitle · $vertical $horizontal · 그림자 ${if (shadowEnabled) "사용" else "안함"}"
}

private fun EditorUiState.endingInfoStops(): List<EndingInfoStop> =
    renderableClips
        .filter(ClipItem::hasUsableSourceLocation)
        .mapNotNull { clip ->
            clip.sourceLocationName
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { location ->
                    EndingInfoStop(
                        location = location,
                        dateText = clip.sourceCreatedAtMillis?.let { value ->
                            SimpleDateFormat("M. d.", Locale.KOREAN).format(Date(value))
                        }.orEmpty()
                    )
                }
        }
        .distinctBy { "${it.dateText}|${it.location}" }

@Composable
private fun CompactSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    palette: HanClipPalette,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = palette.subText, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        Text(
            label,
            color = palette.subText,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun CompactChoice(
    text: String,
    selected: Boolean,
    palette: HanClipPalette,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .height(48.dp)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = RoundedCornerShape(50),
        color = if (selected) palette.primary else palette.chip,
        border = BorderStroke(1.dp, if (selected) palette.primary else palette.border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 10.dp),
                color = if (!enabled) palette.subText.copy(alpha = 0.45f) else if (selected) Color.White else palette.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StepperPill(
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean = true,
    canIncrease: Boolean = true,
    palette: HanClipPalette
) {
    Surface(shape = RoundedCornerShape(50), color = palette.chip, border = BorderStroke(1.dp, palette.border)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = canDecrease, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Remove, contentDescription = "줄이기", modifier = Modifier.size(16.dp))
            }
            Text(
                value,
                color = palette.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onIncrease, enabled = canIncrease, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = "늘리기", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SettingDivider(palette: HanClipPalette) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.border.copy(alpha = 0.7f)))
}

private fun outputRatioChipText(ratio: OutputAspectRatio): String {
    return "${ratio.title} ${ratio.width}x${ratio.height}"
}

private fun percentText(value: Double): String {
    return "${(value.coerceIn(0.0, 1.0) * 100).toInt()}%"
}

@Composable
private fun clearAssistChipColors(
    active: Boolean,
    palette: HanClipPalette
) = AssistChipDefaults.assistChipColors(
    containerColor = if (active) palette.chip else palette.panel,
    labelColor = if (active) palette.primary else palette.text,
    leadingIconContentColor = if (active) palette.primary else palette.subText
)

@Composable
private fun clearFilterChipColors(palette: HanClipPalette) = FilterChipDefaults.filterChipColors(
    containerColor = palette.solidPanel,
    labelColor = palette.text,
    iconColor = palette.subText,
    selectedContainerColor = palette.primary,
    selectedLabelColor = Color.White,
    selectedLeadingIconColor = Color.White
)

@Composable
private fun CompactClipRow(
    palette: HanClipPalette,
    position: Int?,
    clip: ClipItem,
    childSegmentCount: Int,
    isSimilarPhotoGroupExpanded: Boolean,
    onClick: () -> Unit,
    onDecreaseDuration: () -> Unit,
    onIncreaseDuration: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onToggleSegmentMode: () -> Unit,
    onToggleLivePhotoMode: () -> Unit,
    onResetSegments: () -> Unit,
    onPreviewClip: () -> Unit,
    onToggleSimilarPhotoGroup: () -> Unit,
    onIncludeSimilarPhoto: () -> Unit,
    onToggleVideoSegmentSelection: () -> Unit,
    isReorderMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = clipRowFill(clip, palette),
        border = BorderStroke(1.dp, clipRowBorder(clip, palette))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 9.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when {
                    clip.isVideoSegmentParent -> "·"
                    clip.isSimilarPhotoGroupChild -> "+"
                    else -> "${position ?: 0}"
                },
                modifier = Modifier.width(18.dp),
                color = palette.subText,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(clipFallbackBrush(clip, palette))
                    .clickable(
                        enabled = clip.mediaKind == ClipMediaKind.Video,
                        onClick = onPreviewClip
                    ),
                contentAlignment = Alignment.Center
            ) {
                ClipThumbnail(clip = clip, modifier = Modifier.matchParentSize())
                if (clip.isSimilarPhotoGroupParent) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp),
                        shape = RoundedCornerShape(50),
                        color = palette.secondary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            "${clip.similarPhotoGroupCount}",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = clipCompactTimeText(clip, childSegmentCount),
                    color = palette.text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    when {
                        clip.isVideoSegmentChild -> {
                            ClipControlPill(
                                palette = palette,
                                text = if (clip.isVideoSegmentSelected) "제외" else "사용",
                                active = clip.isVideoSegmentSelected,
                                onClick = onToggleVideoSegmentSelection
                            )
                        }
                        clip.isSimilarPhotoGroupChild -> {
                            ClipControlPill(
                                palette = palette,
                                text = if (clip.isHiddenSimilarPhotoGroupMember) "사용" else "제외",
                                active = !clip.isHiddenSimilarPhotoGroupMember,
                                onClick = onIncludeSimilarPhoto
                            )
                        }
                        clip.isVideoSegmentParent -> {
                            ClipControlPill(palette, "원본보기", active = true, onClick = onPreviewClip)
                            ClipControlPill(palette, "재분할", active = true, onClick = onResetSegments)
                        }
                        clip.mediaKind == ClipMediaKind.LivePhoto -> {
                            ClipControlPill(
                                palette,
                                clip.livePhotoMode.title,
                                active = true,
                                onClick = onToggleLivePhotoMode
                            )
                        }
                        clip.mediaKind == ClipMediaKind.Video && !clip.isVideoSegmentChild -> {
                            ClipControlPill(
                                palette,
                                if (clip.videoSegmentMode == VideoSegmentMode.Multiple) "분할" else "한컷",
                                active = clip.videoSegmentMode == VideoSegmentMode.Multiple,
                                onClick = onToggleSegmentMode
                            )
                        }
                        clip.isSimilarPhotoGroupParent -> {
                            ClipControlPill(
                                palette,
                                if (isSimilarPhotoGroupExpanded) "묶음 접기" else "묶음 보기",
                                active = isSimilarPhotoGroupExpanded,
                                onClick = onToggleSimilarPhotoGroup
                            )
                        }
                        else -> {
                            Text(
                                text = if (clip.mediaKind == ClipMediaKind.Photo) "사진" else "영상",
                                color = palette.subText,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            if (isReorderMode) {
                Column {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "위로")
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "아래로")
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = palette.secondary)
                    }
                }
            } else if (!clip.isVideoSegmentParent && !clip.isHiddenSimilarPhotoGroupMember && !clip.isHiddenVideoSegmentChild) {
                CompactDurationStepper(
                    palette = palette,
                    onDecrease = onDecreaseDuration,
                    onIncrease = onIncreaseDuration,
                    canDecrease = clip.durationSeconds - clipDurationStepSeconds(clip) >= 0.1,
                    canIncrease = clip.durationSeconds + clipDurationStepSeconds(clip) <=
                        clipMaximumDuration(clip) + 0.0001,
                    stepSeconds = clipDurationStepSeconds(clip)
                )
            }
        }
    }
}

private fun clipCompactTimeText(clip: ClipItem, childSegmentCount: Int): String {
    val source = clip.sourceDurationSeconds ?: clip.durationSeconds
    return when {
        clip.isVideoSegmentParent ->
            "원본 ${formatClipSeconds(source)} · 자동 컷 ${childSegmentCount}개"
        !clip.isSimilarPhotoGroupParent &&
            clip.mediaKind != ClipMediaKind.Video &&
            clip.livePhotoMode != com.hanclip.android.core.model.LivePhotoMode.Motion ->
            "%.1f초".format(clip.durationSeconds)
        else ->
            "${formatClipSeconds(clip.durationSeconds)} / 전체 ${formatClipSeconds(source)}"
    }
}

@Composable
private fun CompactDurationStepper(
    palette: HanClipPalette,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    canIncrease: Boolean,
    stepSeconds: Double
) {
    Surface(
        modifier = Modifier.size(width = 68.dp, height = 20.dp),
        shape = RoundedCornerShape(50),
        color = palette.chip.copy(alpha = 0.30f),
        border = BorderStroke(0.8.dp, palette.border.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrease,
                enabled = canDecrease,
                modifier = Modifier.size(width = 33.5.dp, height = 20.dp)
            ) {
                Icon(
                    Icons.Outlined.Remove,
                    contentDescription = if (stepSeconds == 1.0) {
                        "클립 시간을 1초 줄이기"
                    } else {
                        "클립 시간을 0.1초 줄이기"
                    },
                    modifier = Modifier.size(15.dp),
                    tint = palette.subText.copy(alpha = if (canDecrease) 0.54f else 0.18f)
                )
            }
            Box(Modifier.width(1.dp).height(11.dp).background(palette.subText.copy(alpha = 0.20f)))
            IconButton(
                onClick = onIncrease,
                enabled = canIncrease,
                modifier = Modifier.size(width = 33.5.dp, height = 20.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = if (stepSeconds == 1.0) {
                        "클립 시간을 1초 늘리기"
                    } else {
                        "클립 시간을 0.1초 늘리기"
                    },
                    modifier = Modifier.size(15.dp),
                    tint = palette.subText.copy(alpha = if (canIncrease) 0.54f else 0.18f)
                )
            }
        }
    }
}

private fun clipDurationStepSeconds(clip: ClipItem): Double =
    if (
        clip.mediaKind == ClipMediaKind.Video ||
        (clip.mediaKind == ClipMediaKind.LivePhoto &&
            clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion)
    ) {
        1.0
    } else {
        0.1
    }

private fun clipMaximumDuration(clip: ClipItem): Double =
    if (
        clip.mediaKind == ClipMediaKind.Video ||
        (clip.mediaKind == ClipMediaKind.LivePhoto &&
            clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion)
    ) {
        clip.sourceDurationSeconds ?: clip.durationSeconds
    } else {
        30.0
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClipRow(
    palette: HanClipPalette,
    position: Int?,
    clip: ClipItem,
    childSegmentCount: Int,
    isSimilarPhotoGroupExpanded: Boolean,
    onClick: () -> Unit,
    onDecreaseDuration: () -> Unit,
    onIncreaseDuration: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onToggleSegmentMode: () -> Unit,
    onToggleLivePhotoMode: () -> Unit,
    onResetSegments: () -> Unit,
    onPreviewClip: () -> Unit,
    onToggleSimilarPhotoGroup: () -> Unit,
    onIncludeSimilarPhoto: () -> Unit,
    isReorderMode: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = clipRowFill(clip, palette)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (clip.isVideoSegmentChild) 0.dp else 1.dp),
        border = BorderStroke(1.dp, clipRowBorder(clip, palette))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .width(if (clip.isVideoSegmentParent) 5.dp else if (clip.isVideoSegmentChild) 3.dp else 0.dp)
                    .fillMaxSize()
                    .background(if (clip.isVideoSegmentParent) palette.primary else palette.secondary)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(
                        start = if (clip.isVideoSegmentChild) 18.dp else 10.dp,
                        top = 9.dp,
                        end = 8.dp,
                        bottom = 9.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when {
                        clip.isVideoSegmentParent -> "·"
                        clip.isSimilarPhotoGroupChild -> "+"
                        else -> "${position ?: 0}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.primary,
                    fontWeight = if (clip.isVideoSegmentParent) FontWeight.Black else FontWeight.SemiBold,
                    modifier = Modifier.width(18.dp)
                )
                Box(
                    modifier = Modifier
                        .size(
                            width = if (clip.isVideoSegmentChild) 50.dp else 58.dp,
                            height = if (clip.isVideoSegmentChild) 50.dp else 58.dp
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(clipFallbackBrush(clip, palette)),
                    contentAlignment = Alignment.Center
                ) {
                    ClipThumbnail(
                        clip = clip,
                        modifier = Modifier.matchParentSize()
                    )
                    Text(
                        text = when {
                            clip.isVideoSegmentParent -> "$childSegmentCount"
                            clip.isSimilarPhotoGroupChild -> "+"
                            else -> "${position ?: 0}"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (clip.isSimilarPhotoGroupParent) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                            shape = RoundedCornerShape(50),
                            color = palette.secondary.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = "${clip.similarPhotoGroupCount}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = clipTitle(clip, position),
                        fontWeight = FontWeight.SemiBold,
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = clipPrimaryTimeText(clip, childSegmentCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.subText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = clipModeText(clip, childSegmentCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        clipInfoChips(clip, childSegmentCount).forEach { info ->
                            ClipInfoChip(
                                palette = palette,
                                text = info,
                                active = clip.mediaKind == ClipMediaKind.Video
                            )
                        }
                    }
                    if (clip.isSimilarPhotoGroupChild) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ClipControlPill(
                                palette = palette,
                                text = if (clip.isHiddenSimilarPhotoGroupMember) "사용" else "제외",
                                active = clip.isSimilarPhotoGroupRepresentative,
                                icon = { Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onIncludeSimilarPhoto
                            )
                        }
                    } else if (!clip.isVideoSegmentParent) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (clip.mediaKind == ClipMediaKind.Video) {
                                ClipControlPill(
                                    palette = palette,
                                    text = "미리보기",
                                    active = clip.isVideoSegmentChild,
                                    icon = { Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                    onClick = onPreviewClip
                                )
                            }
                            if (clip.mediaKind == ClipMediaKind.Video && !clip.isVideoSegmentChild) {
                                ClipControlPill(
                                    palette = palette,
                                    text = if (clip.videoSegmentMode == VideoSegmentMode.Multiple) "자동 컷" else "단일 컷",
                                    active = clip.videoSegmentMode == VideoSegmentMode.Multiple,
                                    icon = { Icon(Icons.Outlined.DragIndicator, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                    onClick = onToggleSegmentMode
                                )
                            }
                            if (clip.mediaKind == ClipMediaKind.LivePhoto) {
                                ClipControlPill(
                                    palette = palette,
                                    text = clip.livePhotoMode.title,
                                    active = clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion,
                                    icon = { Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                    onClick = onToggleLivePhotoMode
                                )
                            }
                            if (clip.isSimilarPhotoGroupParent) {
                                ClipControlPill(
                                    palette = palette,
                                    text = if (isSimilarPhotoGroupExpanded) "접기" else "묶음",
                                    active = isSimilarPhotoGroupExpanded,
                                    icon = { Icon(Icons.Outlined.Collections, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                    onClick = onToggleSimilarPhotoGroup
                                )
                            }
                            ClipControlPill(
                                palette = palette,
                                text = "-1초",
                                icon = { Icon(Icons.Outlined.Remove, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onDecreaseDuration
                            )
                            ClipControlPill(
                                palette = palette,
                                text = "+1초",
                                icon = { Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onIncreaseDuration
                            )
                        }
                    } else if (clip.mediaKind == ClipMediaKind.Video) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ClipControlPill(
                                palette = palette,
                                text = "원본보기",
                                active = true,
                                icon = { Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onPreviewClip
                            )
                            ClipControlPill(
                                palette = palette,
                                text = "재분할",
                                active = true,
                                icon = { Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onResetSegments
                            )
                            ClipControlPill(
                                palette = palette,
                                text = "단일",
                                icon = { Icon(Icons.Outlined.MovieCreation, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                onClick = onToggleSegmentMode
                            )
                        }
                    }
                }
                Column {
                    if (isReorderMode) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "위로", tint = palette.text)
                        }
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "아래로", tint = palette.text)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = palette.secondary)
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ClipPreviewDialog(
    clip: ClipItem,
    clips: List<ClipItem>,
    palette: HanClipPalette,
    childSegmentCount: Int,
    position: Int,
    total: Int,
    autoplayOnLoad: Boolean,
    onAutoplayConsumed: () -> Unit,
    autoAdvanceEnabled: Boolean,
    onAutoAdvanceChange: (Boolean) -> Unit,
    onFirst: (() -> Unit)?,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onSelectClip: (String) -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
    onMakeMovie: () -> Unit,
    onDismiss: () -> Unit
) {
    var isDeleteConfirmationVisible by remember(clip.id) { mutableStateOf(false) }
    var replayRequestKey by remember { mutableIntStateOf(0) }
    var playbackMode by remember {
        mutableStateOf(
            if (autoplayOnLoad) ClipPreviewPlaybackMode.AutoNext
            else if (autoAdvanceEnabled) ClipPreviewPlaybackMode.AutoNext
            else DefaultClipPreviewPlaybackMode
        )
    }
    val previewThumbnailState = rememberLazyListState()
    val hasPlayableMedia = clip.mediaKind == ClipMediaKind.Video ||
        clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion
    val previewBackgroundColor = if (hasPlayableMedia) Color.Black else palette.solidPanel
    val previewText = if (hasPlayableMedia) Color.White else palette.text
    val previewSubText = if (hasPlayableMedia) Color.White.copy(alpha = 0.72f) else palette.subText
    LaunchedEffect(Unit) {
        if (autoplayOnLoad) onAutoplayConsumed()
    }
    LaunchedEffect(clip.id, playbackMode, hasPlayableMedia, position, total) {
        if (!hasPlayableMedia && playbackMode == ClipPreviewPlaybackMode.AutoNext) {
            delay((clip.durationSeconds.coerceAtLeast(0.1) * 1000).toLong())
            when (
                nextClipIndexOnPlaybackEnded(
                    mode = playbackMode,
                    currentIndex = position - 1,
                    clipCount = total
                )
            ) {
                0 -> onFirst?.invoke()
                null -> Unit
                else -> onNext?.invoke()
            }
        }
    }
    LaunchedEffect(clip.id, clips) {
        val selectedIndex = clips.indexOfFirst { it.id == clip.id }
        if (selectedIndex >= 0) previewThumbnailState.animateScrollToItem(selectedIndex)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        FullScreenDialogSystemBars(previewBackgroundColor)
        Surface(color = previewBackgroundColor) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (hasPlayableMedia) Modifier.background(Color.Black)
                        else Modifier.background(palette.background)
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.clickable(onClickLabel = "편집 닫기", onClick = onDismiss)) {
                        HanClipBrandCapsule(palette)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                replayRequestKey += 1
                                clips.firstOrNull()?.let { onSelectClip(it.id) }
                            }
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "편집 초기화", tint = previewText)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "닫기", tint = previewText)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = previewSubText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = if (clip.isVideoSegmentParent) "모클립 편집" else "편집",
                        color = previewText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ClipPreviewPlayer(
                    clip = clip,
                    playbackMode = playbackMode,
                    replayRequestKey = replayRequestKey,
                    onPlaybackEnded = {
                        when (
                            nextClipIndexOnPlaybackEnded(
                                mode = playbackMode,
                                currentIndex = position - 1,
                                clipCount = total
                            )
                        ) {
                            0 -> onFirst?.invoke()
                            null -> Unit
                            else -> onNext?.invoke()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (hasPlayableMedia) Modifier.weight(1f)
                            else Modifier.aspectRatio(1f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (hasPlayableMedia) Color.Black
                            else palette.secondary.copy(alpha = 0.12f)
                        )
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    state = previewThumbnailState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(clips, key = { _, item -> item.id }) { index, item ->
                        val selected = item.id == clip.id
                        Box(
                            modifier = Modifier
                                .size(if (selected) 58.dp else 54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) palette.primary else Color.White.copy(alpha = 0.42f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectClip(item.id) }
                                .semantics {
                                    contentDescription = "${index + 1}번째 클립"
                                    this.selected = selected
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            ClipThumbnail(item, Modifier.matchParentSize())
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(3.dp)
                                    .background(palette.primary.copy(alpha = 0.82f), CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.solidPanel.copy(alpha = 0.98f),
                    border = BorderStroke(1.dp, palette.border.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = clipTitle(clip, null),
                                    color = palette.text,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = clipModeText(clip, childSegmentCount),
                                    color = palette.subText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (onEdit != null) {
                                OutlinedButton(onClick = onEdit) {
                                    Text("편집")
                                }
                            }
                            OutlinedButton(onClick = { isDeleteConfirmationVisible = true }) {
                                Text("삭제", color = Color(0xFFE45D42))
                            }
                            Button(
                                onClick = onMakeMovie,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("만들기")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = playbackMode == ClipPreviewPlaybackMode.AutoNext,
                                onClick = {
                                    val nextMode = if (playbackMode == ClipPreviewPlaybackMode.AutoNext) {
                                        ClipPreviewPlaybackMode.Stop
                                    } else {
                                        ClipPreviewPlaybackMode.AutoNext
                                    }
                                    playbackMode = nextMode
                                    onAutoAdvanceChange(nextMode == ClipPreviewPlaybackMode.AutoNext)
                                },
                                label = { Text("자동 진행") }
                            )
                            if (hasPlayableMedia) {
                                clipPreviewPeakText(clip)?.let { peakText ->
                                    Text(
                                        text = peakText,
                                        modifier = Modifier.weight(1f),
                                        color = palette.subText,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (hasPlayableMedia) {
                            if (
                                clip.audioWaveform.isNotEmpty() ||
                                clip.audioPeakTimesSeconds.isNotEmpty() ||
                                clip.audioPeakTimeSeconds != null
                            ) {
                                ClipPreviewWaveform(clip = clip, palette = palette)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(onClick = { onFirst?.invoke() }, enabled = onFirst != null) {
                                Text("처음")
                            }
                            OutlinedButton(onClick = { onPrevious?.invoke() }, enabled = onPrevious != null) {
                                Text("이전")
                            }
                            Text(
                                text = "$position/$total",
                                modifier = Modifier.weight(1f),
                                color = palette.subText,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            OutlinedButton(onClick = { onNext?.invoke() }, enabled = onNext != null) {
                                Text("다음")
                            }
                        }
                    }
                }
            }
            if (isDeleteConfirmationVisible) {
                AlertDialog(
                    onDismissRequest = { isDeleteConfirmationVisible = false },
                    title = { Text("HanClip") },
                    text = { Text("이 클립을 완성본에서 제외할까요? 원본 미디어는 삭제되지 않습니다.") },
                    dismissButton = {
                        OutlinedButton(onClick = { isDeleteConfirmationVisible = false }) {
                            Text("취소")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isDeleteConfirmationVisible = false
                                onDelete()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE45D42),
                                contentColor = Color.White
                            )
                        ) {
                            Text("제외")
                        }
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ClipPreviewPlayer(
    clip: ClipItem,
    playbackMode: ClipPreviewPlaybackMode,
    replayRequestKey: Int,
    onPlaybackEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSample = clip.sourceUri.scheme == "sample"
    val hasPlayableMedia = clip.mediaKind == ClipMediaKind.Video ||
        clip.livePhotoMode == com.hanclip.android.core.model.LivePhotoMode.Motion
    val mediaItem = remember(clip.id, clip.sourceUri, clip.trimStartSeconds, clip.durationSeconds) {
        if (isSample || !hasPlayableMedia) {
            null
        } else {
            val sourceDuration = clip.sourceDurationSeconds ?: clip.durationSeconds
            val startSeconds = if (clip.isVideoSegmentParent) 0.0 else clip.trimStartSeconds
            val endSeconds = if (clip.isVideoSegmentParent) {
                sourceDuration
            } else {
                (clip.trimStartSeconds + clip.durationSeconds).coerceAtMost(sourceDuration)
            }
            MediaItem.Builder()
                .setUri(clip.sourceUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs((startSeconds * 1000).toLong().coerceAtLeast(0))
                        .setEndPositionMs((endSeconds * 1000).toLong().coerceAtLeast(100))
                        .build()
                )
                .build()
        }
    }
    val player = remember(mediaItem) {
        mediaItem?.let {
            ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                setMediaItem(it)
                prepare()
            }
        }
    }

    LaunchedEffect(player, playbackMode) {
        player?.repeatMode = if (playbackMode == ClipPreviewPlaybackMode.Loop) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }
    LaunchedEffect(player, replayRequestKey) {
        if (replayRequestKey > 0) {
            player?.seekTo(0L)
            player?.play()
        }
    }

    DisposableEffect(player, onPlaybackEnded) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onPlaybackEnded()
            }
        }
        player?.addListener(listener)
        onDispose {
            player?.removeListener(listener)
            player?.release()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (player != null) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.matchParentSize()
            )
        } else if (!hasPlayableMedia) {
            ClipThumbnail(
                clip = clip,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
                overlayAlpha = 0f
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(66.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("실제 영상을 선택하면 미리볼 수 있습니다.", color = Color.White)
            }
        }
    }
}

private fun clipPreviewPeakText(clip: ClipItem): String? {
    val peak = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull() ?: return null
    val count = clip.audioPeakTimesSeconds.size.coerceAtLeast(1)
    return "주요 타격점 ${formatClipSeconds(peak)} · 후보 ${count}개"
}

@Composable
private fun ClipPreviewWaveform(
    clip: ClipItem,
    palette: HanClipPalette
) {
    val bars = clip.audioWaveform.ifEmpty { List(48) { 0.18 } }
    val peaks = clip.audioPeakTimesSeconds.ifEmpty { listOfNotNull(clip.audioPeakTimeSeconds) }
    val sourceDuration = (clip.sourceDurationSeconds ?: clip.durationSeconds).coerceAtLeast(0.1)
    val selectedStart = if (clip.isVideoSegmentParent) 0.0 else clip.trimStartSeconds
    val selectedDuration = if (clip.isVideoSegmentParent) sourceDuration else clip.durationSeconds
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .semantics {
                contentDescription =
                    "오디오 파형, 선택 구간 ${formatClipSeconds(selectedStart)}부터 ${formatClipSeconds(selectedStart + selectedDuration)}, 타격점 ${peaks.size}개"
            }
    ) {
        val selectedStartX = (selectedStart / sourceDuration).toFloat() * size.width
        val selectedEndX = ((selectedStart + selectedDuration) / sourceDuration)
            .coerceAtMost(1.0)
            .toFloat() * size.width
        drawRoundRect(
            color = palette.primary.copy(alpha = 0.12f),
            topLeft = Offset(selectedStartX, 0f),
            size = androidx.compose.ui.geometry.Size(
                width = (selectedEndX - selectedStartX).coerceAtLeast(2f),
                height = size.height
            )
        )
        val gap = size.width / bars.size.coerceAtLeast(1)
        bars.forEachIndexed { index, value ->
            val x = gap * index + gap / 2f
            val normalized = value.coerceIn(0.04, 1.0).toFloat()
            val barHeight = size.height * (0.18f + normalized * 0.72f)
            drawLine(
                color = if (x in selectedStartX..selectedEndX) {
                    palette.primary
                } else {
                    palette.subText.copy(alpha = 0.34f)
                },
                start = Offset(x, (size.height - barHeight) / 2f),
                end = Offset(x, (size.height + barHeight) / 2f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
        peaks
            .filter { it.isFinite() && it in 0.0..sourceDuration }
            .take(12)
            .forEach { peak ->
                val x = (peak / sourceDuration).toFloat() * size.width
                drawLine(
                    color = Color(0xFFE45D42),
                    start = Offset(x, 2f),
                    end = Offset(x, size.height - 2f),
                    strokeWidth = 2.8f,
                    cap = StrokeCap.Round
                )
            }
    }
}

private fun clipPreviewSubtitle(clip: ClipItem, childSegmentCount: Int): String {
    return if (clip.isVideoSegmentParent) {
        "원본 ${formatClipSeconds(clip.sourceDurationSeconds ?: clip.durationSeconds)} · 자동 컷 ${childSegmentCount}개"
    } else if (clip.mediaKind == ClipMediaKind.Video) {
        "구간 ${formatClipSeconds(clip.trimStartSeconds)} - ${formatClipSeconds(clip.trimEndSeconds)} · 클립 ${formatClipSeconds(clip.durationSeconds)}"
    } else if (clip.mediaKind == ClipMediaKind.LivePhoto) {
        "모션포토 ${formatClipSeconds(clip.durationSeconds)} · 완성본에 이 길이로 들어갑니다"
    } else {
        "사진 ${formatClipSeconds(clip.durationSeconds)} · 완성본에 이 길이로 들어갑니다"
    }
}

@Composable
private fun ClipControlPill(
    palette: HanClipPalette,
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(30.dp)
            .defaultMinSize(minWidth = 56.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (active) palette.chip else palette.panel,
        contentColor = if (active) palette.primary else palette.text,
        border = BorderStroke(1.dp, if (active) palette.primary else palette.border),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Text(
                text = text,
                color = if (active) palette.primary else palette.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

private fun clipRowFill(clip: ClipItem, palette: HanClipPalette): Color {
    return when {
        clip.isVideoSegmentParent -> palette.chip
        clip.isHiddenVideoSegmentChild -> palette.chip.copy(alpha = 0.42f)
        clip.isSimilarPhotoGroupChild -> palette.chip.copy(alpha = 0.58f)
        clip.isVideoSegmentChild -> palette.chip.copy(alpha = 0.72f)
        else -> palette.panel
    }
}

private fun clipRowBorder(clip: ClipItem, palette: HanClipPalette): Color {
    return when {
        clip.isVideoSegmentParent -> palette.primary.copy(alpha = 0.45f)
        clip.isHiddenVideoSegmentChild -> palette.border.copy(alpha = 0.55f)
        clip.isSimilarPhotoGroupChild -> palette.secondary.copy(alpha = 0.28f)
        clip.isVideoSegmentChild -> palette.secondary.copy(alpha = 0.35f)
        else -> palette.border
    }
}

private fun clipFallbackBrush(clip: ClipItem, palette: HanClipPalette): Brush {
    return Brush.verticalGradient(
        if (clip.mediaKind == ClipMediaKind.Video) {
            listOf(palette.secondary, palette.primary)
        } else {
            listOf(palette.primary.copy(alpha = 0.76f), palette.secondary)
        }
    )
}

private fun clipTitle(clip: ClipItem, position: Int?): String {
    if (clip.isVideoSegmentParent) return "원본 영상 보관"
    if (clip.isVideoSegmentChild) {
        if (clip.isHiddenVideoSegmentChild) return "선택하지 않은 자클립"
        return position?.let { "완성본 자동 컷 ${it}번" } ?: "완성본 자동 컷"
    }
    if (clip.isHiddenSimilarPhotoGroupMember) return "완성본 제외 후보"
    if (clip.isSimilarPhotoGroupChild) return "완성본 포함 사진"
    if (clip.similarPhotoGroupCount > 1) return "완성본 대표 사진"
    return when (clip.mediaKind) {
        ClipMediaKind.Video -> "영상"
        ClipMediaKind.Photo -> "사진"
        ClipMediaKind.LivePhoto -> "모션포토"
    }
}

private fun clipPrimaryTimeText(clip: ClipItem, childSegmentCount: Int): String {
    val source = clip.sourceDurationSeconds ?: clip.durationSeconds
    return if (clip.isVideoSegmentParent) {
        "원본 ${formatClipSeconds(source)} · 완성본 자동 컷 ${childSegmentCount}개"
    } else if (clip.isVideoSegmentChild) {
        val impactText = clip.audioPeakTimeSeconds
            ?.let { "타격 ${formatClipSeconds(it)} · " }
            .orEmpty()
        "${impactText}완성본 ${formatClipSeconds(clip.durationSeconds)} / 원본 ${formatClipSeconds(source)}"
    } else if (clip.mediaKind == ClipMediaKind.Video) {
        "클립 ${formatClipSeconds(clip.durationSeconds)} / 원본 ${formatClipSeconds(source)}"
    } else if (clip.mediaKind == ClipMediaKind.LivePhoto) {
        "모션포토 ${formatClipSeconds(clip.durationSeconds)}"
    } else {
        "사진 ${formatClipSeconds(clip.durationSeconds)}"
    }
}

private fun clipModeText(clip: ClipItem, childSegmentCount: Int): String {
    return when {
        clip.isVideoSegmentParent -> "원본은 보관하고 타격점 기준 자동 컷만 완성본에 넣습니다"
        clip.isHiddenVideoSegmentChild -> "타격점 중심 구간 · 완성본 제외 중 · 사용하면 다시 포함됩니다"
        clip.isVideoSegmentChild -> "타격점 중심 구간 · 완성본 번호순 포함"
        clip.isHiddenSimilarPhotoGroupMember -> "비슷한 사진 묶음에서 제외 중 · 사용하면 완성본에 포함됩니다"
        clip.isSimilarPhotoGroupChild -> "비슷한 사진 묶음에서 완성본에 포함 중"
        clip.similarPhotoGroupCount > 1 -> "비슷한 사진 ${clip.similarPhotoGroupCount}장 중 이 사진만 완성본에 넣습니다"
        clip.videoSegmentMode == VideoSegmentMode.Multiple -> "타격점 후보 ${clip.audioPeakTimesSeconds.size}개"
        else -> "단일 구간"
    }
}

private fun clipInfoChips(clip: ClipItem, childSegmentCount: Int): List<String> {
    val resolution = "${clip.sourceWidth}x${clip.sourceHeight}".takeIf {
        clip.sourceWidth > 1 && clip.sourceHeight > 1
    }
    val source = clip.sourceDurationSeconds ?: clip.durationSeconds
    return buildList {
        if (clip.isRenderableClip) {
            add("완성 ${formatClipSeconds(clip.durationSeconds)}")
        }
        if (clip.mediaKind == ClipMediaKind.Video) {
            add("원본 ${formatClipSeconds(source)}")
        }
        add(
            when (clip.mediaKind) {
                ClipMediaKind.Video -> "영상"
                ClipMediaKind.Photo -> "사진"
                ClipMediaKind.LivePhoto -> clip.livePhotoMode.title
            }
        )
        resolution?.let { add(it) }
        if (clip.mediaKind == ClipMediaKind.Video && !clip.isVideoSegmentParent) {
            add("시작 ${formatClipSeconds(clip.trimStartSeconds)}")
        }
        if (clip.isVideoSegmentChild) {
            add(if (clip.isVideoSegmentSelected) "완성본 포함" else "완성본 제외")
        }
        if (clip.isVideoSegmentParent) {
            add("원본 보관")
        }
        if (clip.isHiddenSimilarPhotoGroupMember) {
            add("완성본 제외")
        }
        impactChipText(clip)?.let {
            add(it)
        }
        if (clip.isVideoSegmentParent && childSegmentCount > 0) {
            add("자동 ${childSegmentCount}컷")
        }
        if (clip.similarPhotoGroupCount > 1) {
            add(if (clip.isHiddenSimilarPhotoGroupMember) "후보 묶음" else "대표 선택")
        }
    }
}

private fun impactChipText(clip: ClipItem): String? {
    val primary = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull()
        ?: return null
    val count = clip.audioPeakTimesSeconds.size.takeIf { it > 0 } ?: 1
    return if (count > 1) {
        "타격 ${formatClipSeconds(primary)} · 후보 ${count}"
    } else {
        "타격 ${formatClipSeconds(primary)}"
    }
}

@Composable
private fun ClipInfoChip(
    palette: HanClipPalette,
    text: String,
    active: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) palette.primary.copy(alpha = 0.08f) else palette.chip.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.22f) else palette.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = if (active) palette.primary else palette.subText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun formatClipSeconds(seconds: Double): String {
    val totalTenths = (seconds * 10).roundToInt().coerceAtLeast(0)
    val minutes = totalTenths / 600
    val remaining = (totalTenths % 600) / 10.0
    return "%d:%04.1f".format(minutes, remaining)
}

@Composable
private fun ClipThumbnail(
    clip: ClipItem,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    overlayAlpha: Float = 0.18f
) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, clip.thumbnailUri, clip.mediaKind) {
        val uri = clip.thumbnailUri ?: clip.sourceUri
        val thumbnailKind = if (clip.thumbnailUri != null) ClipMediaKind.Photo else clip.mediaKind
        value = if (uri.scheme == "sample") {
            null
        } else {
            MediaImportReader.loadThumbnailBitmap(context, uri, thumbnailKind)
        }
    }

    thumbnail?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
        if (overlayAlpha > 0f) {
            Box(
                modifier = modifier
                    .background(Color.Black.copy(alpha = overlayAlpha))
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GlobalTimePanel(
    defaultDuration: Double,
    hasVideoClips: Boolean,
    palette: HanClipPalette,
    onSetDuration: (Double) -> Unit,
    onApplyAll: () -> Unit,
    onSelectFullRange: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("완성본 기본 리듬", fontWeight = FontWeight.Bold, color = palette.text)
                    Text(
                        "사진, 단일 영상, 자동 컷의 기본 길이를 한 번에 맞춥니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.subText
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            onSetDuration(steppedDefaultDuration(defaultDuration, increase = false))
                        },
                        enabled = defaultDuration > 0.1,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "전체 시간 줄이기", tint = palette.secondary)
                    }
                    Text("%.1f초".format(defaultDuration), fontWeight = FontWeight.Bold, color = palette.primary)
                    IconButton(
                        onClick = {
                            onSetDuration(steppedDefaultDuration(defaultDuration, increase = true))
                        },
                        enabled = defaultDuration < 30.0,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "전체 시간 늘리기", tint = palette.secondary)
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.5, 2.0, 3.0, 4.0, 5.0, 6.0).forEach { seconds ->
                    FilterChip(
                        selected = defaultDuration == seconds,
                        onClick = { onSetDuration(seconds) },
                        label = { Text("%.1f초".format(seconds)) },
                        colors = clearFilterChipColors(palette),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = defaultDuration == seconds,
                            borderColor = palette.border,
                            selectedBorderColor = palette.primary
                        )
                    )
                }
            }
            Button(
                onClick = onApplyAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("모든 클립 기본 %.1f초 적용".format(defaultDuration))
            }
            OutlinedButton(
                onClick = onSelectFullRange,
                enabled = hasVideoClips,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.text,
                    disabledContainerColor = palette.chip,
                    disabledContentColor = palette.subText
                )
            ) {
                Icon(Icons.Outlined.MovieCreation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("영상은 원본 전체 길이로 사용")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomEmptyEditorBar(
    modifier: Modifier,
    palette: HanClipPalette,
    onAdd: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.solidPanel,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.size(62.dp),
                contentPadding = PaddingValues(0.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.secondary
                ),
                border = BorderStroke(1.dp, palette.border)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "미디어 추가", modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(16.dp))
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.size(62.dp),
                contentPadding = PaddingValues(0.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.solidPanel,
                    contentColor = palette.secondary
                ),
                border = BorderStroke(1.dp, palette.border)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "닫기", modifier = Modifier.size(34.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomMakeBar(
    modifier: Modifier,
    palette: HanClipPalette,
    isExporting: Boolean,
    clipCount: Int,
    photoCount: Int,
    videoCount: Int,
    totalSeconds: Double,
    qualityTitle: String,
    hasTextOverlay: Boolean,
    hasLogoOverlay: Boolean,
    hasMusic: Boolean,
    selectedRatio: OutputAspectRatio?,
    onSelectRatio: (OutputAspectRatio?) -> Unit,
    onClose: () -> Unit,
    onMakeMovie: () -> Unit
) {
    var isRatioPickerVisible by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = palette.solidPanel.copy(alpha = 0.98f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isRatioPickerVisible) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedRatio == null,
                            onClick = { onSelectRatio(null); isRatioPickerVisible = false },
                            label = { Text("첫 사진") },
                            colors = clearFilterChipColors(palette)
                        )
                    }
                    itemsIndexed(OutputAspectRatio.entries) { _, ratio ->
                        FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { onSelectRatio(ratio); isRatioPickerVisible = false },
                            label = { Text(ratio.title) },
                            colors = clearFilterChipColors(palette)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "닫기", modifier = Modifier.size(27.dp))
                }
                OutlinedButton(
                    onClick = { isRatioPickerVisible = !isRatioPickerVisible },
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    border = BorderStroke(1.dp, palette.border),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = palette.chip, contentColor = palette.primary)
                ) {
                    if (selectedRatio == null) {
                        Text(
                            text = "첫\n사진",
                            color = palette.primary,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = selectedRatio.title,
                            color = palette.primary,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Button(
                    onClick = onMakeMovie,
                    enabled = !isExporting && clipCount > 0,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White,
                        disabledContainerColor = palette.chip,
                        disabledContentColor = palette.subText
                    )
                ) {
                    Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        when {
                            isExporting -> "만드는 중..."
                            clipCount == 0 -> "사진/영상 선택 필요"
                            else -> "${formatSummaryDuration(totalSeconds)} 만들기"
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun BottomMakeSummaryChips(
    clipCount: Int,
    photoCount: Int,
    videoCount: Int,
    totalSeconds: Double,
    qualityTitle: String,
    hasTextOverlay: Boolean,
    hasLogoOverlay: Boolean,
    hasMusic: Boolean,
    palette: HanClipPalette
) {
    if (clipCount == 0) {
        Text(
            text = "기본 사진첩에서 골프 사진/영상을 고르면 길이, 품질, 자막, 음악을 바로 확인합니다",
            color = palette.subText,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BottomMakeSummaryPill(mediaCountSummary(photoCount, videoCount, clipCount), palette, active = true)
        BottomMakeSummaryPill(formatSummaryDuration(totalSeconds), palette, active = true)
        BottomMakeSummaryPill("번호순 연결", palette, active = true)
        if (videoCount > 0) {
            BottomMakeSummaryPill("타격점 자동 컷", palette, active = true)
        }
        BottomMakeSummaryPill(qualityTitle, palette, active = true)
        BottomMakeSummaryPill(OutputQualityPreset.ExportFormatTitle, palette, active = true)
        BottomMakeSummaryPill(overlayStatusText(hasTextOverlay, hasLogoOverlay), palette, active = hasTextOverlay || hasLogoOverlay)
        BottomMakeSummaryPill(if (hasMusic) "음악 켬" else "음악 없음", palette, active = hasMusic)
        BottomMakeSummaryPill("시사회 확인", palette, active = true)
        BottomMakeSummaryPill("HanClip 앨범 저장", palette, active = true)
    }
}

@Composable
private fun BottomMakeSummaryPill(
    text: String,
    palette: HanClipPalette,
    active: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) palette.chip else palette.panel,
        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.42f) else palette.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            color = if (active) palette.text else palette.subText,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun overlayStatusText(hasTextOverlay: Boolean, hasLogoOverlay: Boolean): String {
    return when {
        hasTextOverlay && hasLogoOverlay -> "자막/로고 켬"
        hasTextOverlay -> "자막 켬"
        hasLogoOverlay -> "HanClip 로고"
        else -> "자막/로고 꺼짐"
    }
}

private tailrec fun Context.findEditorActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findEditorActivity()
    else -> null
}
