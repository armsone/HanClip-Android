package com.hanclip.android.feature.editor

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SportsGolf
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.theme.HanClipPalette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val TrimPrimary = Color(0xFF0B7A4E)
private val TrimText = Color(0xFF14221A)
private val TrimSubText = Color(0xFF46564C)
private val TrimBorder = Color(0xFFD4DDD7)

private enum class WaveformDragMode {
    Start,
    End,
    Range
}

@Composable
fun VideoTrimSheet(
    clip: ClipItem,
    palette: HanClipPalette,
    onDismiss: () -> Unit,
    autoAdvanceOnLoad: Boolean = false,
    onAutoAdvanceConsumed: () -> Unit = {},
    onFirst: ((startSeconds: Double, durationSeconds: Double) -> Unit)? = null,
    onPrevious: ((startSeconds: Double, durationSeconds: Double) -> Unit)? = null,
    onNext: ((startSeconds: Double, durationSeconds: Double) -> Unit)? = null,
    onAutoNext: ((startSeconds: Double, durationSeconds: Double) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    bottomThumbnailStrip: (@Composable (startSeconds: Double, durationSeconds: Double) -> Unit)? = null,
    onApplyTrim: (startSeconds: Double, durationSeconds: Double) -> Unit
) {
    FullScreenDialogSystemBars(palette.solidPanel)
    val sourceDuration = max(0.1, clip.sourceDurationSeconds ?: clip.durationSeconds)
    val initialStartSeconds = remember(clip.id) {
        clip.trimStartSeconds.coerceIn(0.0, sourceDuration)
    }
    val initialDurationSeconds = remember(clip.id) {
        clip.durationSeconds.coerceIn(0.1, sourceDuration)
    }
    var startSeconds by rememberSaveable(clip.id) {
        mutableDoubleStateOf(initialStartSeconds)
    }
    var durationSeconds by rememberSaveable(clip.id) {
        mutableDoubleStateOf(initialDurationSeconds)
    }
    var autoAdvances by rememberSaveable(clip.id) { mutableStateOf(autoAdvanceOnLoad) }

    LaunchedEffect(clip.id) {
        if (autoAdvanceOnLoad) onAutoAdvanceConsumed()
    }

    LaunchedEffect(startSeconds, durationSeconds, sourceDuration) {
        if (startSeconds + durationSeconds > sourceDuration) {
            startSeconds = max(0.0, sourceDuration - durationSeconds)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        color = palette.solidPanel
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "구간 선택",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.text
                    )
                    Text(
                        text = "원본 ${formatSeconds(sourceDuration)}",
                        color = palette.subText
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        enabled = onPrevious != null,
                        onClick = { onPrevious?.invoke(startSeconds, durationSeconds) }
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = "이전 영상", tint = palette.text)
                    }
                    IconButton(onClick = { autoAdvances = !autoAdvances }) {
                        Icon(
                            Icons.Outlined.Repeat,
                            contentDescription = if (autoAdvances) "자동 진행 끄기" else "자동 진행 켜기",
                            tint = if (autoAdvances) palette.primary else palette.text
                        )
                    }
                    IconButton(
                        enabled = onNext != null,
                        onClick = { onNext?.invoke(startSeconds, durationSeconds) }
                    ) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = "다음 영상", tint = palette.text)
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Outlined.Delete, contentDescription = "영상 삭제", tint = Color(0xFFE45D42))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "닫기", tint = palette.text)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(72.dp)
                    .height(18.dp)
                    .pointerInput(clip.id, startSeconds, durationSeconds) {
                        var downwardDrag = 0f
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, amount ->
                                change.consume()
                                downwardDrag = (downwardDrag + amount).coerceAtLeast(0f)
                            },
                            onDragEnd = {
                                if (downwardDrag >= 55.dp.toPx()) {
                                    onApplyTrim(startSeconds, durationSeconds)
                                    onDismiss()
                                }
                                downwardDrag = 0f
                            },
                            onDragCancel = { downwardDrag = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .background(palette.border, RoundedCornerShape(50))
                )
            }

            VideoPreview(
                clip = clip,
                startSeconds = startSeconds,
                durationSeconds = durationSeconds,
                autoAdvances = autoAdvances,
                onAutoAdvance = when {
                    onAutoNext != null -> {
                        { onAutoNext(startSeconds, durationSeconds) }
                    }
                    onNext != null -> {
                        { onNext(startSeconds, durationSeconds) }
                    }
                    onFirst != null -> {
                        { onFirst(startSeconds, durationSeconds) }
                    }
                    else -> null
                }
            )

            ImpactWaveform(
                waveform = clip.audioWaveform,
                peaks = clip.audioPeakTimesSeconds.ifEmpty {
                    listOfNotNull(clip.audioPeakTimeSeconds)
                },
                sourceDuration = sourceDuration,
                startSeconds = startSeconds,
                durationSeconds = durationSeconds,
                palette = palette,
                onRangeChange = { nextStart, nextDuration ->
                    startSeconds = nextStart
                    durationSeconds = nextDuration
                }
            )

            bottomThumbnailStrip?.invoke(startSeconds, durationSeconds)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("선택 구간", color = TrimSubText)
                    Text(
                        "${formatSeconds(startSeconds)} - ${formatSeconds(startSeconds + durationSeconds)}",
                        color = TrimText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        impactInRangeText(clip, startSeconds, durationSeconds),
                        color = TrimSubText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            startSeconds = initialStartSeconds
                            durationSeconds = initialDurationSeconds
                        }
                    ) {
                        Text("리셋")
                    }
                    Button(
                        onClick = {
                            onApplyTrim(startSeconds, durationSeconds)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("선택 구간 적용")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VideoImpactPanel(
    clip: ClipItem,
    sourceDuration: Double,
    startSeconds: Double,
    durationSeconds: Double,
    palette: HanClipPalette,
    onCenterOnImpact: () -> Unit,
    onUseFullRange: () -> Unit,
    onRangeChange: (startSeconds: Double, durationSeconds: Double) -> Unit
) {
    val impactIncluded = isImpactInRange(clip, startSeconds, durationSeconds)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.chip,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("자동 타격점", color = palette.text, fontWeight = FontWeight.Bold)
                    Text(
                        "${impactSummaryText(clip)} · 선택한 길이는 유지합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.subText
                    )
                }
                ImpactRangeChip(
                    text = impactRangeChipText(clip, impactIncluded),
                    active = impactIncluded == true,
                    palette = palette
                )
            }
            ImpactWaveform(
                waveform = clip.audioWaveform,
                peaks = clip.audioPeakTimesSeconds.ifEmpty { listOfNotNull(clip.audioPeakTimeSeconds) },
                sourceDuration = sourceDuration,
                startSeconds = startSeconds,
                durationSeconds = durationSeconds,
                palette = palette,
                onRangeChange = onRangeChange
            )
            Text(
                text = impactSelectionGuideText(impactIncluded, durationSeconds),
                color = palette.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onCenterOnImpact,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Outlined.SportsGolf, contentDescription = null)
                    Text("타격점 중심 맞춤")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onUseFullRange,
                    border = BorderStroke(1.dp, palette.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = palette.solidPanel,
                        contentColor = palette.text
                    )
                ) {
                    Icon(Icons.Outlined.Timelapse, contentDescription = null)
                    Text("원본 전체 사용")
                }
            }
        }
    }
}

