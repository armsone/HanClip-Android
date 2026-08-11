package com.hanclip.android.core.safety

import java.io.File
import kotlin.math.ceil

internal object StorageSpaceGuard {
    private const val SafetyReserveBytes = 64L * 1024L * 1024L
    private const val EstimatedVideoBitsPerPixelFrame = 0.10
    private const val EstimatedAudioBitsPerSecond = 192_000.0

    fun requiredImportBytes(sourceBytes: Long?): Long {
        return SafetyReserveBytes + (sourceBytes ?: 0L).coerceAtLeast(0L)
    }

    fun requiredExportBytes(
        width: Int,
        height: Int,
        frameRate: Int,
        durationSeconds: Double
    ): Long {
        val safeDuration = durationSeconds.coerceAtLeast(0.1)
        val videoBits = width.coerceAtLeast(1).toDouble() *
            height.coerceAtLeast(1).toDouble() *
            frameRate.coerceAtLeast(1).toDouble() *
            safeDuration *
            EstimatedVideoBitsPerPixelFrame
        val audioBits = EstimatedAudioBitsPerSecond * safeDuration
        val estimatedOutputBytes = ceil((videoBits + audioBits) / 8.0)
            .coerceAtMost(Long.MAX_VALUE.toDouble() - SafetyReserveBytes)
            .toLong()
        return SafetyReserveBytes + estimatedOutputBytes
    }

    fun requireAvailable(directory: File, requiredBytes: Long) {
        directory.mkdirs()
        val availableBytes = directory.usableSpace
        if (availableBytes < requiredBytes) {
            val requiredMiB = ceil(requiredBytes / (1024.0 * 1024.0)).toLong()
            val availableMiB = availableBytes / (1024L * 1024L)
            throw InsufficientStorageException(
                "저장 공간이 부족합니다. 약 ${requiredMiB}MB가 필요하고 ${availableMiB}MB를 사용할 수 있습니다. 공간을 확보한 뒤 다시 시도해 주세요."
            )
        }
    }
}

internal class InsufficientStorageException(message: String) : IllegalStateException(message)
