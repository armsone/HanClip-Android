package com.hanclip.android.core.media

import com.hanai.core.VideoFrameCandidate
import com.hanai.core.VideoHighlightConfiguration
import com.hanai.core.VideoHighlightSelector
import com.hanai.core.VideoProtectedAction
import com.hanclip.android.core.model.ClipProtectedAction
import kotlin.math.max
import kotlin.math.min

/** One source-video range chosen for a video segment child clip. */
internal data class VideoSegmentRange(
    val startSeconds: Double,
    val durationSeconds: Double,
    val peakSeconds: Double,
    val isProtectedAction: Boolean
)

/**
 * Adapter from HanClip's analysis to HanAI [VideoHighlightSelector].
 * Protected action windows (golf stroke context) are kept whole; ordinary
 * highlight peaks keep their usual centered window around the remaining time.
 */
internal object ProtectedActionSegmenter {
    /**
     * Returns null when there is no protected action evidence, so callers keep
     * the existing peak-centered segmentation unchanged.
     */
    fun ranges(
        peaks: List<Double>,
        protectedActions: List<ClipProtectedAction>,
        sourceDurationSeconds: Double,
        segmentDurationSeconds: Double
    ): List<VideoSegmentRange>? {
        if (protectedActions.isEmpty() ||
            !sourceDurationSeconds.isFinite() || sourceDurationSeconds <= 0.0
        ) {
            return null
        }

        val duration = min(max(0.1, segmentDurationSeconds), sourceDurationSeconds)
        // Peaks are already non-overlapping, so every one is kept as an anchor.
        val candidates = peaks.mapIndexed { index, peak ->
            VideoFrameCandidate(
                index = index,
                timeSeconds = peak.coerceIn(0.0, sourceDurationSeconds),
                qualityScore = 1.0
            )
        }
        val result = VideoHighlightSelector.select(
            candidates = candidates,
            distances = emptyList(),
            durationSeconds = sourceDurationSeconds,
            configuration = VideoHighlightConfiguration(
                frameCount = candidates.size,
                similarityThreshold = 0.0,
                minimumQuality = 0.0,
                minimumSpacingSeconds = 0.0,
                localDiversityWindowSeconds = 0.0,
                preRollSeconds = duration / 2.0,
                postRollSeconds = duration / 2.0,
                maxSegmentDurationSeconds = duration,
                totalDurationBudgetSeconds = sourceDurationSeconds,
                importanceWeight = 0.0
            ),
            protectedActions = protectedActions.map {
                VideoProtectedAction(
                    timeSeconds = it.timeSeconds,
                    isLoud = it.isLoud,
                    isSharpTransient = it.isSharpTransient
                )
            }
        )
        if (result.configurationErrors.isNotEmpty()) return null

        return result.segments.mapNotNull { segment ->
            val length = segment.endSeconds - segment.startSeconds
            if (length < 0.1) return@mapNotNull null
            val peak = segment.protectedActionIndices.firstOrNull()
                ?.let { protectedActions[it].timeSeconds }
                ?: segment.anchorIndices.firstOrNull()?.let { candidates[it].timeSeconds }
                ?: (segment.startSeconds + length / 2.0)
            VideoSegmentRange(
                startSeconds = segment.startSeconds,
                durationSeconds = length,
                peakSeconds = peak,
                isProtectedAction = segment.protectedActionIndices.isNotEmpty()
            )
        }
    }
}