@Composable
private fun ImpactWaveform(
    waveform: List<Double>,
    peaks: List<Double>,
    sourceDuration: Double,
    startSeconds: Double,
    durationSeconds: Double,
    palette: HanClipPalette,
    onRangeChange: (startSeconds: Double, durationSeconds: Double) -> Unit
) {
    val bars = if (waveform.isEmpty()) List(48) { 0.18 } else waveform
    val latestStartSeconds by rememberUpdatedState(startSeconds)
    val latestDurationSeconds by rememberUpdatedState(durationSeconds)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .pointerInput(sourceDuration) {
                var dragMode = WaveformDragMode.Range
                var dragStartX = 0f
                var initialStart = 0.0
                var initialDuration = 0.1
                detectDragGestures(
                    onDragStart = { position ->
                        dragStartX = position.x
                        initialStart = latestStartSeconds
                        initialDuration = latestDurationSeconds
                        val startX = (initialStart / sourceDuration).toFloat() * size.width
                        val endX = ((initialStart + initialDuration) / sourceDuration).toFloat() * size.width
                        dragMode = when {
                            abs(position.x - startX) <= 32.dp.toPx() -> WaveformDragMode.Start
                            abs(position.x - endX) <= 32.dp.toPx() -> WaveformDragMode.End
                            else -> WaveformDragMode.Range
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val secondsAtPointer = change.position.x.toDouble()
                            .div(size.width.coerceAtLeast(1).toDouble())
                            .times(sourceDuration)
                            .coerceIn(0.0, sourceDuration)
                        when (dragMode) {
                            WaveformDragMode.Start -> {
                                val end = initialStart + initialDuration
                                val nextStart = secondsAtPointer.coerceAtMost(end - 0.1)
                                onRangeChange(nextStart, end - nextStart)
                            }
                            WaveformDragMode.End -> {
                                val nextEnd = secondsAtPointer.coerceAtLeast(initialStart + 0.1)
                                onRangeChange(initialStart, nextEnd - initialStart)
                            }
                            WaveformDragMode.Range -> {
                                val deltaSeconds = (change.position.x - dragStartX).toDouble() /
                                    size.width.coerceAtLeast(1).toDouble() * sourceDuration
                                val nextStart = (initialStart + deltaSeconds)
                                    .coerceIn(0.0, sourceDuration - initialDuration)
                                onRangeChange(nextStart, initialDuration)
                            }
                        }
                    }
                )
            }
            .semantics {
                contentDescription =
                    "오디오 파형, 선택 구간 ${"%.1f".format(startSeconds)}초부터 ${"%.1f".format(startSeconds + durationSeconds)}초, 타격점 ${peaks.size}개"
            }
    ) {
        val safeDuration = sourceDuration.coerceAtLeast(0.1)
        val selectedStartX = (startSeconds / safeDuration).toFloat() * size.width
        val selectedEndX = ((startSeconds + durationSeconds) / safeDuration).toFloat() * size.width
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
            val inSelection = x in selectedStartX..selectedEndX
            drawLine(
                color = if (inSelection) palette.primary else palette.subText.copy(alpha = 0.34f),
                start = Offset(x, (size.height - barHeight) / 2f),
                end = Offset(x, (size.height + barHeight) / 2f),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
        peaks
            .filter { it.isFinite() && it in 0.0..safeDuration }
            .take(12)
            .forEach { peak ->
                val x = (peak / safeDuration).toFloat() * size.width
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

@Composable
private fun ImpactRangeChip(
    text: String,
    active: Boolean,
    palette: HanClipPalette
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) palette.primary.copy(alpha = 0.14f) else Color.White,
        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.38f) else TrimBorder)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (active) palette.primary else TrimSubText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun impactSummaryText(clip: ClipItem): String {
    val peakCount = clip.audioPeakTimesSeconds.size
    val primary = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull()
    return when {
        primary != null && peakCount > 1 -> "주요 ${formatSeconds(primary)} · 후보 ${peakCount}개"
        primary != null -> "주요 ${formatSeconds(primary)}"
        else -> "피크가 없으면 영상 중앙을 기준으로 맞춥니다."
    }
}

private fun impactSelectionGuideText(
    impactIncluded: Boolean?,
    durationSeconds: Double
): String {
    val lengthText = "선택 길이 ${formatSeconds(durationSeconds)}"
    return when (impactIncluded) {
        true -> "$lengthText 유지 · 타격점이 완성본 구간 안에 있습니다."
        false -> "$lengthText 유지 · 타격점 중심 맞춤을 누르면 같은 길이로 다시 맞춥니다."
        null -> "$lengthText 유지 · 타격점 정보가 없으면 영상 중앙 기준으로 맞춥니다."
    }
}

@Composable
private fun TrimPrecisionControls(
    startSeconds: Double,
    durationSeconds: Double,
    sourceDuration: Double,
    onStartChange: (Double) -> Unit,
    onDurationChange: (Double) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("타격점 구간 미세 조정", color = palette.text, fontWeight = FontWeight.Bold)
            Text(
                "자주 쓰는 자동 컷 길이",
                color = palette.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2.0, 3.0).forEach { seconds ->
                    TrimDurationPresetButton(
                        seconds = seconds,
                        selected = abs(durationSeconds - seconds) < 0.05,
                        enabled = seconds <= sourceDuration,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onDurationChange(seconds) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(4.0, 6.0).forEach { seconds ->
                    TrimDurationPresetButton(
                        seconds = seconds,
                        selected = abs(durationSeconds - seconds) < 0.05,
                        enabled = seconds <= sourceDuration,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onDurationChange(seconds) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrimAdjustButton(
                    text = "앞으로 0.1초",
                    icon = { Icon(Icons.Outlined.Remove, contentDescription = null) },
                    enabled = startSeconds > 0.0,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChange(startSeconds - 0.1) }
                )
                TrimAdjustButton(
                    text = "뒤로 0.1초",
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    enabled = startSeconds < sourceDuration - durationSeconds,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChange(startSeconds + 0.1) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrimAdjustButton(
                    text = "짧게 0.1초",
                    icon = { Icon(Icons.Outlined.Remove, contentDescription = null) },
                    enabled = durationSeconds > 0.1,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onClick = { onDurationChange(durationSeconds - 0.1) }
                )
                TrimAdjustButton(
                    text = "길게 0.1초",
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    enabled = durationSeconds < sourceDuration,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onClick = { onDurationChange(durationSeconds + 0.1) }
                )
            }
        }
    }
}

@Composable
private fun TrimDurationPresetButton(
    seconds: Double,
    selected: Boolean,
    enabled: Boolean,
    palette: HanClipPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .height(48.dp)
            .semantics { this.selected = selected },
        enabled = enabled,
        onClick = onClick,
        border = BorderStroke(1.dp, if (selected) palette.primary else palette.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) palette.primary.copy(alpha = 0.12f) else palette.panel,
            contentColor = if (selected) palette.primary else palette.text,
            disabledContainerColor = palette.chip,
            disabledContentColor = palette.subText.copy(alpha = 0.55f)
        )
    ) {
        Text("%.1f초".format(seconds), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TrimAdjustButton(
    text: String,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    palette: HanClipPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier.height(48.dp),
        enabled = enabled,
        onClick = onClick,
        border = BorderStroke(1.dp, palette.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.solidPanel,
            contentColor = palette.text,
            disabledContainerColor = palette.chip,
            disabledContentColor = palette.subText.copy(alpha = 0.55f)
        )
    ) {
        icon()
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

private fun isImpactInRange(
    clip: ClipItem,
    startSeconds: Double,
    durationSeconds: Double
): Boolean? {
    val primary = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull()
        ?: return null
    return primary in startSeconds..(startSeconds + durationSeconds)
}

private fun impactRangeChipText(
    clip: ClipItem,
    included: Boolean?
): String {
    val primary = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull()
        ?: return "타격점 정보 없음"
    return if (included == true) {
        "타격점 포함 ${formatSeconds(primary)}"
    } else {
        "타격점 밖 ${formatSeconds(primary)}"
    }
}

private fun impactInRangeText(
    clip: ClipItem,
    startSeconds: Double,
    durationSeconds: Double
): String {
    val primary = clip.audioPeakTimeSeconds ?: clip.audioPeakTimesSeconds.firstOrNull()
        ?: return "타격점 정보 없음 · 선택한 구간만 사용"
    val endSeconds = startSeconds + durationSeconds
    return if (primary in startSeconds..endSeconds) {
        "타격점 ${formatSeconds(primary)} 포함 · 자동 컷에 적합"
    } else {
        "타격점 ${formatSeconds(primary)} 구간 밖 · 타격점 맞춤으로 조정"
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPreview(
    clip: ClipItem,
    startSeconds: Double,
    durationSeconds: Double,
    autoAdvances: Boolean,
    onAutoAdvance: (() -> Unit)?
) {
    val context = LocalContext.current
    val isSample = clip.sourceUri.scheme == "sample"
    val player = remember(clip.sourceUri) {
        if (isSample) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                setMediaItem(MediaItem.fromUri(clip.sourceUri))
                prepare()
            }
        }
    }

    LaunchedEffect(player, startSeconds) {
        player?.seekTo((startSeconds * 1000).toLong())
    }

    LaunchedEffect(player, startSeconds, durationSeconds, autoAdvances, onAutoAdvance) {
        val activePlayer = player ?: return@LaunchedEffect
        val startMs = (startSeconds * 1000).toLong()
        val endMs = ((startSeconds + durationSeconds) * 1000).toLong()
        if (autoAdvances) {
            activePlayer.seekTo(startMs)
            activePlayer.play()
        }
        while (true) {
            if (activePlayer.currentPosition >= endMs) {
                if (autoAdvances && onAutoAdvance != null) {
                    activePlayer.pause()
                    onAutoAdvance()
                    break
                } else if (autoAdvances) {
                    activePlayer.seekTo(startMs)
                    activePlayer.play()
                } else {
                    activePlayer.pause()
                    activePlayer.seekTo(endMs)
                }
            }
            delay(50L)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color.Black, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
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
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.height(64.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("실제 영상을 선택하면 여기서 재생됩니다.", color = Color.White)
            }
        }
    }
}

@Composable
private fun TrimSliderBlock(
    title: String,
    valueText: String,
    value: Double,
    valueRange: ClosedFloatingPointRange<Double>,
    onValueChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TrimText, fontWeight = FontWeight.SemiBold)
            Text(valueText, color = TrimPrimary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat()
        )
    }
}

private fun formatSeconds(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = seconds - minutes * 60
    return if (minutes > 0) {
        "%d:%04.1f".format(minutes, remainingSeconds)
    } else {
        "%.1f초".format(seconds)
    }
}
