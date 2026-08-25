package com.hanclip.android.feature.aishot

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal enum class AiShotModelVersion(val displayName: String) {
    V0_4_0("0.4.0"),
    V0_5_0("0.5.0"),
    V0_5_1("0.5.1"),
    V0_6_0("0.6.0"),
    V0_7_0("0.7.0");

    val usesFusionPolicy: Boolean
        get() = ordinal >= V0_4_0.ordinal

    val supportsPoseImpactWindow: Boolean
        get() = ordinal >= V0_5_1.ordinal

    val supportsSoundlessPuttFallback: Boolean
        get() = ordinal >= V0_6_0.ordinal

    val requiresVisualShotEvidence: Boolean
        get() = this == V0_7_0

    val supportsVisualBackedWeakImpact: Boolean
        get() = this == V0_7_0

    companion object {
        // 롤백은 이 값 교체로 수행한다(iOS AudioImpactClassifier.currentModelVersion 대응).
        val current: AiShotModelVersion = V0_7_0
    }
}

internal enum class GolfSwingMotionPhase {
    SeekingAddress,
    Addressed,
    Backswing,
    Downswing
}

internal data class GolfSwingVisualSample(
    val timeSeconds: Double,
    val localMotion: Double,
    val globalMotion: Double,
    val widespreadMotion: Double,
    val concentration: Double,
    val brightnessChange: Double,
    val dominantRegion: Int
)

internal data class GolfSwingMotionSignal(
    val phase: GolfSwingMotionPhase,
    val confidence: Double,
    val impactTimeSeconds: Double?
) {
    val isImpactWindow: Boolean
        get() = phase == GolfSwingMotionPhase.Downswing && impactTimeSeconds != null
}

internal class GolfSwingMotionAnalyzer {
    private var phase = GolfSwingMotionPhase.SeekingAddress
    private var quietSince: Double? = null
    private var motionCandidateSince: Double? = null
    private var motionCandidateCount = 0
    private var backswingStart: Double? = null
    private var backswingPeak = 0.0
    private var backswingSamples = 0
    private var previousMotion = 0.0
    private var lockedRegion: Int? = null
    private var downswingTime: Double? = null
    private var globalMotionCount = 0

    // 무음 퍼팅 장면 안정성 판정용. reset()으로 지우지 않는다(마지막 전역 변화 시각 유지).
    var lastGlobalChangeTimeSeconds: Double = Double.NEGATIVE_INFINITY
        private set

    fun reset() {
        phase = GolfSwingMotionPhase.SeekingAddress
        quietSince = null
        motionCandidateSince = null
        motionCandidateCount = 0
        backswingStart = null
        backswingPeak = 0.0
        backswingSamples = 0
        previousMotion = 0.0
        lockedRegion = null
        downswingTime = null
        globalMotionCount = 0
    }

