package com.hanclip.android.feature.aishot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiShotSoundlessPuttTest {
    @Test
    fun currentModelSupportsSoundlessPuttFallback() {
        assertEquals("0.6.0", AiShotModelVersion.current.displayName)
        assertTrue(AiShotModelVersion.current.supportsSoundlessPuttFallback)
        assertTrue(AiShotModelVersion.current.supportsPoseImpactWindow)
        assertFalse(AiShotModelVersion.V0_5_1.supportsSoundlessPuttFallback)
        assertFalse(AiShotModelVersion.V0_5_0.supportsPoseImpactWindow)
    }

    @Test
    fun validPuttSequenceConfirmsStroke() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        analyzer.observe(sample(1.0, handX = 0.25))
        analyzer.observe(sample(1.2, handX = 0.30))
        analyzer.observe(sample(1.4, handX = 0.22))
        analyzer.observe(sample(1.6, handX = 0.08))
        analyzer.observe(sample(1.8, handX = -0.06))
        val signal = analyzer.observe(sample(2.0, handX = -0.10))

        assertTrue(signal.isConfirmedStroke)
        assertTrue(signal.confidence >= 0.72)
        assertEquals(1.6, signal.strokeTimeSeconds!!, 1e-9)
        assertNotNull(analyzer.latchedConfirmedStroke(2.1))
    }

    @Test
    fun standingStillOnlyDoesNotConfirm() {
        val analyzer = GolfPuttStrokeAnalyzer()
        var time = 0.0
        repeat(20) {
            analyzer.observe(sample(time, handX = 0.0))
            time += 0.2
        }
        assertNull(analyzer.latchedConfirmedStroke(time))
    }

    @Test
    fun waggleBelowDisplacementThresholdStaysAddressed() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        val signal = analyzer.observe(sample(0.8, handX = 0.04))

        assertEquals(GolfPuttStrokePhase.Addressed, signal.phase)
        assertNull(analyzer.latchedConfirmedStroke(0.9))
    }

    @Test
    fun ironLikeBackswingAbovePeakCapResets() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        analyzer.observe(sample(1.0, handX = 0.40))
        val signal = analyzer.observe(sample(1.2, handX = 0.70))

        assertEquals(GolfPuttStrokePhase.SeekingAddress, signal.phase)
        analyzer.observe(sample(1.4, handX = -0.06))
        analyzer.observe(sample(1.6, handX = -0.10))
        assertNull(analyzer.latchedConfirmedStroke(1.7))
    }

    @Test
    fun fastReturnThroughAddressIsTreatedAsSwingAndResets() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        analyzer.observe(sample(1.0, handX = 0.30))
        analyzer.observe(sample(1.2, handX = 0.50))
        val signal = analyzer.observe(sample(1.4, handX = 0.02))

        assertEquals(GolfPuttStrokePhase.SeekingAddress, signal.phase)
        assertNull(analyzer.latchedConfirmedStroke(1.5))
    }

    @Test
    fun walkingCoreMotionResetsAnyPhase() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        val signal = analyzer.observe(sample(1.0, handX = 0.25, coreX = 0.7))

        assertEquals(GolfPuttStrokePhase.SeekingAddress, signal.phase)
    }

    @Test
    fun followThroughOvershootResets() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        analyzer.observe(sample(1.0, handX = 0.25))
        analyzer.observe(sample(1.2, handX = 0.30))
        analyzer.observe(sample(1.4, handX = 0.22))
        analyzer.observe(sample(1.6, handX = 0.08))
        val signal = analyzer.observe(sample(1.8, handX = -0.50))

        assertEquals(GolfPuttStrokePhase.SeekingAddress, signal.phase)
        assertNull(analyzer.latchedConfirmedStroke(1.9))
    }

    @Test
    fun confirmedStrokeLatchExpiresAndConsumes() {
        val analyzer = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer)
        analyzer.observe(sample(0.8, handX = 0.10))
        analyzer.observe(sample(1.0, handX = 0.25))
        analyzer.observe(sample(1.2, handX = 0.30))
        analyzer.observe(sample(1.4, handX = 0.22))
        analyzer.observe(sample(1.6, handX = 0.08))
        analyzer.observe(sample(1.8, handX = -0.06))
        analyzer.observe(sample(2.0, handX = -0.10))

        assertNotNull(analyzer.latchedConfirmedStroke(2.3))
        assertNull(analyzer.latchedConfirmedStroke(2.4))

        // 새 확정을 다시 만들고 consume이 latch를 비우는지 확인한다.
        val analyzer2 = GolfPuttStrokeAnalyzer()
        stillSamples(analyzer2)
        analyzer2.observe(sample(0.8, handX = 0.10))
        analyzer2.observe(sample(1.0, handX = 0.25))
        analyzer2.observe(sample(1.2, handX = 0.30))
        analyzer2.observe(sample(1.4, handX = 0.22))
        analyzer2.observe(sample(1.6, handX = 0.08))
        analyzer2.observe(sample(1.8, handX = -0.06))
        analyzer2.observe(sample(2.0, handX = -0.10))
        assertNotNull(analyzer2.latchedConfirmedStroke(2.1))
        analyzer2.consumeConfirmedStroke()
        assertNull(analyzer2.latchedConfirmedStroke(2.1))
    }

    @Test
    fun puttFusionPolicyRequiresEveryGate() {
        val stroke = GolfPuttStrokeSignal(
            phase = GolfPuttStrokePhase.ConfirmedStroke,
            confidence = 0.85,
            strokeTimeSeconds = 1.6
        )

        assertTrue(policy(stroke))
        assertFalse(policy(stroke.copy(confidence = 0.71)))
        assertFalse(policy(stroke, poseObservationConfidence = 0.71))
        assertFalse(policy(stroke, secondsSinceLatestPose = 0.36))
        assertFalse(policy(stroke, secondsSinceLatestVisualFrame = 0.36))
        assertFalse(policy(stroke, secondsSinceLastGlobalChange = 0.9))
        assertFalse(policy(stroke, isReady = false))
        assertFalse(policy(stroke, isInsideReadyPromptWindow = true))
        assertFalse(policy(stroke, isTriggerPending = true))
        assertFalse(policy(stroke, modelVersion = AiShotModelVersion.V0_5_1))
        assertFalse(policy(null))
        assertFalse(
            policy(
                stroke.copy(phase = GolfPuttStrokePhase.ForwardStroke)
            )
        )
    }

    private fun policy(
        stroke: GolfPuttStrokeSignal?,
        poseObservationConfidence: Double = 0.9,
        secondsSinceLatestPose: Double = 0.1,
        secondsSinceLatestVisualFrame: Double = 0.1,
        secondsSinceLastGlobalChange: Double = 5.0,
        isReady: Boolean = true,
        isInsideReadyPromptWindow: Boolean = false,
        isTriggerPending: Boolean = false,
        modelVersion: AiShotModelVersion = AiShotModelVersion.V0_6_0
    ): Boolean {
        return GolfPuttFusionPolicy.shouldTrigger(
            stroke = stroke,
            poseObservationConfidence = poseObservationConfidence,
            secondsSinceLatestPose = secondsSinceLatestPose,
            secondsSinceLatestVisualFrame = secondsSinceLatestVisualFrame,
            secondsSinceLastGlobalChange = secondsSinceLastGlobalChange,
            isReady = isReady,
            isInsideReadyPromptWindow = isInsideReadyPromptWindow,
            isTriggerPending = isTriggerPending,
            modelVersion = modelVersion
        )
    }

    private fun stillSamples(analyzer: GolfPuttStrokeAnalyzer) {
        listOf(0.0, 0.2, 0.4, 0.6).forEach { time ->
            analyzer.observe(sample(time, handX = 0.0))
        }
    }

    private fun sample(
        time: Double,
        handX: Double,
        coreX: Double = 0.5,
        confidence: Double = 0.9
    ) = GolfSwingPoseSample(
        timeSeconds = time,
        handX = handX,
        handY = 0.0,
        coreX = coreX,
        coreY = 0.5,
        bodyScale = 0.2,
        confidence = confidence
    )
}
