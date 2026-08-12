package com.hanclip.android.feature.aishot

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.TextureView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hanclip.android.R
import com.hanclip.android.core.safety.orderedCaptureValues
import com.hanclip.android.core.theme.HanClipSystemBars
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.ceil
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private enum class ShotSensitivity(val title: String) {
    Auto("자동"),
    Quiet("조용함"),
    Normal("일반"),
    Loud("시끄러움")
}

private enum class AiShotCapturePhase(val title: String) {
    Detecting("감지 중"),
    Detected("감지 됨"),
    Saving("저장 중")
}

private data class CapturedShot(val sequence: Long, val uri: Uri)

private enum class ShotLength(
    val title: String,
    val beforeSeconds: Double,
    val afterSeconds: Double
) {
    Short("짧게", 1.5, 1.5),
    Normal("일반", 2.0, 3.0),
    Long("길게", 5.0, 5.0);

    val fullSeconds: Double
        get() = beforeSeconds + afterSeconds

    val recordingSeconds: Long
        get() = ceil(fullSeconds).toLong()

    val timingDescription: String
        get() = "앞 ${durationText(beforeSeconds)}초 · 뒤 ${durationText(afterSeconds)}초"

    val totalDurationDescription: String
        get() = "${durationText(fullSeconds)}초"

    private fun durationText(seconds: Double): String {
        return if (seconds % 1.0 == 0.0) {
            seconds.toInt().toString()
        } else {
            "%.1f".format(seconds)
        }
    }
}

private enum class ZoomPreset(val title: String, val ratio: Double) {
    Half(".5", 0.5),
    One("1", 1.0),
    Two("2", 2.0),
    Four("4", 4.0),
    Eight("8", 8.0)
}

internal fun zoomRatioToControlPosition(ratio: Float, minimum: Float, maximum: Float): Float {
    if (minimum <= 0f || maximum <= minimum) return 0f
    val safeRatio = ratio.coerceIn(minimum, maximum)
    return ((ln(safeRatio) - ln(minimum)) / (ln(maximum) - ln(minimum))).coerceIn(0f, 1f)
}

internal fun zoomControlPositionToRatio(position: Float, minimum: Float, maximum: Float): Float {
    if (minimum <= 0f || maximum <= minimum) return minimum
    val logRatio = ln(minimum) + position.coerceIn(0f, 1f) * (ln(maximum) - ln(minimum))
    return exp(logRatio).coerceIn(minimum, maximum)
}

internal fun zoomRatioAfterHorizontalDrag(
    startRatio: Float,
    dragDistancePx: Float,
    octaveWidthPx: Float,
    minimum: Float,
    maximum: Float
): Float {
    if (octaveWidthPx <= 0f) return startRatio.coerceIn(minimum, maximum)
    val octaveOffset = -dragDistancePx / octaveWidthPx
    return (startRatio * 2f.pow(octaveOffset)).coerceIn(minimum, maximum)
}

private fun zoomRatioTitle(ratio: Float): String {
    if (abs(ratio - 0.5f) < 0.05f) return ".5x"
    val rounded = ratio.toInt()
    return if (abs(ratio - rounded) < 0.05f) "${rounded}x" else "%.1fx".format(ratio)
}

private object AiShotModelInfo {
    const val Version = "0.2.1"
    const val Title = "798 영상 보정 Ai"
    const val Summary = "소리의 피크보다 타격 뒤 반응과 화면 변화를 더 차분하게 함께 봅니다."
}

private object AiShotPreferenceStore {
    private const val PreferencesName = "hanclip_aishot_preferences"
    private const val SensitivityKey = "sensitivity"
    private const val ShotLengthKey = "shotLength"
    private const val NextShotLengthEdgeKey = "nextShotLengthEdge"

    fun loadSensitivity(context: Context): ShotSensitivity {
        val raw = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(SensitivityKey, ShotSensitivity.Auto.name)
        return runCatching { enumValueOf<ShotSensitivity>(raw.orEmpty()) }
            .getOrDefault(ShotSensitivity.Auto)
    }

    fun saveSensitivity(context: Context, sensitivity: ShotSensitivity) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(SensitivityKey, sensitivity.name)
            .apply()
    }

    fun loadShotLength(context: Context, projectId: String): ShotLength {
        val raw = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString("$ShotLengthKey.$projectId", ShotLength.Normal.name)
        return runCatching { enumValueOf<ShotLength>(raw.orEmpty()) }
            .getOrDefault(ShotLength.Normal)
    }

    fun saveShotLength(context: Context, projectId: String, shotLength: ShotLength) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString("$ShotLengthKey.$projectId", shotLength.name)
            .apply()
    }

    fun loadNextShotLengthEdge(context: Context, projectId: String): ShotLength {
        val raw = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString("$NextShotLengthEdgeKey.$projectId", ShotLength.Long.name)
        return runCatching { enumValueOf<ShotLength>(raw.orEmpty()) }
            .getOrDefault(ShotLength.Long)
            .takeIf { it != ShotLength.Normal }
            ?: ShotLength.Long
    }

    fun saveNextShotLengthEdge(context: Context, projectId: String, shotLength: ShotLength) {
        val safeValue = if (shotLength == ShotLength.Short) ShotLength.Short else ShotLength.Long
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString("$NextShotLengthEdgeKey.$projectId", safeValue.name)
            .apply()
    }
}