    fun observe(sample: GolfSwingVisualSample): GolfSwingMotionSignal {
        val isGlobalChange = sample.globalMotion >= 0.16 ||
            sample.widespreadMotion >= 0.68 ||
            sample.brightnessChange >= 0.14
        if (isGlobalChange) {
            lastGlobalChangeTimeSeconds = sample.timeSeconds
            globalMotionCount += 1
            if (globalMotionCount >= 2) reset()
            return currentSignal(sample.timeSeconds)
        }
        globalMotionCount = 0

        when (phase) {
            GolfSwingMotionPhase.SeekingAddress -> {
                val isQuiet = sample.localMotion <= 0.075 &&
                    sample.globalMotion <= 0.08 &&
                    sample.widespreadMotion <= 0.32
                if (isQuiet) {
                    quietSince = quietSince ?: sample.timeSeconds
                    if (sample.timeSeconds - (quietSince ?: sample.timeSeconds) >= 0.55) {
                        phase = GolfSwingMotionPhase.Addressed
                        motionCandidateSince = null
                        motionCandidateCount = 0
                    }
                } else {
                    quietSince = null
                }
            }
            GolfSwingMotionPhase.Addressed -> {
                val isBackswingCandidate = sample.localMotion >= 0.12 &&
                    sample.concentration >= 0.38 &&
                    sample.widespreadMotion in 0.04..0.58
                if (isBackswingCandidate) {
                    val since = motionCandidateSince
                    if (since != null &&
                        sample.timeSeconds - since <= 0.35 &&
                        abs((lockedRegion ?: sample.dominantRegion) - sample.dominantRegion) <= 1
                    ) {
                        motionCandidateCount += 1
                    } else {
                        motionCandidateSince = sample.timeSeconds
                        motionCandidateCount = 1
                        lockedRegion = sample.dominantRegion
                    }
                    if (motionCandidateCount >= 2) {
                        phase = GolfSwingMotionPhase.Backswing
                        backswingStart = motionCandidateSince
                        backswingPeak = sample.localMotion
                        backswingSamples = motionCandidateCount
                        previousMotion = sample.localMotion
                    }
                } else if (sample.localMotion <= 0.085) {
                    motionCandidateSince = null
                    motionCandidateCount = 0
                    lockedRegion = null
                }
            }
            GolfSwingMotionPhase.Backswing -> {
                val start = backswingStart
                if (start == null) {
                    reset()
                    return currentSignal(sample.timeSeconds)
                }
                val elapsed = sample.timeSeconds - start
                val movedToAnotherRegion = abs(
                    (lockedRegion ?: sample.dominantRegion) - sample.dominantRegion
                ) > 1 && sample.localMotion >= 0.18
                if (elapsed > 1.8 || movedToAnotherRegion) {
                    reset()
                    return currentSignal(sample.timeSeconds)
                }
                if (sample.localMotion >= 0.10) backswingSamples += 1
                val acceleration = sample.localMotion - previousMotion
                val previousPeak = backswingPeak
                val isDownswing = elapsed >= 0.18 &&
                    backswingSamples >= 3 &&
                    sample.localMotion >= 0.20 &&
                    (acceleration >= 0.035 || sample.localMotion >= max(0.26, previousPeak * 1.15))
                backswingPeak = max(backswingPeak, sample.localMotion)
                previousMotion = sample.localMotion
                if (isDownswing) {
                    phase = GolfSwingMotionPhase.Downswing
                    downswingTime = sample.timeSeconds
                }
            }
            GolfSwingMotionPhase.Downswing -> {
                if (sample.timeSeconds - (downswingTime ?: sample.timeSeconds) > 0.42) reset()
            }
        }
        return currentSignal(sample.timeSeconds)
    }

    fun currentSignal(timeSeconds: Double): GolfSwingMotionSignal {
        return when (phase) {
            GolfSwingMotionPhase.Downswing -> {
                val impactTime = downswingTime
                if (impactTime == null || timeSeconds - impactTime > 0.42) {
                    GolfSwingMotionSignal(GolfSwingMotionPhase.SeekingAddress, 0.0, null)
                } else {
                    GolfSwingMotionSignal(
                        GolfSwingMotionPhase.Downswing,
                        max(0.72, min(1.0, 0.62 + backswingPeak * 0.95)),
                        impactTime
                    )
                }
            }
            GolfSwingMotionPhase.Backswing -> GolfSwingMotionSignal(
                GolfSwingMotionPhase.Backswing,
                min(0.7, 0.34 + backswingPeak),
                null
            )
            GolfSwingMotionPhase.Addressed -> GolfSwingMotionSignal(
                GolfSwingMotionPhase.Addressed,
                0.28,
                null
            )
            GolfSwingMotionPhase.SeekingAddress -> GolfSwingMotionSignal(
                GolfSwingMotionPhase.SeekingAddress,
                0.0,
                null
            )
        }
    }
}

internal enum class GolfSwingPosePhase {
    SeekingAddress,
    Addressed,
    Backswing,
    ImpactWindow
}

internal data class GolfSwingPoseSample(
    val timeSeconds: Double,
    val handX: Double,
    val handY: Double,
    val coreX: Double,
    val coreY: Double,
    val bodyScale: Double,
    val confidence: Double
)

internal data class GolfSwingPoseSignal(
    val phase: GolfSwingPosePhase,
    val confidence: Double,
    val impactWindowStartSeconds: Double?,
    val impactWindowEndSeconds: Double?
) {
    fun isImpactWindow(timeSeconds: Double): Boolean {
        val start = impactWindowStartSeconds ?: return false
        val end = impactWindowEndSeconds ?: return false
        return phase == GolfSwingPosePhase.ImpactWindow && timeSeconds in start..end
    }
}

