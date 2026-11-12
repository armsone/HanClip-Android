package com.hanclip.android.core.media

import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.ClipMediaKind
import com.hanclip.android.core.model.LivePhotoMode
import kotlin.math.max
import kotlin.random.Random

internal const val HanClipExportFrameRate = 30

internal data class PhotoZoomMotion(
    val zoomsIn: Boolean,
    val focalX: Float,
    val focalY: Float
)

internal fun randomPhotoZoomMotion(random: Random = Random.Default): PhotoZoomMotion {
    return PhotoZoomMotion(
        zoomsIn = random.nextBoolean(),
        focalX = random.nextDouble(0.15, 0.85).toFloat(),
        focalY = random.nextDouble(0.15, 0.85).toFloat()
    )
}

internal fun photoZoomScale(
    motion: PhotoZoomMotion,
    presentationTimeUs: Long,
    durationUs: Long
): Float {
    val linearProgress = presentationTimeUs.toDouble()
        .div(max(1L, durationUs).toDouble())
        .coerceIn(0.0, 1.0)
    val easedProgress = linearProgress * linearProgress * (3.0 - 2.0 * linearProgress)
    val zoomProgress = if (motion.zoomsIn) easedProgress else 1.0 - easedProgress
    return (1.0 + 0.10 * zoomProgress).toFloat()
}

internal fun ClipItem.usesMotionVideo(): Boolean {
    return mediaKind == ClipMediaKind.LivePhoto && livePhotoMode == LivePhotoMode.Motion
}

internal fun ClipItem.keepsSourceAudio(): Boolean {
    return keepsSourceAudio(mediaKind, livePhotoMode)
}

internal fun keepsSourceAudio(mediaKind: ClipMediaKind, livePhotoMode: LivePhotoMode): Boolean {
    return mediaKind == ClipMediaKind.Video ||
        (mediaKind == ClipMediaKind.LivePhoto && livePhotoMode == LivePhotoMode.Motion)
}

internal data class OriginalAudioRangeUs(val startUs: Long, val endUs: Long)

internal object BackgroundMusicDuckingPolicy {
    const val mergeGapUs = 400_000L
    const val rampDownUs = 150_000L
    const val rampUpUs = 250_000L
    const val duckedAbsoluteVolume = 0.15f

    fun ranges(clips: List<ClipItem>): List<OriginalAudioRangeUs> {
        var cursorUs = 0L
        val ranges = mutableListOf<OriginalAudioRangeUs>()
        clips.forEach { clip ->
            val durationUs = (clip.durationSeconds * 1_000_000.0).toLong().coerceAtLeast(0L)
            if (clip.keepsSourceAudio() && durationUs > 0L) {
                val next = OriginalAudioRangeUs(cursorUs, cursorUs + durationUs)
                val previous = ranges.lastOrNull()
                if (previous != null && next.startUs - previous.endUs <= mergeGapUs) {
                    ranges[ranges.lastIndex] = previous.copy(endUs = next.endUs)
                } else {
                    ranges += next
                }
            }
            cursorUs += durationUs
        }
        return ranges
    }

    fun relativeGainAt(positionUs: Long, baseVolume: Double, ranges: List<OriginalAudioRangeUs>): Float {
        if (ranges.isEmpty() || baseVolume <= 0.0) return 1f
        val duckedGain = (duckedAbsoluteVolume / baseVolume.toFloat()).coerceIn(0f, 1f)
        ranges.forEach { range ->
            val rampStart = (range.startUs - rampDownUs).coerceAtLeast(0L)
            if (positionUs in rampStart until range.startUs) {
                val progress = (positionUs - rampStart).toFloat() / (range.startUs - rampStart).coerceAtLeast(1L)
                return 1f + (duckedGain - 1f) * progress
            }
            if (positionUs in range.startUs..range.endUs) return duckedGain
            val rampEnd = range.endUs + rampUpUs
            if (positionUs in (range.endUs + 1)..rampEnd) {
                val progress = (positionUs - range.endUs).toFloat() / rampUpUs
                return duckedGain + (1f - duckedGain) * progress
            }
        }
        return 1f
    }
}

internal fun ClipItem.usesPhotoZoom(): Boolean {
    return usesPhotoZoom(mediaKind, livePhotoMode)
}

internal fun usesPhotoZoom(mediaKind: ClipMediaKind, livePhotoMode: LivePhotoMode): Boolean {
    return mediaKind == ClipMediaKind.Photo ||
        (mediaKind == ClipMediaKind.LivePhoto && livePhotoMode != LivePhotoMode.Motion)
}

internal data class WatermarkLayerPolicy(
    val rendersText: Boolean,
    val rendersLogo: Boolean
)

internal fun watermarkLayerPolicy(
    shouldRenderText: Boolean,
    logoEnabled: Boolean,
    isEndingInfoClip: Boolean
): WatermarkLayerPolicy {
    return WatermarkLayerPolicy(
        rendersText = shouldRenderText && !isEndingInfoClip,
        rendersLogo = logoEnabled
    )
}