@Composable
fun AiShotRoute(
    projectId: String,
    onClose: () -> Unit,
    onOpenEditor: (List<Uri>) -> Unit
) {
    HanClipSystemBars(Color.Black)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val visualAnalyzer = remember { RealtimeVisualAnalyzer() }

    var hasPermissions by remember {
        mutableStateOf(context.hasAiShotPermissions())
    }
    var isLifecycleResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermissions = grants[Manifest.permission.CAMERA] == true &&
            grants[Manifest.permission.RECORD_AUDIO] == true
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    hasPermissions = context.hasAiShotPermissions()
                    isLifecycleResumed = true
                }
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> isLifecycleResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraPreview by remember { mutableStateOf<Preview?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var recordingStartedAtMillis by remember { mutableLongStateOf(0L) }
    var recordingDurationNanos by remember { mutableLongStateOf(0L) }
    var recordingDurationObservedAtMillis by remember { mutableLongStateOf(0L) }
    var hasCleanedAbandonedSessionFiles by remember { mutableStateOf(false) }
    var isRollingRecordingActive by remember { mutableStateOf(false) }
    var triggerTimeSeconds by remember { mutableStateOf<Double?>(null) }
    var activeCaptureSequence by remember { mutableStateOf<Long?>(null) }
    var nextCaptureSequence by remember { mutableLongStateOf(0L) }
    var activeShotLength by remember { mutableStateOf<ShotLength?>(null) }
    var discardCurrentRecording by remember { mutableStateOf(false) }
    var pendingSaveCount by remember { mutableIntStateOf(0) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var sensitivity by remember { mutableStateOf(AiShotPreferenceStore.loadSensitivity(context)) }
    var shotLength by remember(projectId) {
        mutableStateOf(AiShotPreferenceStore.loadShotLength(context, projectId))
    }
    var nextShotLengthEdge by remember(projectId) {
        mutableStateOf(AiShotPreferenceStore.loadNextShotLengthEdge(context, projectId))
    }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var minimumZoomRatio by remember { mutableFloatStateOf(1f) }
    var maximumZoomRatio by remember { mutableFloatStateOf(8f) }
    var level by remember { mutableDoubleStateOf(0.0) }
    var statusText by remember { mutableStateOf("스윙 감지 대기") }
    var savedCount by remember { mutableIntStateOf(0) }
    var capturedShots by remember { mutableStateOf<List<CapturedShot>>(emptyList()) }
    val capturedUris = orderedCaptureValues(capturedShots.map { it.sequence to it.uri })
    var didHandOffCapturedUris by remember { mutableStateOf(false) }
    var recordingRemainingSeconds by remember { mutableStateOf(0L) }
    var activeRecordingSeconds by remember { mutableStateOf(shotLength.recordingSeconds) }
    var shotLengthNotice by remember { mutableStateOf<ShotLength?>(null) }
    var capturePhase by remember { mutableStateOf(AiShotCapturePhase.Detecting) }

    @SuppressLint("MissingPermission")
    fun triggerClip(reason: String) {
        val activeRecording = recording ?: return
        if (!isRollingRecordingActive || triggerTimeSeconds != null) return
        val now = SystemClock.elapsedRealtime()
        val statusAgeSeconds = ((now - recordingDurationObservedAtMillis).coerceIn(0L, 250L)) / 1000.0
        val elapsedSeconds = recordingDurationNanos / 1_000_000_000.0 + statusAgeSeconds
        if (elapsedSeconds < shotLength.beforeSeconds) {
            statusText = "준비 중"
            return
        }
        val timing = shotLength
        val sequence = nextCaptureSequence
        nextCaptureSequence += 1L
        triggerTimeSeconds = elapsedSeconds
        activeCaptureSequence = sequence
        activeShotLength = timing
        statusText = reason
        capturePhase = AiShotCapturePhase.Detected
        activeRecordingSeconds = timing.recordingSeconds
        recordingRemainingSeconds = ceil(timing.afterSeconds).toLong()
        scope.launch {
            val captureEndMillis = SystemClock.elapsedRealtime() + (timing.afterSeconds * 1000).toLong()
            delay(600L)
            if (activeCaptureSequence == sequence && triggerTimeSeconds != null) {
                capturePhase = AiShotCapturePhase.Saving
            }
            val stopAtMillis = captureEndMillis + 350L
            while (recording == activeRecording && SystemClock.elapsedRealtime() < stopAtMillis) {
                delay(100L)
                val remainingMillis = (captureEndMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                recordingRemainingSeconds = ceil(remainingMillis / 1000.0).toLong()
            }
            if (recording == activeRecording) activeRecording.stop()
        }
    }

    @SuppressLint("MissingPermission")
    fun startRollingRecording() {
        if (!hasPermissions || recording != null) return
        val capture = videoCapture ?: return
        val outputDirectory = context.cacheDir.resolve("aishot").apply { mkdirs() }
        if (!hasCleanedAbandonedSessionFiles) {
            AiShotVideoTrimmer.cleanupAbandonedSessionFiles(outputDirectory)
            hasCleanedAbandonedSessionFiles = true
        }
        val outputFile = File(
            outputDirectory,
            "aishot-buffer-${System.currentTimeMillis()}.mp4"
        )
        val options = FileOutputOptions.Builder(outputFile).build()
        discardCurrentRecording = false
        recordingStartedAtMillis = 0L
        recordingDurationNanos = 0L
        recordingDurationObservedAtMillis = 0L
        isRollingRecordingActive = false
        triggerTimeSeconds = null
        activeShotLength = null
        statusText = "준비 중"
        capturePhase = AiShotCapturePhase.Detecting
        val pending = capture.output
            .prepareRecording(context, options)
            .withAudioEnabled()
        val startedRecording = pending.start(cameraExecutor) { event ->
            recordingDurationNanos = event.recordingStats.recordedDurationNanos
            recordingDurationObservedAtMillis = SystemClock.elapsedRealtime()
            if (event is VideoRecordEvent.Start) {
                recordingStartedAtMillis = recordingDurationObservedAtMillis
                isRollingRecordingActive = true
            }
            if (event is VideoRecordEvent.Finalize) {
                val finalizedTrigger = triggerTimeSeconds
                val finalizedTiming = activeShotLength
                val finalizedSequence = activeCaptureSequence
                val shouldDiscard = discardCurrentRecording
                recording = null
                isRollingRecordingActive = false
                triggerTimeSeconds = null
                activeCaptureSequence = null
                activeShotLength = null
                recordingRemainingSeconds = 0L
                activeRecordingSeconds = shotLength.recordingSeconds
                if (event.hasError() || shouldDiscard || finalizedTrigger == null ||
                    finalizedTiming == null || finalizedSequence == null
                ) {
                    outputFile.delete()
                    if (event.hasError() && !shouldDiscard) statusText = "클립 저장 실패"
                    return@start
                }
                pendingSaveCount += 1
                statusText = "클립 저장 중"
                scope.launch {
                    val destination = File(
                        outputFile.parentFile,
                        "aishot-$finalizedSequence-${System.currentTimeMillis()}.mp4"
                    )
                    val result = runCatching {
                        AiShotVideoTrimmer.trimAroundTrigger(
                            context = context,
                            sourceFile = outputFile,
                            destinationFile = destination,
                            triggerSeconds = finalizedTrigger,
                            beforeSeconds = finalizedTiming.beforeSeconds,
                            afterSeconds = finalizedTiming.afterSeconds
                        )
                    }
                    outputFile.delete()
                    pendingSaveCount = (pendingSaveCount - 1).coerceAtLeast(0)
                    result.onSuccess { uri ->
                        capturedShots = (capturedShots + CapturedShot(finalizedSequence, uri))
                            .sortedBy(CapturedShot::sequence)
                        savedCount = capturedShots.size
                        statusText = "클립 저장 완료"
                    }.onFailure {
                        destination.delete()
                        statusText = "클립 저장 실패"
                    }
                }
            }
        }
        recording = startedRecording
    }

    fun discardAndStopRollingRecording() {
        discardCurrentRecording = true
        recording?.stop()
    }

    LaunchedEffect(isLifecycleResumed) {
        if (!isLifecycleResumed && recording != null) {
            statusText = "촬영 일시 정지"
            discardAndStopRollingRecording()
        }
    }

    LaunchedEffect(recording, isRollingRecordingActive, shotLength) {
        if (recording == null || !isRollingRecordingActive || triggerTimeSeconds != null) {
            return@LaunchedEffect
        }
        val readyAt = recordingStartedAtMillis + (shotLength.beforeSeconds * 1000).toLong()
        val delayMillis = (readyAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        delay(delayMillis)
        if (recording != null && triggerTimeSeconds == null) {
            statusText = "스윙 감지 대기"
        }
    }

    LaunchedEffect(savedCount) {
        if (savedCount == 0) return@LaunchedEffect
        delay(1700L)
        if (recording != null && triggerTimeSeconds == null && statusText == "클립 저장 완료") {
            statusText = "스윙 감지 대기"
        }
    }

    fun selectNextShotLength() {
        val next = when (shotLength) {
            ShotLength.Normal -> nextShotLengthEdge
            ShotLength.Short,
            ShotLength.Long -> ShotLength.Normal
        }
        if (shotLength == ShotLength.Normal) {
            nextShotLengthEdge = if (next == ShotLength.Long) ShotLength.Short else ShotLength.Long
        }
        shotLength = next
        shotLengthNotice = next
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    LaunchedEffect(sensitivity) {
        AiShotPreferenceStore.saveSensitivity(context, sensitivity)
    }

    LaunchedEffect(shotLength) {
        AiShotPreferenceStore.saveShotLength(context, projectId, shotLength)
        activeRecordingSeconds = shotLength.recordingSeconds
    }

    LaunchedEffect(nextShotLengthEdge) {
        AiShotPreferenceStore.saveNextShotLengthEdge(context, projectId, nextShotLengthEdge)
    }

    LaunchedEffect(shotLengthNotice) {
        if (shotLengthNotice == null) return@LaunchedEffect
        delay(1700L)
        shotLengthNotice = null
    }

    LaunchedEffect(previewView, lensFacing, hasPermissions) {
        val view = previewView ?: return@LaunchedEffect
        if (!hasPermissions) return@LaunchedEffect
        discardAndStopRollingRecording()
        val cameraProvider = context.awaitCameraProvider()
        val rotation = view.display?.rotation ?: Surface.ROTATION_0
        val preview = Preview.Builder().setTargetRotation(rotation).build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.FHD))
            .build()
        val capture = VideoCapture.withOutput(recorder).also { it.targetRotation = rotation }
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        runCatching {
            cameraProvider.unbindAll()
            visualAnalyzer.reset()
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            cameraPreview = preview
            videoCapture = capture
        }.onFailure {
            statusText = "카메라 준비 실패"
        }
    }

    DisposableEffect(cameraPreview, videoCapture) {
        val preview = cameraPreview
        val capture = videoCapture
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                if (preview?.targetRotation == rotation && capture?.targetRotation == rotation) return
                preview?.targetRotation = rotation
                capture?.targetRotation = rotation
            }
        }
        if (listener.canDetectOrientation()) listener.enable()
        onDispose { listener.disable() }
    }

    LaunchedEffect(videoCapture, recording, hasPermissions, isLifecycleResumed) {
        if (hasPermissions && isLifecycleResumed && videoCapture != null && recording == null) {
            delay(350L)
            startRollingRecording()
        }
    }

    LaunchedEffect(camera) {
        val boundCamera = camera ?: return@LaunchedEffect
        val zoomState = boundCamera.cameraInfo.zoomState.value
        minimumZoomRatio = zoomState?.minZoomRatio ?: 1f
        maximumZoomRatio = zoomState?.maxZoomRatio ?: 8f
        zoomRatio = zoomRatio.coerceIn(minimumZoomRatio, maximumZoomRatio)
    }

    LaunchedEffect(camera, zoomRatio) {
        val boundCamera = camera ?: return@LaunchedEffect
        val safeRatio = zoomRatio.coerceIn(minimumZoomRatio, maximumZoomRatio)
        runCatching {
            boundCamera.cameraControl.setZoomRatio(safeRatio)
        }
    }

    LaunchedEffect(previewView, camera) {
        val view = previewView ?: return@LaunchedEffect
        if (camera == null) return@LaunchedEffect
        while (isActive) {
            delay(220L)
            val textureView = view.getChildAt(0) as? TextureView ?: continue
            val bitmap = textureView.getBitmap(8, 8) ?: continue
            visualAnalyzer.analyze(bitmap)
            bitmap.recycle()
        }
    }

    LaunchedEffect(
        hasPermissions,
        sensitivity,
        shotLength,
        recording,
        isRollingRecordingActive,
        triggerTimeSeconds
    ) {
        if (!hasPermissions || recording == null || !isRollingRecordingActive || triggerTimeSeconds != null) {
            return@LaunchedEffect
        }
        withContext(Dispatchers.Default) {
            monitorImpactAudio(
                sensitivity = sensitivity,
                beforeSeconds = shotLength.beforeSeconds,
                visualAnalyzer = visualAnalyzer,
                onLevel = {
                    withContext(Dispatchers.Main) {
                        level = it
                    }
                },
                onImpact = {
                    withContext(Dispatchers.Main) {
                        triggerClip(reason = "타격 감지")
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            discardAndStopRollingRecording()
            if (!didHandOffCapturedUris) {
                capturedUris.forEach { uri ->
                    if (uri.scheme == "file") runCatching { File(uri.path.orEmpty()).delete() }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiShotTopBar(
                statusText = statusText,
                savedCount = savedCount,
                onClose = {
                    didHandOffCapturedUris = false
                    onClose()
                }
            )

            AiShotBottomPanel(
                level = level,
                sensitivity = sensitivity,
                onSensitivityChange = { sensitivity = it },
                capturePhase = capturePhase,
                recordingRemainingSeconds = recordingRemainingSeconds,
                activeRecordingSeconds = activeRecordingSeconds
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (hasPermissions) {
                AndroidView(
                    factory = { viewContext ->
                        PreviewView(viewContext).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PermissionPanel(
                    onRequest = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                )
            }
            shotLengthNotice?.let { notice ->
                ShotLengthNoticePanel(
                    shotLength = notice,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AiShotFloatingControls(
                shotLength = shotLength,
                onShotLengthTap = ::selectNextShotLength,
                zoomRatio = zoomRatio,
                minimumZoomRatio = minimumZoomRatio,
                maximumZoomRatio = maximumZoomRatio,
                onZoomRatioChange = { zoomRatio = it },
                lensLabel = if (lensFacing == CameraSelector.LENS_FACING_FRONT) "전면" else "후면",
                isRecording = triggerTimeSeconds != null,
                onManualRecord = {
                    if (triggerTimeSeconds == null) {
                        triggerClip("수동 클립 저장 중")
                    } else {
                        recording?.stop()
                    }
                },
                onSwitchCamera = {
                    discardAndStopRollingRecording()
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }
            )
            if (savedCount > 0) {
                Button(
                    onClick = {
                        didHandOffCapturedUris = true
                        onOpenEditor(capturedUris)
                    },
                    enabled = pendingSaveCount == 0 && triggerTimeSeconds == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF07323A),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF07323A).copy(alpha = 0.54f),
                        disabledContentColor = Color.White.copy(alpha = 0.52f)
                    )
                ) {
                    Text("저장한 ${savedCount}개 클립 편집", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AiShotTopBar(
    statusText: String,
    savedCount: Int,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF4A1719),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6525F).copy(alpha = 0.68f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(Icons.Outlined.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("$statusText · ${savedCount}개", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            modifier = Modifier.height(48.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6525F).copy(alpha = 0.68f)),
            onClick = onClose
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = null, tint = Color(0xFFE6525F), modifier = Modifier.size(19.dp))
                Text("닫기", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AiShotBottomPanel(
    level: Double,
    sensitivity: ShotSensitivity,
    onSensitivityChange: (ShotSensitivity) -> Unit,
    capturePhase: AiShotCapturePhase,
    recordingRemainingSeconds: Long,
    activeRecordingSeconds: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(Color(0xFFFFB432), CircleShape)
                )
                Spacer(Modifier.size(9.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(level.coerceIn(0.02, 1.0).toFloat())
                            .height(10.dp)
                            .background(Color(0xFF1DBA7A), RoundedCornerShape(16.dp))
                    )
                }
                Spacer(Modifier.size(10.dp))
                Text(
                    capturePhase.title,
                    color = if (capturePhase == AiShotCapturePhase.Detecting) {
                        Color(0xFFFFB432)
                    } else {
                        Color(0xFF4FD18A)
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "감도",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                listOf(
                    ShotSensitivity.Loud,
                    ShotSensitivity.Normal,
                    ShotSensitivity.Quiet,
                    ShotSensitivity.Auto
                ).forEach {
                    DarkFilterChip(
                        text = it.title,
                        selected = sensitivity == it,
                        modifier = Modifier.weight(1f),
                        onClick = { onSensitivityChange(it) }
                    )
                }
            }
            if (capturePhase != AiShotCapturePhase.Detecting) {
                RecordingProgress(
                    remainingSeconds = recordingRemainingSeconds,
                    totalSeconds = activeRecordingSeconds
                )
            }
        }
    }
}

@Composable
private fun AiShotModelInfoRow() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Ai ${AiShotModelInfo.Version} · ${AiShotModelInfo.Title}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                AiShotModelInfo.Summary,
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AiShotFloatingControls(
    modifier: Modifier = Modifier,
    shotLength: ShotLength,
    onShotLengthTap: () -> Unit,
    zoomRatio: Float,
    minimumZoomRatio: Float,
    maximumZoomRatio: Float,
    onZoomRatioChange: (Float) -> Unit,
    lensLabel: String,
    isRecording: Boolean,
    onManualRecord: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    var isPrecisionZoomVisible by remember { mutableStateOf(false) }
    var isPrecisionZoomInteracting by remember { mutableStateOf(false) }
    val latestZoomRatio by rememberUpdatedState(zoomRatio)
    val zoomOctaveWidthPx = with(LocalDensity.current) { 92.dp.toPx() }
    LaunchedEffect(isPrecisionZoomVisible, isPrecisionZoomInteracting, zoomRatio) {
        if (isPrecisionZoomVisible && !isPrecisionZoomInteracting) {
            delay(500)
            isPrecisionZoomVisible = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .height(56.dp)
                .background(Color.Black.copy(alpha = 0.34f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .pointerInput(minimumZoomRatio, maximumZoomRatio, zoomOctaveWidthPx) {
                    var startZoom = latestZoomRatio
                    var totalDragX = 0f
                    detectDragGestures(
                        onDragStart = {
                            startZoom = latestZoomRatio
                            totalDragX = 0f
                            isPrecisionZoomVisible = true
                            isPrecisionZoomInteracting = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            val nextZoom = zoomRatioAfterHorizontalDrag(
                                startRatio = startZoom,
                                dragDistancePx = totalDragX,
                                octaveWidthPx = zoomOctaveWidthPx,
                                minimum = minimumZoomRatio,
                                maximum = maximumZoomRatio
                            )
                            onZoomRatioChange(nextZoom)
                        },
                        onDragEnd = {
                            isPrecisionZoomInteracting = false
                        },
                        onDragCancel = {
                            isPrecisionZoomInteracting = false
                        }
                    )
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPrecisionZoomVisible) {
                Text(
                    zoomRatioTitle(zoomRatio),
                    color = Color(0xFF25C481),
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = zoomRatioToControlPosition(
                        zoomRatio,
                        minimumZoomRatio,
                        maximumZoomRatio
                    ),
                    onValueChange = { position ->
                        isPrecisionZoomVisible = true
                        isPrecisionZoomInteracting = true
                        onZoomRatioChange(
                            zoomControlPositionToRatio(
                                position,
                                minimumZoomRatio,
                                maximumZoomRatio
                            )
                        )
                    },
                    onValueChangeFinished = {
                        isPrecisionZoomInteracting = false
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "정밀 확대 조절"
                            stateDescription = zoomRatioTitle(zoomRatio)
                        }
                )
            } else {
                val factors = ZoomPreset.entries
                    .filter { it.ratio.toFloat() in minimumZoomRatio..maximumZoomRatio }
                val activeFactor = factors.lastOrNull {
                    it.ratio.toFloat() <= zoomRatio + 0.02f
                }
                factors.forEach { option ->
                    val selected = option == activeFactor
                    Button(
                        onClick = {
                            onZoomRatioChange(option.ratio.toFloat())
                            isPrecisionZoomVisible = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics { this.selected = selected },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) {
                                Color.White.copy(alpha = 0.16f)
                            } else {
                                Color.Transparent
                            },
                            contentColor = if (selected) Color(0xFF25C481) else Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            if (selected && option != ZoomPreset.Half) "${option.title}x" else option.title,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingSideButton(
                icon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
                title = shotLength.title,
                contentDescription = "샷 시간 ${shotLength.title}, ${shotLength.timingDescription}",
                onClick = onShotLengthTap
            )
            Button(
                onClick = onManualRecord,
                modifier = Modifier
                    .size(88.dp)
                    .semantics {
                        contentDescription = if (isRecording) {
                            "AiShot 클립 저장 중지"
                        } else {
                            "AiShot 수동 촬영"
                        }
                    },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.34f),
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .border(
                                5.dp,
                                if (isRecording) Color(0xFFE45D42) else Color(0xFF25C481).copy(alpha = 0.72f),
                                CircleShape
                            )
                    )
                    GolfSwingSpriteIndicator(
                        isAnimating = isRecording,
                        playbackDurationSeconds = shotLength.fullSeconds,
                        modifier = Modifier.size(62.dp)
                    )
                }
            }
            FloatingSideButton(
                icon = { Icon(Icons.Outlined.Cameraswitch, contentDescription = null) },
                title = lensLabel,
                contentDescription = "카메라 전환, 현재 $lensLabel",
                onClick = onSwitchCamera
            )
        }
    }
}

@Composable
private fun GolfSwingSpriteIndicator(
    isAnimating: Boolean,
    playbackDurationSeconds: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val frames = remember(context) {
        val sheet = BitmapFactory.decodeResource(context.resources, R.drawable.golf_swing_frames)
        val cellWidth = sheet.width / 6
        val cellHeight = sheet.height / 6
        List(36) { index ->
            Bitmap.createBitmap(
                sheet,
                (index % 6) * cellWidth,
                (index / 6) * cellHeight,
                cellWidth,
                cellHeight
            ).asImageBitmap()
        }
    }
    var frameIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(isAnimating, playbackDurationSeconds) {
        frameIndex = 0
        if (!isAnimating) return@LaunchedEffect
        val motionDurationMillis = (playbackDurationSeconds * 0.8 * 1_000.0)
            .toLong()
            .coerceAtLeast(100L)
        val frameDelayMillis = (motionDurationMillis / 35L).coerceAtLeast(16L)
        for (index in 0 until frames.lastIndex) {
            frameIndex = index
            delay(frameDelayMillis)
        }
        frameIndex = frames.lastIndex
    }
    Image(
        bitmap = frames[frameIndex],
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black, CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun FloatingSideButton(
    icon: @Composable () -> Unit,
    title: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(116.dp)
            .height(52.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.70f),
            contentColor = Color.White
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFF0B7A4E),
                shape = RoundedCornerShape(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
            Text(
                title,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecordingProgress(
    remainingSeconds: Long,
    totalSeconds: Long
) {
    val elapsedFraction = if (totalSeconds <= 0L) {
        0f
    } else {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }
    Column(
        modifier = Modifier.semantics {
            stateDescription =
                "${(elapsedFraction * 100).toInt()}%, ${remainingSeconds.coerceAtLeast(0L)}초 남음"
        },
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AiShot 클립 저장 중", color = Color.White, fontWeight = FontWeight.Bold)
            Text("${remainingSeconds.coerceAtLeast(0L)}초", color = Color.White.copy(alpha = 0.86f))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(elapsedFraction.coerceAtLeast(0.04f))
                    .height(8.dp)
                    .background(Color(0xFF1DBA7A), RoundedCornerShape(999.dp))
            )
        }
    }
}

@Composable
private fun ShotLengthNoticePanel(
    shotLength: ShotLength,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.78f)
            .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(16.dp)),
        color = Color.Black.copy(alpha = 0.94f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF0B7A4E),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "샷 시간",
                        color = Color.White.copy(alpha = 0.66f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        shotLength.timingDescription,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = Color(0xFF1DBA7A).copy(alpha = 0.46f),
                    shape = CircleShape
                ) {
                    Text(
                        shotLength.totalDurationDescription,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ShotLength.entries.forEach { option ->
                    val selected = option == shotLength
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (selected) Color(0xFF1DBA7A) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            option.title,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipRow(content: @Composable RowScopeHack.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RowScopeHack.content()
    }
}

private object RowScopeHack

@Composable
private fun DarkFilterChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF0D7778) else Color.Black.copy(alpha = 0.36f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) Color(0xFF6BA5A7) else Color.White.copy(alpha = 0.24f)
        )
    ) {
        Box(
            modifier = Modifier.height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = Color.White,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PermissionPanel(onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.padding(24.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.SettingsVoice, contentDescription = null, tint = Color(0xFF0B7A4E))
                Text("카메라와 마이크 권한이 필요합니다.", fontWeight = FontWeight.Bold)
                Text(
                    "카메라는 스윙 영상을 찍고, 마이크는 타격음을 감지해 HanClip 클립을 자동 저장합니다.",
                    color = Color(0xFF4B5A57),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(onClick = onRequest) {
                    Text("권한 허용")
                }
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5A57))
                ) {
                    Text("앱 설정 열기")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun monitorImpactAudio(
    sensitivity: ShotSensitivity,
    beforeSeconds: Double,
    visualAnalyzer: RealtimeVisualAnalyzer,
    onLevel: suspend (Double) -> Unit,
    onImpact: suspend () -> Unit
) {
    val sampleRate = 16_000
    val minBuffer = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 2)
    val buffer = ShortArray(minBuffer / 2)
    val recorder = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBuffer
    )
    var ambient = 0.04
    var baseline = 0.008
    var recentLevel = 0.008
    var lastTrigger = 0L
    val readyDelayMillis = (beforeSeconds.coerceAtLeast(0.5) * 1000).toLong()
    val readyPromptSuppressionMillis = 1050L
    var startedAt = 0L
    try {
        recorder.startRecording()
        startedAt = System.currentTimeMillis()
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            val count = recorder.read(buffer, 0, buffer.size)
            if (count <= 0) continue
            var sum = 0.0
            var peak = 0.0
            var crossings = 0
            var previousSign = 0
            for (index in 0 until count) {
                val sample = buffer[index] / 32768.0
                val sign = if (sample >= 0.0) 1 else -1
                sum += sample * sample
                peak = max(peak, kotlin.math.abs(sample))
                if (index > 0 && sign != previousSign) {
                    crossings += 1
                }
                previousSign = sign
            }
            val rms = sqrt(sum / count).coerceIn(0.0, 1.0)
            val metrics = RealtimeImpactMetrics(
                rms = rms,
                peak = peak.coerceIn(0.0, 1.0),
                crossingRate = crossings.toDouble() / max(1, count).toDouble()
            )
            val score = metrics.impactScore
            val previousRecentLevel = recentLevel
            val baselineSample = min(score, max(0.004, baseline * 1.35))
            val elapsedMillis = System.currentTimeMillis() - startedAt
            val baselineWeight = if (elapsedMillis < readyDelayMillis) 0.04 else 0.012
            baseline = baseline * (1.0 - baselineWeight) +
                max(0.002, baselineSample) * baselineWeight
            recentLevel = recentLevel * 0.72 + max(0.002, score) * 0.28
            ambient = ambient * 0.94 + rms * 0.06
            onLevel((score / 0.45).coerceIn(0.0, 1.0))
            val now = System.currentTimeMillis()
            val decision = RealtimeImpactClassifier.detectImpact(
                metrics = metrics,
                baseline = baseline,
                previousRecentLevel = previousRecentLevel,
                sensitivity = sensitivity
            )
            val visualScore = visualAnalyzer.recentScore()
            val isVisuallySupportedImpact = visualScore >= 0.62 &&
                decision.confidence + visualScore * 0.32 >= 0.96 &&
                score >= max(0.04, baseline * 1.45)
            val isInsideReadyPromptWindow = now - startedAt <
                readyDelayMillis + readyPromptSuppressionMillis
            val isClearlyPhysicalImpact = visualScore >= 0.5 &&
                score >= max(0.16, baseline * 2.4) &&
                metrics.peak >= 0.25
            val isAudioTriggerAllowed = decision.isTriggered &&
                (!isInsideReadyPromptWindow || isClearlyPhysicalImpact)
            val isVisualTriggerAllowed = isVisuallySupportedImpact &&
                (!isInsideReadyPromptWindow || isClearlyPhysicalImpact)
            val ready = now - startedAt >= readyDelayMillis
            if (ready &&
                (isAudioTriggerAllowed || isVisualTriggerAllowed) &&
                now - lastTrigger > 3500L
            ) {
                lastTrigger = now
                onImpact()
                delay(1500L)
            }
        }
    } finally {
        runCatching { recorder.stop() }
        recorder.release()
    }
}

private data class RealtimeVisualFrame(
    val cells: DoubleArray,
    val averageBrightness: Double
)

private class RealtimeVisualAnalyzer {
    companion object {
        private const val AnalysisIntervalMillis = 220L
        private const val SignalLifetimeMillis = 750L
        private const val GridWidth = 8
        private const val GridHeight = 8
    }

    @Volatile
    private var latestSignalTimeMillis = 0L

    @Volatile
    private var latestSignalScore = 0.0

    private var lastAnalysisTimeMillis = 0L
    private var lastFrame: RealtimeVisualFrame? = null
    private var visualBaseline = 0.04
    private var processedSignalCount = 0

    fun reset() {
        latestSignalTimeMillis = 0L
        latestSignalScore = 0.0
        lastAnalysisTimeMillis = 0L
        lastFrame = null
        visualBaseline = 0.04
        processedSignalCount = 0
    }

    fun recentScore(nowMillis: Long = SystemClock.elapsedRealtime()): Double {
        return if (nowMillis - latestSignalTimeMillis <= SignalLifetimeMillis) {
            latestSignalScore
        } else {
            0.0
        }
    }

    fun analyze(bitmap: android.graphics.Bitmap) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalysisTimeMillis < AnalysisIntervalMillis) return
        lastAnalysisTimeMillis = now
        val frame = makeFrame(bitmap) ?: return
        val previous = lastFrame
        lastFrame = frame
        if (previous == null || previous.cells.size != frame.cells.size) return

        var motion = 0.0
        for (index in frame.cells.indices) {
            motion += abs(frame.cells[index] - previous.cells[index])
        }
        motion /= max(1, frame.cells.size).toDouble()
        val brightnessChange = abs(frame.averageBrightness - previous.averageBrightness)
        val rawVisualEnergy = motion * 2.6 + brightnessChange * 1.4
        visualBaseline = visualBaseline * 0.92 +
            min(rawVisualEnergy, max(0.015, visualBaseline * 1.45)) * 0.08
        val contrast = rawVisualEnergy / max(0.018, visualBaseline)
        latestSignalScore = min(
            1.0,
            max(0.0, contrast - 1.0) * 0.28 +
                min(1.0, motion * 4.0) * 0.48 +
                min(1.0, brightnessChange * 3.0) * 0.24
        )
        latestSignalTimeMillis = now
        processedSignalCount += 1
        if (processedSignalCount == 1) {
            Log.d("HanClipAiShot", "Visual assist active: 8x8 preview frames")
        }
    }

    private fun makeFrame(bitmap: android.graphics.Bitmap): RealtimeVisualFrame? {
        if (bitmap.width <= 0 || bitmap.height <= 0) return null
        val cells = DoubleArray(GridWidth * GridHeight)
        var brightnessTotal = 0.0
        var outputIndex = 0
        for (yCell in 0 until GridHeight) {
            val y = ((yCell * bitmap.height + bitmap.height / 2) / GridHeight)
                .coerceIn(0, bitmap.height - 1)
            for (xCell in 0 until GridWidth) {
                val x = ((xCell * bitmap.width + bitmap.width / 2) / GridWidth)
                    .coerceIn(0, bitmap.width - 1)
                val pixel = bitmap.getPixel(x, y)
                val brightness = (
                    android.graphics.Color.red(pixel) * 0.299 +
                        android.graphics.Color.green(pixel) * 0.587 +
                        android.graphics.Color.blue(pixel) * 0.114
                    ) / 255.0
                cells[outputIndex++] = brightness
                brightnessTotal += brightness
            }
        }
        return RealtimeVisualFrame(
            cells = cells,
            averageBrightness = brightnessTotal / max(1, cells.size).toDouble()
        )
    }
}

private data class RealtimeImpactMetrics(
    val rms: Double,
    val peak: Double,
    val crossingRate: Double
) {
    val impactScore: Double
        get() {
            val highFrequencyWeight = min(1.0, crossingRate * 10.0)
            return min(1.0, rms * 0.55 + peak * 0.45 + highFrequencyWeight * rms * 0.35)
        }
}

private data class RealtimeImpactDecision(
    val isTriggered: Boolean,
    val confidence: Double
)

private object RealtimeImpactClassifier {
    private data class Thresholds(
        val strongScoreFloor: Double,
        val strongBaselineMultiplier: Double,
        val strongPeakFloor: Double,
        val strongPeakBaselineMultiplier: Double,
        val strongRise: Double,
        val strongCrossingRate: Double,
        val strongCrestFactor: Double,
        val distantScoreFloor: Double,
        val distantBaselineMultiplier: Double,
        val distantPeakFloor: Double,
        val distantPeakBaselineMultiplier: Double,
        val distantRise: Double,
        val distantCrossingRate: Double,
        val distantCrestFactor: Double
    )

    fun detectImpact(
        metrics: RealtimeImpactMetrics,
        baseline: Double,
        previousRecentLevel: Double,
        sensitivity: ShotSensitivity
    ): RealtimeImpactDecision {
        val score = metrics.impactScore
        val referenceLevel = max(0.003, max(baseline, previousRecentLevel * 0.82))
        val suddenRise = score / referenceLevel
        val crestFactor = metrics.peak / max(0.001, metrics.rms)
        val thresholds = thresholdsFor(effectiveSensitivity(sensitivity, baseline))
        val strongScoreRequirement = max(
            thresholds.strongScoreFloor,
            baseline * thresholds.strongBaselineMultiplier
        )
        val strongPeakRequirement = max(
            thresholds.strongPeakFloor,
            baseline * thresholds.strongPeakBaselineMultiplier
        )
        val isStrongImpact = score >= strongScoreRequirement &&
            metrics.peak >= strongPeakRequirement &&
            suddenRise >= thresholds.strongRise &&
            metrics.crossingRate >= thresholds.strongCrossingRate &&
            crestFactor >= thresholds.strongCrestFactor

        val distantScoreRequirement = max(
            thresholds.distantScoreFloor,
            baseline * thresholds.distantBaselineMultiplier
        )
        val distantPeakRequirement = max(
            thresholds.distantPeakFloor,
            baseline * thresholds.distantPeakBaselineMultiplier
        )
        val isDistantSharpImpact = score >= distantScoreRequirement &&
            metrics.peak >= distantPeakRequirement &&
            suddenRise >= thresholds.distantRise &&
            metrics.crossingRate >= thresholds.distantCrossingRate &&
            crestFactor >= thresholds.distantCrestFactor

        val isSpeechLikePrompt = metrics.rms >= 0.025 &&
            crestFactor < 3.15 &&
            metrics.crossingRate < 0.16 &&
            suddenRise < 4.8 &&
            score < 0.18

        val confidence = impactConfidence(
            score = score,
            peak = metrics.peak,
            suddenRise = suddenRise,
            crossingRate = metrics.crossingRate,
            crestFactor = crestFactor,
            thresholds = thresholds
        )

        return RealtimeImpactDecision(
            isTriggered = !isSpeechLikePrompt && (isStrongImpact || isDistantSharpImpact),
            confidence = if (isSpeechLikePrompt) 0.0 else confidence
        )
    }

    private fun effectiveSensitivity(
        sensitivity: ShotSensitivity,
        baseline: Double
    ): ShotSensitivity {
        if (sensitivity != ShotSensitivity.Auto) return sensitivity
        return when {
            baseline >= 0.026 -> ShotSensitivity.Loud
            baseline <= 0.009 -> ShotSensitivity.Quiet
            else -> ShotSensitivity.Normal
        }
    }

    private fun thresholdsFor(sensitivity: ShotSensitivity): Thresholds {
        return when (sensitivity) {
            ShotSensitivity.Loud -> Thresholds(
                strongScoreFloor = 0.10,
                strongBaselineMultiplier = 2.7,
                strongPeakFloor = 0.18,
                strongPeakBaselineMultiplier = 4.2,
                strongRise = 2.2,
                strongCrossingRate = 0.07,
                strongCrestFactor = 2.3,
                distantScoreFloor = 0.065,
                distantBaselineMultiplier = 3.4,
                distantPeakFloor = 0.12,
                distantPeakBaselineMultiplier = 5.0,
                distantRise = 3.0,
                distantCrossingRate = 0.10,
                distantCrestFactor = 3.5
            )
            ShotSensitivity.Normal,
            ShotSensitivity.Auto -> Thresholds(
                strongScoreFloor = 0.075,
                strongBaselineMultiplier = 2.3,
                strongPeakFloor = 0.13,
                strongPeakBaselineMultiplier = 3.5,
                strongRise = 1.8,
                strongCrossingRate = 0.05,
                strongCrestFactor = 2.0,
                distantScoreFloor = 0.045,
                distantBaselineMultiplier = 2.8,
                distantPeakFloor = 0.09,
                distantPeakBaselineMultiplier = 4.2,
                distantRise = 2.4,
                distantCrossingRate = 0.08,
                distantCrestFactor = 3.0
            )
            ShotSensitivity.Quiet -> Thresholds(
                strongScoreFloor = 0.055,
                strongBaselineMultiplier = 1.9,
                strongPeakFloor = 0.095,
                strongPeakBaselineMultiplier = 3.0,
                strongRise = 1.55,
                strongCrossingRate = 0.04,
                strongCrestFactor = 1.7,
                distantScoreFloor = 0.035,
                distantBaselineMultiplier = 2.3,
                distantPeakFloor = 0.07,
                distantPeakBaselineMultiplier = 3.4,
                distantRise = 2.0,
                distantCrossingRate = 0.065,
                distantCrestFactor = 2.5
            )
        }
    }

    private fun impactConfidence(
        score: Double,
        peak: Double,
        suddenRise: Double,
        crossingRate: Double,
        crestFactor: Double,
        thresholds: Thresholds
    ): Double {
        val scoreRatio = score / max(0.001, thresholds.distantScoreFloor)
        val peakRatio = peak / max(0.001, thresholds.distantPeakFloor)
        val riseRatio = suddenRise / max(0.001, thresholds.distantRise)
        val crossingRatio = crossingRate / max(0.001, thresholds.distantCrossingRate)
        val crestRatio = crestFactor / max(0.001, thresholds.distantCrestFactor)
        return scoreRatio * 0.28 +
            peakRatio * 0.22 +
            riseRatio * 0.24 +
            min(crossingRatio, 1.8) * 0.13 +
            min(crestRatio, 1.8) * 0.13
    }
}

private fun Context.hasAiShotPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider {
    val future = ProcessCameraProvider.getInstance(this)
    return suspendCancellableCoroutine { continuation ->
        future.addListener(
            {
                if (!continuation.isActive) return@addListener
                runCatching { future.get() }
                    .onSuccess { provider ->
                        if (continuation.isActive) continuation.resume(provider)
                    }
                    .onFailure { error ->
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}