internal class GolfSwingPoseAnalyzer {
    private var phase = GolfSwingPosePhase.SeekingAddress
    private var quietSince: Double? = null
    private var addressX = 0.0
    private var addressY = 0.0
    private var addressSamples = 0
    private var previousSample: GolfSwingPoseSample? = null
    private var backswingDirectionX = 0.0
    private var backswingDirectionY = 0.0
    private var backswingStart: Double? = null
    private var peakProgress = 0.0
    private var previousProgress = 0.0
    private var downswingSamples = 0
    private var impactWindowStart: Double? = null
    private var impactWindowEnd: Double? = null
    private var latestConfidence = 0.0

    fun reset() {
        phase = GolfSwingPosePhase.SeekingAddress
        quietSince = null
        addressX = 0.0
        addressY = 0.0
        addressSamples = 0
        previousSample = null
        backswingDirectionX = 0.0
        backswingDirectionY = 0.0
        backswingStart = null
        peakProgress = 0.0
        previousProgress = 0.0
        downswingSamples = 0
        impactWindowStart = null
        impactWindowEnd = null
        latestConfidence = 0.0
    }

    fun observe(sample: GolfSwingPoseSample): GolfSwingPoseSignal {
        if (sample.confidence < 0.45 || sample.bodyScale < 0.04) {
            return currentSignal(sample.timeSeconds)
        }
        latestConfidence = sample.confidence
        previousSample?.let { previous ->
            val sampleGap = sample.timeSeconds - previous.timeSeconds
            val scaleChange = abs(sample.bodyScale - previous.bodyScale) / max(0.001, previous.bodyScale)
            if (sampleGap <= 0.0 || sampleGap > 0.6 || scaleChange > 0.25) reset()
        }
        val previous = previousSample

        when (phase) {
            GolfSwingPosePhase.SeekingAddress -> {
                if (previous == null) {
                    beginAddressAverage(sample)
                } else {
                    val handSpeed = hypot(sample.handX - previous.handX, sample.handY - previous.handY) /
                        max(0.05, sample.timeSeconds - previous.timeSeconds)
                    val coreSpeed = hypot(sample.coreX - previous.coreX, sample.coreY - previous.coreY) /
                        max(0.05, sample.timeSeconds - previous.timeSeconds) /
                        max(0.04, sample.bodyScale)
                    if (handSpeed <= 0.22 && coreSpeed <= 0.20) {
                        quietSince = quietSince ?: previous.timeSeconds
                        addToAddressAverage(sample)
                        if (sample.timeSeconds - (quietSince ?: sample.timeSeconds) >= 0.55 &&
                            addressSamples >= 3
                        ) {
                            phase = GolfSwingPosePhase.Addressed
                        }
                    } else {
                        quietSince = null
                        beginAddressAverage(sample)
                    }
                }
            }
            GolfSwingPosePhase.Addressed -> {
                val dx = sample.handX - addressX
                val dy = sample.handY - addressY
                val displacement = hypot(dx, dy)
                if (displacement >= 0.20) {
                    backswingDirectionX = dx / displacement
                    backswingDirectionY = dy / displacement
                    backswingStart = sample.timeSeconds
                    peakProgress = displacement
                    previousProgress = displacement
                    downswingSamples = 0
                    phase = GolfSwingPosePhase.Backswing
                }
            }
            GolfSwingPosePhase.Backswing -> {
                val start = backswingStart
                if (start == null || sample.timeSeconds - start > 1.8) {
                    reset()
                } else {
                    val progress = (sample.handX - addressX) * backswingDirectionX +
                        (sample.handY - addressY) * backswingDirectionY
                    peakProgress = max(peakProgress, progress)
                    val deltaTime = max(0.05, sample.timeSeconds - (previous?.timeSeconds ?: sample.timeSeconds - 0.2))
                    val returnSpeed = (previousProgress - progress) / deltaTime
                    if (sample.timeSeconds - start >= 0.18 &&
                        peakProgress >= 0.28 &&
                        progress < previousProgress &&
                        returnSpeed >= 0.55
                    ) {
                        downswingSamples += 1
                    } else if (progress >= previousProgress) {
                        downswingSamples = 0
                    }
                    previousProgress = progress
                    val returnedEnough = peakProgress - progress >= max(0.18, peakProgress * 0.55)
                    if (downswingSamples >= 2 && returnedEnough) {
                        phase = GolfSwingPosePhase.ImpactWindow
                        impactWindowStart = sample.timeSeconds - 0.45
                        impactWindowEnd = sample.timeSeconds + 0.30
                    }
                }
            }
            GolfSwingPosePhase.ImpactWindow -> {
                if (sample.timeSeconds > (impactWindowEnd ?: sample.timeSeconds)) reset()
            }
        }
        previousSample = sample
        return currentSignal(sample.timeSeconds)
    }

