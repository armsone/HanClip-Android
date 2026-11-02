package com.hanclip.android.feature.photosort

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.activity.viewModels
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import android.view.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.IconButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import com.hanclip.android.core.media.ByungkiPlayerPolicy
import com.hanclip.android.core.theme.HanClipTheme
import kotlinx.coroutines.delay
import java.util.UUID

/** An additional share destination inside the existing HanClip package, not a launcher app. */
class PhotoSortActivity : ComponentActivity() {
    private val model: PhotoSortModel by viewModels()
    private var permissionDenied by mutableStateOf(false)
    private var readyToRecommend by mutableStateOf(false)
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        readyToRecommend = granted
    }
    private val writeConsent = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        model.consentResult(result.resultCode == Activity.RESULT_OK)
    }
    private val readPermission get() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restoredSession = savedInstanceState?.getString(SESSION)?.takeIf { value ->
            runCatching { UUID.fromString(value).toString() == value }.getOrDefault(false)
        }
        // External share senders cannot choose another session journal or a file path.
        intent.putExtra(SESSION, restoredSession ?: UUID.randomUUID().toString())
        setContent {
            HanClipTheme {
                BackHandler(enabled = model.moving) { /* Wait for confirmed individual move results. */ }
                Surface(Modifier.fillMaxSize()) {
                    SortScreen(model, permissionDenied, readyToRecommend, onRecommend = ::loadShared, onPermission = { permission.launch(readPermission) },
                        onClose = { if (!model.moving) { model.cancelAnalysis(); finish() } }, onMove = {
                            model.requestMove { uris ->
                                try {
                                    val request = MediaStore.createWriteRequest(contentResolver, uris)
                                    writeConsent.launch(IntentSenderRequest.Builder(request.intentSender).build())
                                } catch (e: Exception) { model.launchFailure(e) }
                            }
                        })
                }
            }
        }
        if (Build.VERSION.SDK_INT < 30) loadShared()
        else if (ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED) readyToRecommend = true
        else permission.launch(readPermission)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SESSION, intent.getStringExtra(SESSION))
        super.onSaveInstanceState(outState)
    }
    @Suppress("DEPRECATION")
    private fun loadShared() {
        val items = mutableListOf<Uri>()
        when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(items::add)
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(items::addAll)
        }
        intent.clipData?.let { clip -> for (i in 0 until clip.itemCount) clip.getItemAt(i).uri?.let(items::add) }
        model.load(intent.getStringExtra(SESSION) ?: return, items.distinct())
    }
    companion object { private const val SESSION = "com.hanclip.android.photo_sort_session" }
}

