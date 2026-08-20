package com.hanclip.android.feature.aishot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiShotMotionFusionTest {
    @Test
    fun quietAddressBackswingAndDownswingOpensImpactWindow() {
        val analyzer = GolfSwingMotionAnalyzer()
        listOf(0.0, 0.3, 0.6).forEach { time ->
            analyzer.observe(sample(time, localMotion = 0.05))
        }
        analyzer.observe(sample(0.80, localMotion = 0.13))
        analyzer.observe(sample(0.95, localMotion = 0.15))
        analyzer.observe(sample(1.12, localMotion = 0.18))
        val signal = analyzer.observe(sample(1.30, localMotion = 0.27))

        assertTrue(signal.isImpactWindow)
        assertTrue(signal.confidence >= 0.72)
    }

    @Test
    fun globalCameraMotionDoesNotCreateImpactWindow() {
        val analyzer = GolfSwingMotionAnalyzer()
        listOf(0.0, 0.3, 0.6).forEach { time ->
            analyzer.observe(sample(time, localMotion = 0.05))
        }
        analyzer.observe(
            sample(
                time = 0.8,
                localMotion = 0.4,
                globalMotion = 0.2,
                widespreadMotion = 0.8
            )
        )
        val signal = analyzer.observe(
            sample(
                time = 0.9,
                localMotion = 0.5,
                globalMotion = 0.22,
                widespreadMotion = 0.82
            )
        )

        assertFalse(signal.isImpactWindow)
    }

    @Test
    fun fusionRejectsAudioWithoutMotionWhenVisualFrameExists() {
        val evidence = AiShotImpactEvidence(
            isTriggered = true,
            confidence = 1.0,
            peak = 0.3,
            impactScore = 0.2
        )
        val motion = GolfSwingMotionSignal(
            phase = GolfSwingMotionPhase.Addressed,
            confidence = 0.28,
            impactTimeSeconds = null
        )

        assertFalse(
            GolfSwingFusionPolicy.shouldTrigger(
                evidence = evidence,
                motion = motion,
                pose = null,
                referenceTimeSeconds = 1.0,
                requiresPoseConfirmation = false,
                hasRecentVisualFrame = true,
                isInsideReadyPromptWindow = false
            )
        )
    }

    @Test
    fun poseTrackerRecognizesReturnFromBackswing() {
        val analyzer = GolfSwingPoseAnalyzer()
        analyzer.observe(poseSample(0.0, handX = 0.0))
        analyzer.observe(poseSample(0.3, handX = 0.0))
        analyzer.observe(poseSample(0.6, handX = 0.0))
        analyzer.observe(poseSample(0.8, handX = 0.25))
        analyzer.observe(poseSample(1.0, handX = 0.50))
        analyzer.observe(poseSample(1.2, handX = 0.35))
        val signal = analyzer.observe(poseSample(1.4, handX = 0.15))

        assertTrue(signal.isImpactWindow(1.4))
        assertTrue(signal.confidence >= 0.72)
    }

    private fun sample(
        time: Double,
        localMotion: Double,
        globalMotion: Double = 0.04,
        widespreadMotion: Double = 0.2
    ) = GolfSwingVisualSample(
        timeSeconds = time,
        localMotion = localMotion,
        globalMotion = globalMotion,
        widespreadMotion = widespreadMotion,
        concentration = 0.6,
        brightnessChange = 0.02,
        dominantRegion = 1
    )

    private fun poseSample(time: Double, handX: Double) = GolfSwingPoseSample(
        timeSeconds = time,
        handX = handX,
        handY = 0.0,
        coreX = 0.5,
        coreY = 0.5,
        bodyScale = 0.2,
        confidence = 0.9
    )
}