    fun currentSignal(timeSeconds: Double): GolfSwingPoseSignal {
        return when (phase) {
            GolfSwingPosePhase.ImpactWindow -> GolfSwingPoseSignal(
                phase,
                min(latestConfidence, min(1.0, 0.72 + peakProgress * 0.45)),
                impactWindowStart,
                impactWindowEnd
            )
            GolfSwingPosePhase.Backswing -> GolfSwingPoseSignal(
                phase,
                min(0.7, 0.35 + peakProgress),
                null,
                null
            )
            GolfSwingPosePhase.Addressed -> GolfSwingPoseSignal(phase, 0.32, null, null)
            GolfSwingPosePhase.SeekingAddress -> GolfSwingPoseSignal(phase, 0.0, null, null)
        }
    }

    private fun beginAddressAverage(sample: GolfSwingPoseSample) {
        addressX = sample.handX
        addressY = sample.handY
        addressSamples = 1
    }

    private fun addToAddressAverage(sample: GolfSwingPoseSample) {
        addressSamples += 1
        val weight = 1.0 / addressSamples.toDouble()
        addressX += (sample.handX - addressX) * weight
        addressY += (sample.handY - addressY) * weight
    }
}

internal data class AiShotImpactEvidence(
    val isTriggered: Boolean,
    val confidence: Double,
    val rms: Double,
    val peak: Double,
    val impactScore: Double,
    val crossingRate: Double
) {
    val crestFactor: Double
        get() = peak / max(0.001, rms)

    fun isVisualBackedWeakImpactCandidate(
        modelVersion: AiShotModelVersion = AiShotModelVersion.current
    ): Boolean = modelVersion.supportsVisualBackedWeakImpact &&
        peak >= 0.10 &&
        impactScore >= 0.065 &&
        crossingRate >= 0.08 &&
        crestFactor >= 3.5
}

internal object GolfSwingFusionPolicy {
    fun shouldTrigger(
        evidence: AiShotImpactEvidence,
        motion: GolfSwingMotionSignal,
        pose: GolfSwingPoseSignal?,
        referenceTimeSeconds: Double,
        requiresPoseConfirmation: Boolean,
        hasRecentVisualFrame: Boolean,
        isInsideReadyPromptWindow: Boolean,
        modelVersion: AiShotModelVersion = AiShotModelVersion.current
    ): Boolean {
        if (!hasRecentVisualFrame) {
            return evidence.isTriggered &&
                !modelVersion.requiresVisualShotEvidence &&
                !isInsideReadyPromptWindow
        }
        val motionImpactTime = motion.impactTimeSeconds
        val hasAlignedMotion = motion.isImpactWindow &&
            motion.confidence >= 0.72 &&
            motionImpactTime != null &&
            referenceTimeSeconds - motionImpactTime in -0.20..0.32
        val hasAlignedPose = modelVersion.supportsPoseImpactWindow &&
            (pose?.confidence ?: 0.0) >= 0.72 &&
            pose?.isImpactWindow(referenceTimeSeconds) == true
        if (!hasAlignedMotion && !hasAlignedPose) return false
        if (requiresPoseConfirmation && !hasAlignedPose) return false
        if (!evidence.isTriggered) {
            return !isInsideReadyPromptWindow &&
                evidence.isVisualBackedWeakImpactCandidate(modelVersion)
        }
        if (isInsideReadyPromptWindow) {
            return evidence.peak >= 0.16 && evidence.impactScore >= 0.08
        }
        return true
    }
}

internal enum class GolfPuttStrokePhase {
    SeekingAddress,
    Addressed,
    Backswing,
    ForwardStroke,
    ConfirmedStroke
}

internal data class GolfPuttStrokeSignal(
    val phase: GolfPuttStrokePhase,
    val confidence: Double,
    val strokeTimeSeconds: Double?
) {
    val isConfirmedStroke: Boolean
        get() = phase == GolfPuttStrokePhase.ConfirmedStroke && strokeTimeSeconds != null
}