@Composable
private fun SortScreen(model: PhotoSortModel, denied: Boolean, ready: Boolean, onRecommend: () -> Unit, onPermission: () -> Unit,
    onClose: () -> Unit, onMove: () -> Unit) {
    var showAlbums by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var confirmMove by remember { mutableStateOf(false) }
    var previewURI by remember { mutableStateOf<Uri?>(null) }
    val selectableAlbums = model.albums.filter { album -> model.selected.all { it.volume == album.volume } }
    // The controls and results share one scroll container, including on short windows and large fonts.
    LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("한양 사진 고르기", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onClose, enabled = !model.moving) { Text("닫기") }
                }
                Text("선명도·밝기·해상도를 기준으로 좋은 사진을 고릅니다.")
                Text(model.status, style = MaterialTheme.typography.bodyMedium)
                if (denied) {
                    Text("대상 앨범의 파일명 충돌을 확인하려면 사진 전체 접근이 필요합니다. 선택한 사진만 허용한 경우 원본 이동을 진행하지 않습니다.")
                    Button(onClick = onPermission) { Text("사진 접근 허용") }
                }
                if (model.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (model.photos.isEmpty() && ready && !model.busy) {
                    Button(onClick = onRecommend, modifier = Modifier.fillMaxWidth()) { Text("한양에게 추천받기") }
                }
                if (model.photos.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(enabled = model.canChange && model.count > 1, onClick = { model.count-- }) { Text("−") }
                        Text("${model.photos.size}장 중 ${model.count}장", Modifier.weight(1f))
                        TextButton(enabled = model.canChange && model.count < model.photos.size, onClick = { model.count++ }) { Text("＋") }
                    }
                    Text("한양 추천 ${model.recommendedCount}장 · 현재 선택 ${model.count}장", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        if (model.photos.isNotEmpty()) {
            items(model.selected, key = { it.uri.toString() }) { photo ->
                Column(Modifier.fillMaxWidth()) {
                    PhotoSortResultImage(photo, onClick = { previewURI = photo.uri })
                    Text(if (photo.uri.toString() in model.completed) "이동 완료" else photo.name,
                        maxLines = 1, style = MaterialTheme.typography.labelSmall)
                }
            }
            item {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showAlbums = true }, enabled = model.canChange, modifier = Modifier.fillMaxWidth()) {
                        Text(model.destination?.label ?: "넣을 앨범 선택")
                    }
                    Text("원본 사진이 기존 앨범에서 선택한 앨범으로 이동됩니다. 복사본은 만들지 않습니다.",
                        style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { confirmMove = true }, enabled = !model.busy && model.destination != null &&
                        model.selected.any { it.uri.toString() !in model.completed }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (model.completed.isEmpty()) "앨범으로 이동" else "남은 사진 다시 이동")
                    }
                }
            }
        }
    }
    previewURI?.let { uri ->
        val selected = model.selected
        val index = selected.indexOfFirst { it.uri == uri }
        if (index >= 0) PhotoSortLargePreview(selected, index,
            onPage = { previewURI = selected[it].uri }, onClose = { previewURI = null })
        else previewURI = null
    }
    if (showAlbums) AlertDialog(onDismissRequest = { showAlbums = false }, title = { Text("앨범 선택") }, text = {
        Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
            if (selectableAlbums.isEmpty()) Text("선택한 원본과 같은 저장소의 앨범이 없습니다.")
            selectableAlbums.forEach { album -> TextButton(onClick = { model.destination = album; showAlbums = false }) {
                Text(album.label)
            } }
            TextButton(onClick = { showAlbums = false; showNew = true }) { Text("새 앨범 만들기") }
        }
    }, confirmButton = { TextButton(onClick = { showAlbums = false }) { Text("닫기") } })
    if (showNew) AlertDialog(onDismissRequest = { showNew = false }, title = { Text("새 앨범") }, text = {
        Column {
            OutlinedTextField(value = newName, onValueChange = { newName = it; nameError = null }, label = { Text("앨범 이름") }, singleLine = true)
            Text("Pictures 폴더에 원본을 이동합니다. 이동하기 전에는 폴더를 만들지 않습니다.")
            nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = {
        val name = newName.trim()
        val volumes = model.selected.map { it.volume }.distinct()
        if (name.isEmpty() || name == "." || name == ".." || name.any { it == '/' || it == '\\' || it.code < 32 } || name.length > 80) {
            nameError = "슬래시 없이 1~80자의 앨범 이름을 입력해 주세요."
        } else if (volumes.size != 1) nameError = "같은 저장소의 사진끼리 선택해 주세요."
        else { model.destination = PhotoAlbum(volumes.single(), "Pictures/$name/"); showNew = false }
    }) { Text("선택") } }, dismissButton = { TextButton(onClick = { showNew = false }) { Text("취소") } })
    if (confirmMove) AlertDialog(onDismissRequest = { confirmMove = false }, title = { Text("원본을 이동할까요?") },
        text = { Text("${model.count - model.completed.size}장 원본을 선택한 앨범으로 이동합니다. 기존 앨범에서는 빠지며, 복사본을 만들지 않습니다.") },
        confirmButton = { TextButton(onClick = { confirmMove = false; onMove() }) { Text("앨범으로 이동") } },
        dismissButton = { TextButton(onClick = { confirmMove = false }) { Text("취소") } })
}


