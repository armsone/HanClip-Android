package com.hanclip.android.feature.editor

import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.ClipMediaKind
import com.hanclip.android.core.model.LivePhotoMode
import kotlin.math.max
import kotlin.math.min

internal data class QuickDurationPlan(
    val defaultDurationSeconds: Double,
    val clips: List<ClipItem>
)

internal fun quickContentClips(clips: List<ClipItem>): List<ClipItem> = clips
    .map { clip ->
        if (clip.isVideoSegmentChild) clip.copy(isVideoSegmentSelected = true) else clip
    }
    .filter { clip -> !clip.isVideoSegmentParent && !clip.isHiddenSimilarPhotoGroupMember }

internal fun quickDurationCapacity(clip: ClipItem): Double {
    val usesVideoTimeline = clip.mediaKind == ClipMediaKind.Video ||
        (clip.mediaKind == ClipMediaKind.LivePhoto && clip.livePhotoMode == LivePhotoMode.Motion)
    return if (usesVideoTimeline) {
        max(
            0.1,
            clip.sourceDurationSeconds ?: clip.livePhotoDurationSeconds ?: clip.durationSeconds
        )
    } else {
        Double.POSITIVE_INFINITY
    }
}

internal fun quickEstimatedTotalDuration(
    clips: List<ClipItem>,
    targetDurationSeconds: Double,
    endingDurationSeconds: Double
): Double {
    val requestedDuration = max(0.1, targetDurationSeconds)
    val availableDuration = quickContentClips(clips).sumOf { clip ->
        min(requestedDuration, quickDurationCapacity(clip))
    }
    return min(requestedDuration, availableDuration) + max(0.0, endingDurationSeconds)
}

internal fun quickDurationPlan(
    clips: List<ClipItem>,
    targetDurationSeconds: Double
): QuickDurationPlan {
    val selectedClips = quickContentClips(clips)
    if (selectedClips.isEmpty()) {
        return QuickDurationPlan(defaultDurationSeconds = 0.1, clips = clips)
    }

    val requestedDuration = max(0.1, targetDurationSeconds)
    val allocatedDurations = allocateQuickDurations(
        capacities = selectedClips.map(::quickDurationCapacity),
        targetDurationSeconds = requestedDuration
    )
    val allocations = selectedClips.map(ClipItem::id).zip(allocatedDurations).toMap()

    val selectedById = selectedClips.associateBy(ClipItem::id)
    val updated = clips.map { original ->
        val selected = selectedById[original.id] ?: return@map original
        val duration = min(
            allocations[selected.id] ?: selected.durationSeconds,
            quickDurationCapacity(selected)
        )
        val usesVideoTimeline = selected.mediaKind == ClipMediaKind.Video ||
            (selected.mediaKind == ClipMediaKind.LivePhoto && selected.livePhotoMode == LivePhotoMode.Motion)
        val trimStart = if (usesVideoTimeline) {
            val capacity = quickDurationCapacity(selected)
            val center = selected.trimStartSeconds + selected.durationSeconds / 2.0
            max(0.0, min(capacity - duration, center - duration / 2.0))
        } else {
            0.0
        }
        selected.copy(
            isVideoSegmentSelected = true,
            durationSeconds = duration,
            photoDurationSeconds = duration,
            trimStartSeconds = trimStart
        )
    }

    return QuickDurationPlan(
        defaultDurationSeconds = max(0.1, requestedDuration / selectedClips.size.toDouble()),
        clips = updated
    )
}

internal fun allocateQuickDurations(
    capacities: List<Double>,
    targetDurationSeconds: Double
): List<Double> {
    if (capacities.isEmpty()) return emptyList()
    val allocations = MutableList(capacities.size) { 0.0 }
    val remaining = capacities.indices.toMutableList()
    var remainingDuration = max(0.1, targetDurationSeconds)

    while (remaining.isNotEmpty()) {
        val share = remainingDuration / remaining.size.toDouble()
        val capped = remaining.filter { capacities[it] < share }
        if (capped.isEmpty()) {
            remaining.forEach { allocations[it] = share }
            break
        }
        capped.forEach { index ->
            allocations[index] = capacities[index]
            remainingDuration = max(0.0, remainingDuration - capacities[index])
        }
        remaining.removeAll(capped.toSet())
    }
    return allocations
}