internal class GolfPuttStrokeAnalyzer(
    modelVersion: AiShotModelVersion = AiShotModelVersion.current
) {
    private val confirmationLatchDuration = if (modelVersion == AiShotModelVersion.V0_7_0) 0.60 else 0.35
    private var phase = GolfPuttStrokePhase.SeekingAddress
    private var quietSince: Double? = null
    private var addressX = 0.0
    private var addressY = 0.0
    private var addressSamples = 0
    private var previousSample: GolfSwingPoseSample? = null
    private var directionX = 0.0
    private var directionY = 0.0
    private var backswingStart: Double? = null
    private var peakProgress = 0.0
    private var previousProgress = 0.0
    private var returningSamples = 0
    private var followThroughSamples = 0
    private var strokeTime: Double? = null
    private var sequenceMinimumConfidence = 1.0
    private var confirmedSignal: GolfPuttStrokeSignal? = null
    private var lastConfirmTime = Double.NEGATIVE_INFINITY

    fun reset() {
        phase = GolfPuttStrokePhase.SeekingAddress
        quietSince = null
        addressX = 0.0
        addressY = 0.0
        addressSamples = 0
        previousSample = null
        directionX = 0.0
        directionY = 0.0
        backswingStart = null
        peakProgress = 0.0
        previousProgress = 0.0
        returningSamples = 0
        followThroughSamples = 0
        strokeTime = null
        sequenceMinimumConfidence = 1.0
        confirmedSignal = null
    }

    fun latchedConfirmedStroke(nowSeconds: Double): GolfPuttStrokeSignal? {
        val signal = confirmedSignal ?: return null
        if (nowSeconds - lastConfirmTime > confirmationLatchDuration) {
            confirmedSignal = null
            return null
        }
        return signal
    }

    fun consumeConfirmedStroke() {
        confirmedSignal = null
    }

    fun observe(sample: GolfSwingPoseSample): GolfPuttStrokeSignal {
        if (sample.confidence < 0.45 || sample.bodyScale < 0.04) {
            return currentSignal()
        }
        val previous = previousSample
        previousSample = sample
        val deltaTime = previous?.let { max(0.05, sample.timeSeconds - it.timeSeconds) } ?: 0.2
        val coreSpeed = previous?.let {
            hypot(sample.coreX - it.coreX, sample.coreY - it.coreY) / deltaTime /
                max(0.04, sample.bodyScale)
        } ?: 0.0
        // 걷기·이동으로 판정하면 어느 단계든 리셋한다.
        if (coreSpeed > 0.9) {
            reset()
            previousSample = sample
            return currentSignal()
        }
        sequenceMinimumConfidence = min(sequenceMinimumConfidence, sample.confidence)

        when (phase) {
            GolfPuttStrokePhase.SeekingAddress -> {
                val handSpeed = previous?.let {
                    hypot(sample.handX - it.handX, sample.handY - it.handY) / deltaTime
                } ?: 0.0
                if (previous == null || (handSpeed <= 0.25 && coreSpeed <= 0.20)) {
                    quietSince = quietSince ?: sample.timeSeconds
                    addressSamples += 1
                    val weight = 1.0 / addressSamples.toDouble()
                    addressX += (sample.handX - addressX) * weight
                    addressY += (sample.handY - addressY) * weight
                    if (sample.timeSeconds - (quietSince ?: sample.timeSeconds) >= 0.60 &&
                        addressSamples >= 3
                    ) {
                        phase = GolfPuttStrokePhase.Addressed
                        sequenceMinimumConfidence = sample.confidence
                    }
                } else {
                    quietSince = null
                    addressSamples = 1
                    addressX = sample.handX
                    addressY = sample.handY
                }
            }
            GolfPuttStrokePhase.Addressed -> {
                val dx = sample.handX - addressX
                val dy = sample.handY - addressY
                val displacement = hypot(dx, dy)
                if (displacement >= 0.06) {
                    directionX = dx / displacement
                    directionY = dy / displacement
                    backswingStart = sample.timeSeconds
                    peakProgress = displacement
                    previousProgress = displacement
                    returningSamples = 0
                    phase = GolfPuttStrokePhase.Backswing
                }
            }
            GolfPuttStrokePhase.Backswing -> {
                val start = backswingStart
                if (start == null || sample.timeSeconds - start > 2.0) {
                    reset()
                    return currentSignal()
                }
                val progress = (sample.handX - addressX) * directionX +
                    (sample.handY - addressY) * directionY
                peakProgress = max(peakProgress, progress)
                // 아이언 반례 상한: 백스윙 폭이 퍼팅 범위를 넘으면 스윙으로 본다.
                if (peakProgress > 0.60) {
                    reset()
                    return currentSignal()
                }
                val returnSpeed = (previousProgress - progress) / deltaTime
                val addressPassThreshold = max(0.04, peakProgress * 0.30)
                if (progress <= addressPassThreshold && returnSpeed > 1.5) {
                    reset()
                    return currentSignal()
                }
                if (sample.timeSeconds - start >= 0.15 &&
                    peakProgress >= 0.12 &&
                    progress < previousProgress &&
                    returnSpeed >= 0.20
                ) {
                    returningSamples += 1
                } else if (progress >= previousProgress) {
                    returningSamples = 0
                }
                previousProgress = progress
                if (returningSamples >= 2 && progress <= addressPassThreshold) {
                    phase = GolfPuttStrokePhase.ForwardStroke
                    strokeTime = sample.timeSeconds
                    followThroughSamples = 0
                }
            }
            GolfPuttStrokePhase.ForwardStroke -> {
                val stroke = strokeTime
                if (stroke == null || sample.timeSeconds - stroke > 0.9) {
                    reset()
                    return currentSignal()
                }
                val progress = (sample.handX - addressX) * directionX +
                    (sample.handY - addressY) * directionY
                if (progress <= -max(0.30, peakProgress * 1.2)) {
                    reset()
                    return currentSignal()
                }
                if (progress <= -max(0.025, peakProgress * 0.15)) {
                    followThroughSamples += 1
                }
                previousProgress = progress
                if (followThroughSamples >= 2 &&
                    sample.timeSeconds - lastConfirmTime >= 2.0
                ) {
                    phase = GolfPuttStrokePhase.ConfirmedStroke
                    lastConfirmTime = sample.timeSeconds
                    confirmedSignal = GolfPuttStrokeSignal(
                        phase = GolfPuttStrokePhase.ConfirmedStroke,
                        confidence = min(
                            sequenceMinimumConfidence,
                            min(1.0, 0.62 + peakProgress * 0.9)
                        ),
                        strokeTimeSeconds = stroke
                    )
                }
            }
            GolfPuttStrokePhase.ConfirmedStroke -> {
                // 한 스트로크 중복 방지: 확정 뒤에는 다시 정지 탐색부터 시작한다.
                val latched = confirmedSignal
                reset()
                confirmedSignal = latched
                previousSample = sample
            }
        }
        return currentSignal()
    }

    private fun currentSignal(): GolfPuttStrokeSignal {
        val confirmed = confirmedSignal
        if (phase == GolfPuttStrokePhase.ConfirmedStroke && confirmed != null) return confirmed
        return GolfPuttStrokeSignal(phase, 0.0, null)
    }
}

internal object GolfPuttFusionPolicy {
    fun shouldTrigger(
        stroke: GolfPuttStrokeSignal?,
        poseObservationConfidence: Double,
        secondsSinceLatestPose: Double,
        secondsSinceLatestVisualFrame: Double,
        secondsSinceLastGlobalChange: Double,
        isReady: Boolean,
        isInsideReadyPromptWindow: Boolean,
        isTriggerPending: Boolean,
        modelVersion: AiShotModelVersion = AiShotModelVersion.current
    ): Boolean {
        if (!modelVersion.supportsSoundlessPuttFallback) return false
        if (stroke == null || !stroke.isConfirmedStroke) return false
        if (stroke.confidence < 0.72) return false
        if (poseObservationConfidence < 0.72) return false
        val maximumAge = if (modelVersion == AiShotModelVersion.V0_7_0) 0.55 else 0.35
        if (secondsSinceLatestPose > maximumAge) return false
        if (secondsSinceLatestVisualFrame > maximumAge) return false
        if (secondsSinceLastGlobalChange < 1.0) return false
        if (!isReady || isInsideReadyPromptWindow) return false
        if (isTriggerPending) return false
        return true
    }
}
