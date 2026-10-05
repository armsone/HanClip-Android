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
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.core.content.ContextCompat
import com.hanclip.android.core.theme.HanClipTheme
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
private fun PhotoSortLargePreview(photos: List<SortPhoto>, index: Int,
    onPage: (Int) -> Unit, onClose: () -> Unit) {
    val photo = photos[index]
    val context = LocalContext.current
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}/${photos.size} · ${photo.name}", Modifier.weight(1f).padding(start = 16.dp), maxLines = 1)
                    TextButton(onClick = onClose) { Text("닫기") }
                }
                key(photo.uri) {
                    var loadError by remember { mutableStateOf(false) }
                    val large by produceState<Bitmap?>(null, photo.uri) {
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
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    var viewport by remember { mutableStateOf(IntSize.Zero) }
                    val image = large ?: photo.thumbnail
                    val transform = rememberTransformableState { zoom, pan, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        val fit = min(viewport.width.toFloat() / image.width, viewport.height.toFloat() / image.height)
                        val xLimit = max(0f, (image.width * fit * scale - viewport.width) / 2)
                        val yLimit = max(0f, (image.height * fit * scale - viewport.height) / 2)
                        offset = Offset((offset.x + pan.x).coerceIn(-xLimit, xLimit), (offset.y + pan.y).coerceIn(-yLimit, yLimit))
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onSizeChanged { viewport = it }
                        .transformable(transform), contentAlignment = Alignment.Center) {
                        Image(image.asImageBitmap(), photo.name, Modifier.fillMaxSize().graphicsLayer {
                            scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y
                        }, contentScale = ContentScale.Fit)
                        if (large == null && !loadError) CircularProgressIndicator()
                    }
                    if (loadError) Text("큰 사진을 읽지 못해 작은 미리보기를 표시합니다.", Modifier.padding(horizontal = 16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = { onPage(index - 1) }, enabled = index > 0) { Text("이전") }
                        TextButton(onClick = { scale = 1f; offset = Offset.Zero }) { Text("전체 보기") }
                        TextButton(onClick = { onPage(index + 1) }, enabled = index < photos.lastIndex) { Text("다음") }
                    }
                    Text("두 손가락으로 확대하고 움직여 보세요.", Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
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
