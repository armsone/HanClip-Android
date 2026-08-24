package com.hanclip.android.feature.aishot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealtimeImpactClassifierTest {
    @Test
    fun sharpImpactAboveNormalThresholdsTriggers() {
        val decision = RealtimeImpactClassifier.detectImpact(
            metrics = RealtimeImpactMetrics(rms = 0.06, peak = 0.30, crossingRate = 0.12),
            baseline = 0.008,
            previousRecentLevel = 0.008,
            sensitivity = ShotSensitivity.Normal
        )

        assertTrue(decision.isTriggered)
        assertTrue(decision.confidence > 0.0)
    }

    @Test
    fun speechLikePromptIsSuppressed() {
        // rms≥0.025, crest<3.15, cr<0.16, rise<4.8, score<0.18 → 트리거·confidence 0
        val metrics = RealtimeImpactMetrics(rms = 0.05, peak = 0.10, crossingRate = 0.05)
        val decision = RealtimeImpactClassifier.detectImpact(
            metrics = metrics,
            baseline = 0.03,
            previousRecentLevel = 0.05,
            sensitivity = ShotSensitivity.Quiet
        )

        assertFalse(decision.isTriggered)
        assertEquals(0.0, decision.confidence, 0.0)
    }

    @Test
    fun quietBaselineDoesNotTriggerOnFaintNoise() {
        val decision = RealtimeImpactClassifier.detectImpact(
            metrics = RealtimeImpactMetrics(rms = 0.004, peak = 0.02, crossingRate = 0.02),
            baseline = 0.008,
            previousRecentLevel = 0.008,
            sensitivity = ShotSensitivity.Loud
        )

        assertFalse(decision.isTriggered)
    }

    @Test
    fun automaticSensitivityFollowsBaselineGrades() {
        // baseline≥0.026 → 시끄러움 임계, ≤0.009 → 조용함 임계.
        val faintSharp = RealtimeImpactMetrics(rms = 0.012, peak = 0.075, crossingRate = 0.07)
        val quietDecision = RealtimeImpactClassifier.detectImpact(
            metrics = faintSharp,
            baseline = 0.005,
            previousRecentLevel = 0.005,
            sensitivity = ShotSensitivity.Auto
        )
        val loudDecision = RealtimeImpactClassifier.detectImpact(
            metrics = faintSharp,
            baseline = 0.03,
            previousRecentLevel = 0.03,
            sensitivity = ShotSensitivity.Auto
        )

        assertTrue(quietDecision.isTriggered)
        assertFalse(loudDecision.isTriggered)
    }
}
