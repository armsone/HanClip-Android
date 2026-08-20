package com.hanclip.android.feature.aishot

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.hypot

internal class RealtimePoseDetector {
    private val detector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )
    private val requestInFlight = AtomicBoolean(false)

    fun analyze(
        bitmap: Bitmap,
        timeSeconds: Double,
        onResult: (GolfSwingPoseSample?) -> Unit
    ) {
        if (!requestInFlight.compareAndSet(false, true)) {
            bitmap.recycle()
            return
        }
        detector.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { pose ->
                onResult(pose.toGolfSwingPoseSample(timeSeconds, bitmap.width, bitmap.height))
            }
            .addOnFailureListener {
                onResult(null)
            }
            .addOnCompleteListener {
                bitmap.recycle()
                requestInFlight.set(false)
            }
    }

    fun close() {
        detector.close()
    }
}

private fun Pose.toGolfSwingPoseSample(
    timeSeconds: Double,
    imageWidth: Int,
    imageHeight: Int
): GolfSwingPoseSample? {
    fun landmark(type: Int, minimumConfidence: Float): PoseLandmark? {
        return getPoseLandmark(type)?.takeIf { it.inFrameLikelihood >= minimumConfidence }
    }

    val leftShoulder = landmark(PoseLandmark.LEFT_SHOULDER, 0.35f)
    val rightShoulder = landmark(PoseLandmark.RIGHT_SHOULDER, 0.35f)
    val leftHip = landmark(PoseLandmark.LEFT_HIP, 0.35f)
    val rightHip = landmark(PoseLandmark.RIGHT_HIP, 0.35f)
    val shoulders = listOfNotNull(leftShoulder, rightShoulder)
    val hips = listOfNotNull(leftHip, rightHip)
    if (shoulders.size + hips.size < 3 || shoulders.isEmpty() || hips.isEmpty()) return null

    fun average(points: List<PoseLandmark>): Pair<Double, Double> {
        return points.map { it.position.x.toDouble() / imageWidth.coerceAtLeast(1) }.average() to
            points.map { it.position.y.toDouble() / imageHeight.coerceAtLeast(1) }.average()
    }

    val midShoulder = average(shoulders)
    val midHip = average(hips)
    val axisX = midShoulder.first - midHip.first
    val axisY = midShoulder.second - midHip.second
    val bodyScale = hypot(axisX, axisY)
    if (bodyScale < 0.04) return null

    val leftWrist = landmark(PoseLandmark.LEFT_WRIST, 0.30f)
    val rightWrist = landmark(PoseLandmark.RIGHT_WRIST, 0.30f)
    val wrists = listOfNotNull(leftWrist, rightWrist)
    val hand = when {
        leftWrist != null && rightWrist != null -> average(wrists)
        leftWrist != null && leftWrist.inFrameLikelihood >= 0.55f &&
            landmark(PoseLandmark.LEFT_ELBOW, 0.40f) != null -> average(listOf(leftWrist))
        rightWrist != null && rightWrist.inFrameLikelihood >= 0.55f &&
            landmark(PoseLandmark.RIGHT_ELBOW, 0.40f) != null -> average(listOf(rightWrist))
        else -> return null
    }

    val unitX = axisX / bodyScale
    val unitY = axisY / bodyScale
    val lateralX = unitY
    val lateralY = -unitX
    val relativeX = hand.first - midHip.first
    val relativeY = hand.second - midHip.second
    val handX = (relativeX * lateralX + relativeY * lateralY) / bodyScale
    val handY = (relativeX * unitX + relativeY * unitY) / bodyScale
    val confidencePoints = shoulders + hips + wrists
    val confidence = confidencePoints.map { it.inFrameLikelihood.toDouble() }.average()
    if (confidence < 0.45) return null

    return GolfSwingPoseSample(
        timeSeconds = timeSeconds,
        handX = handX,
        handY = handY,
        coreX = midHip.first,
        coreY = midHip.second,
        bodyScale = bodyScale,
        confidence = confidence
    )
}
