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