@Composable
private fun PhotoSortLargePreview(
    photos: List<SortPhoto>,
    index: Int,
    onPage: (Int) -> Unit,
    onClose: () -> Unit
) {
    if (photos.isEmpty() || index !in photos.indices) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    val photo = photos[index]
    val context = LocalContext.current
    var isPlaying by rememberSaveable { mutableStateOf(false) } // New open is always paused
    var controlsVisible by remember { mutableStateOf(true) } // Initial visible
    var intervalSeconds by remember { mutableIntStateOf(ByungkiPlayerPolicy.getImageInterval(context)) }
    var repeatMode by remember { mutableStateOf(ByungkiPlayerPolicy.getImageRepeatMode(context)) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var isHoldingSingleImage by remember { mutableStateOf(false) }
    var isCurrentFrameReady by remember(photo.uri, reloadTrigger) { mutableStateOf(false) }
    var isForeground by remember { mutableStateOf(true) }
    var isGestureActive by remember { mutableStateOf(false) }
    var isTouchingOrAccessibilityActive by remember { mutableStateOf(false) }
    var hasCurrentFrameError by remember(photo.uri, reloadTrigger) { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> isForeground = true
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    isForeground = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(context) {
        val am = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
        val listener = android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener { active ->
            isTouchingOrAccessibilityActive = active
        }
        if (am != null) {
            isTouchingOrAccessibilityActive = am.isTouchExplorationEnabled
            am.addTouchExplorationStateChangeListener(listener)
        }
        onDispose {
            am?.removeTouchExplorationStateChangeListener(listener)
        }
    }

    // Controlled loop / sole scheduler
    LaunchedEffect(isPlaying, isForeground, photos.size, index, reloadTrigger, intervalSeconds, repeatMode) {
        if (!isPlaying || !isForeground || photos.isEmpty()) {
            isHoldingSingleImage = false
            return@LaunchedEffect
        }
        while (isPlaying && isForeground) {
            snapshotFlow { isCurrentFrameReady }.first { it }
            delay(intervalSeconds * 1000L)
            if (!isPlaying || !isForeground) break

            if (photos.size <= 1) {
                isHoldingSingleImage = true
                continue
            }

            when (repeatMode) {
                ByungkiPlayerPolicy.ImageRepeatMode.ALL_REPEAT -> {
                    isHoldingSingleImage = false
                    onPage((index + 1) % photos.size)
                    break
                }
                ByungkiPlayerPolicy.ImageRepeatMode.SHUFFLE -> {
                    isHoldingSingleImage = false
                    val availableIndices = photos.indices.filter { it != index }
                    onPage(availableIndices.random())
                    break
                }
                ByungkiPlayerPolicy.ImageRepeatMode.ONE_REPEAT -> {
                    isHoldingSingleImage = true
                    // ONE_REPEAT wait again without reload
                }
                ByungkiPlayerPolicy.ImageRepeatMode.ONCE -> {
                    isHoldingSingleImage = false
                    if (index < photos.lastIndex) {
                        onPage(index + 1)
                        break
                    } else {
                        isPlaying = false
                        break
                    }
                }
            }
        }
    }

    // Auto-hide controls: 2.5 seconds playing and idle only
    LaunchedEffect(controlsVisible, isPlaying, isGestureActive, isCurrentFrameReady, hasCurrentFrameError, isTouchingOrAccessibilityActive) {
        if (!isPlaying || !isCurrentFrameReady || hasCurrentFrameError) {
            controlsVisible = true
            return@LaunchedEffect
        }
        if (controlsVisible && isPlaying && !isGestureActive && !isTouchingOrAccessibilityActive) {
            delay(ByungkiPlayerPolicy.CONTROLS_HIDE_DELAY_MS)
            if (isPlaying && !isGestureActive && !isTouchingOrAccessibilityActive && isCurrentFrameReady && !hasCurrentFrameError) {
                controlsVisible = false
            }
        }
    }

    val handleClose = {
        isPlaying = false
        onClose()
    }

    val handleShare = {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, photo.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, ByungkiPlayerPolicy.LABEL_SHARE))
        } catch (_: Exception) { }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = handleClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        com.hanclip.android.feature.editor.FullScreenDialogSystemBars(
            background = Color.Black,
            navigationBackground = Color.Black,
            hideSystemBars = true
        )
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key.nativeKeyCode) {
                            KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK -> {
                                handleClose()
                                true
                            }
                            KeyEvent.KEYCODE_SPACE -> {
                                if (photos.size > 1) isPlaying = !isPlaying
                                controlsVisible = true
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (photos.size > 1) onPage((index - 1 + photos.size) % photos.size)
                                controlsVisible = true
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (photos.size > 1) onPage((index + 1) % photos.size)
                                controlsVisible = true
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            color = Color.Black
        ) {
            Box(Modifier.fillMaxSize()) {
                key(photo.uri) {
                    var loadError by remember { mutableStateOf(false) }
                    val large by produceState<Bitmap?>(null, photo.uri, reloadTrigger) {
                        var decoded: Bitmap? = null
                        try {
                            withContext(Dispatchers.IO) {
                                decoded = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, photo.uri)) { decoder, info, _ ->
                                    val ratio = min(1.0, 2048.0 / max(info.size.width, info.size.height))
                                    decoder.setTargetSize(max(1, (info.size.width * ratio).toInt()), max(1, (info.size.height * ratio).toInt()))
                                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                }
                            }
                            value = decoded
                            awaitDispose { decoded?.recycle() }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            decoded?.recycle()
                            throw e
                        } catch (_: Exception) {
                            decoded?.recycle()
                            loadError = true
                        }
                    }

                    LaunchedEffect(large, loadError) {
                        if (large != null || loadError) {
                            isCurrentFrameReady = true
                            hasCurrentFrameError = loadError
                        }
                    }
                    DisposableEffect(Unit) {
                        onDispose {
                            isCurrentFrameReady = true
                        }
                    }

                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    var viewport by remember { mutableStateOf(IntSize.Zero) }
                    val image = large ?: photo.thumbnail
                    val density = LocalDensity.current

                    val transform = rememberTransformableState { zoom, pan, _ ->
                        scale = (scale * zoom).coerceIn(ByungkiPlayerPolicy.IMAGE_MIN_ZOOM, ByungkiPlayerPolicy.IMAGE_MAX_ZOOM)
                        if (kotlin.math.abs(scale - 1f) <= ByungkiPlayerPolicy.ZOOM_RESET_EPSILON) {
                            offset = Offset.Zero
                        } else {
                            val fit = min(viewport.width.toFloat() / image.width, viewport.height.toFloat() / image.height)
                            val xLimit = max(0f, (image.width * fit * scale - viewport.width) / 2)
                            val yLimit = max(0f, (image.height * fit * scale - viewport.height) / 2)
                            offset = Offset(
                                (offset.x + pan.x).coerceIn(-xLimit, xLimit),
                                (offset.y + pan.y).coerceIn(-yLimit, yLimit)
                            )
                        }
                    }

                    LaunchedEffect(transform.isTransformInProgress) {
                        if (transform.isTransformInProgress) {
                            isGestureActive = true
                            controlsVisible = true
                        } else {
                            isGestureActive = false
                        }
                    }

                    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
                    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .onSizeChanged { viewport = it }
                            .transformable(transform)
                            .pointerInput(photos.size, scale, offset) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        // Priority: if zoom != fit (scale != 1) or panned -> restore fitted size AND center ONLY
                                        if (kotlin.math.abs(scale - 1f) > ByungkiPlayerPolicy.ZOOM_RESET_EPSILON ||
                                            offset.getDistance() > ByungkiPlayerPolicy.PAN_RESET_EPSILON_LOGICAL) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else if (photos.size > 1) {
                                            // otherwise toggle play/pause
                                            isPlaying = !isPlaying
                                        }
                                        controlsVisible = true
                                    },
                                    onTap = {
                                        // Hidden -> show only; visible -> show only (don't dismiss on tap)
                                        controlsVisible = true
                                    }
                                )
                            }
                            .pointerInput(photos.size, scale, index) {
                                val navDistancePx = with(density) { ByungkiPlayerPolicy.IMAGE_HORIZONTAL_NAV_DISTANCE_DP.dp.toPx() }
                                val dismissDistancePx = with(density) { ByungkiPlayerPolicy.DOWNWARD_DISMISS_DISTANCE_DP.dp.toPx() }
                                val slopPx = with(density) { ByungkiPlayerPolicy.GESTURE_START_SLOP_DP.dp.toPx() }

                                detectDragGestures(
                                    onDragStart = {
                                        accumulatedDragX = 0f
                                        accumulatedDragY = 0f
                                        isGestureActive = true
                                        controlsVisible = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        // Pinch/pan priority when zoomed
                                        if (kotlin.math.abs(scale - 1f) > ByungkiPlayerPolicy.ZOOM_RESET_EPSILON) {
                                            return@detectDragGestures
                                        }
                                        change.consume()
                                        accumulatedDragX += dragAmount.x
                                        accumulatedDragY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        isGestureActive = false
                                        if (kotlin.math.abs(scale - 1f) <= ByungkiPlayerPolicy.ZOOM_RESET_EPSILON) {
                                            if (accumulatedDragY >= dismissDistancePx &&
                                                kotlin.math.abs(accumulatedDragY) > kotlin.math.abs(accumulatedDragX)) {
                                                handleClose()
                                            } else if (kotlin.math.abs(accumulatedDragX) >= navDistancePx) {
                                                if (accumulatedDragX > 0 && photos.size > 1) {
                                                    onPage((index - 1 + photos.size) % photos.size)
                                                } else if (accumulatedDragX < 0 && photos.size > 1) {
                                                    onPage((index + 1) % photos.size)
                                                }
                                            }
                                        }
                                        accumulatedDragX = 0f
                                        accumulatedDragY = 0f
                                    },
                                    onDragCancel = {
                                        isGestureActive = false
                                        accumulatedDragX = 0f
                                        accumulatedDragY = 0f
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            image.asImageBitmap(),
                            photo.name,
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                },
                            contentScale = ContentScale.Fit
                        )
                        if (large == null && !loadError) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    if (loadError) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "큰 사진을 읽지 못해 작은 미리보기를 표시합니다.",
                                    color = Color.White,
                                    fontSize = ByungkiPlayerPolicy.SECONDARY_TEXT_SP.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            loadError = false
                                            reloadTrigger++
                                        },
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(ByungkiPlayerPolicy.LABEL_RETRY)
                                    }
                                    OutlinedButton(
                                        onClick = handleClose,
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text(ByungkiPlayerPolicy.LABEL_CLOSE)
                                    }
                                }
                            }
                        }
                    }
                }

                // Overlay Controls - no layout reflow when hidden
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .graphicsLayer {
                            alpha = if (controlsVisible) 1f else 0f
                        }
                ) {
                    if (controlsVisible) {
                        // Top bar
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PhotoControlCircleButton(
                                onClick = handleClose,
                                contentDescription = ByungkiPlayerPolicy.LABEL_CLOSE
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
                                )
                            }
                            Text(
                                "${index + 1}/${photos.size} · ${photo.name}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                                fontSize = ByungkiPlayerPolicy.TITLE_TEXT_SP.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            PhotoControlCircleButton(
                                onClick = handleShare,
                                contentDescription = ByungkiPlayerPolicy.LABEL_SHARE
                            ) {
                                Icon(
                                    Icons.Outlined.IosShare,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
                                )
                            }
                        }

                        // Bottom bar
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}/${photos.size} · ${intervalSeconds}초 · ${repeatMode.label}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = ByungkiPlayerPolicy.SECONDARY_TEXT_SP.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Box {
                                    PhotoControlCircleButton(
                                        onClick = { showSettingsMenu = !showSettingsMenu },
                                        contentDescription = "재생 설정"
                                    ) {
                                        Icon(
                                            Icons.Outlined.Settings,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showSettingsMenu,
                                        onDismissRequest = { showSettingsMenu = false }
                                    ) {
                                        Text(
                                            "반복 방식",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        ByungkiPlayerPolicy.ImageRepeatMode.entries.forEach { mode ->
                                            DropdownMenuItem(
                                                text = { Text(if (mode == repeatMode) "✓ ${mode.label}" else "    ${mode.label}") },
                                                onClick = {
                                                    repeatMode = mode
                                                    ByungkiPlayerPolicy.setImageRepeatMode(context, mode)
                                                    showSettingsMenu = false
                                                }
                                            )
                                        }
                                        HorizontalDivider()
                                        Text(
                                            "재생 시간 간격",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        ByungkiPlayerPolicy.INTERVAL_OPTIONS_SECONDS.forEach { sec ->
                                            DropdownMenuItem(
                                                text = { Text(if (sec == intervalSeconds) "✓ ${sec}초" else "    ${sec}초") },
                                                onClick = {
                                                    intervalSeconds = sec
                                                    ByungkiPlayerPolicy.setImageInterval(context, sec)
                                                    showSettingsMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PhotoControlCircleButton(
                                    onClick = { if (photos.size > 1) onPage((index - 1 + photos.size) % photos.size) },
                                    enabled = photos.size > 1,
                                    contentDescription = ByungkiPlayerPolicy.LABEL_PREVIOUS
                                ) {
                                    Icon(
                                        Icons.Outlined.SkipPrevious,
                                        contentDescription = null,
                                        tint = if (photos.size > 1) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
                                    )
                                }

                                PhotoControlCircleButton(
                                    onClick = {
                                        if (photos.size > 1) isPlaying = !isPlaying
                                    },
                                    enabled = photos.size > 1,
                                    contentDescription = if (isPlaying) ByungkiPlayerPolicy.LABEL_PAUSE else ByungkiPlayerPolicy.LABEL_PLAY
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                        contentDescription = null,
                                        tint = if (photos.size > 1) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
                                    )
                                }

                                PhotoControlCircleButton(
                                    onClick = { if (photos.size > 1) onPage((index + 1) % photos.size) },
                                    enabled = photos.size > 1,
                                    contentDescription = ByungkiPlayerPolicy.LABEL_NEXT
                                ) {
                                    Icon(
                                        Icons.Outlined.SkipNext,
                                        contentDescription = null,
                                        tint = if (photos.size > 1) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(ByungkiPlayerPolicy.ICON_SIZE_DP.dp)
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
private fun PhotoControlCircleButton(
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(ByungkiPlayerPolicy.BUTTON_TOUCH_TARGET_DP.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Surface(
            modifier = Modifier.size(ByungkiPlayerPolicy.BUTTON_VISUAL_DP.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
    }
}


/** Only visible result rows decode a larger image; off-screen rows release their bitmap. */
@Composable
private fun PhotoSortResultImage(photo: SortPhoto, onClick: () -> Unit) {
    val context = LocalContext.current
    val image by produceState<Bitmap?>(null, photo.uri) {
        var decoded: Bitmap? = null
        try {
            withContext(Dispatchers.IO) {
                decoded = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, photo.uri)) { decoder, info, _ ->
                    val ratio = min(1.0, 768.0 / max(info.size.width, info.size.height))
                    decoder.setTargetSize(max(1, (info.size.width * ratio).toInt()), max(1, (info.size.height * ratio).toInt()))
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
            value = decoded
            awaitDispose { decoded?.recycle() }
        } catch (e: kotlinx.coroutines.CancellationException) {
            decoded?.recycle()
            throw e
        } catch (_: Exception) { decoded?.recycle() }
    }
    Image((image ?: photo.thumbnail).asImageBitmap(), contentDescription = photo.name,
        modifier = Modifier.fillMaxWidth().aspectRatio(photo.thumbnail.width.toFloat() / photo.thumbnail.height)
            .clickable(onClick = onClick), contentScale = ContentScale.Fit)
}
