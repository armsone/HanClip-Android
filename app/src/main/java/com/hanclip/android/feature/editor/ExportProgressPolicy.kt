package com.hanclip.android.feature.editor

import kotlin.math.roundToLong

internal class ExportOperationGate {
    private var currentToken: Long = 0

    fun begin(): Long = ++currentToken

    fun invalidate(): Long = ++currentToken

    fun isCurrent(token: Long): Boolean = token == currentToken
}

internal fun exportProgressMessage(
    progress: Float,
    elapsedMillis: Long,
    attemptLabel: String
): String {
    val safeProgress = progress.coerceIn(0f, 1f)
    val safeElapsedMillis = elapsedMillis.coerceAtLeast(0)
    val percent = (safeProgress * 100).toInt().coerceIn(0, 100)
    val timing = if (safeProgress >= 0.01f && safeProgress < 1f) {
        val totalMillis = (safeElapsedMillis / safeProgress).roundToLong()
        val remainingMillis = (totalMillis - safeElapsedMillis).coerceAtLeast(0)
        "처리 ${formatExportDuration(safeElapsedMillis)} · 예상 ${formatExportDuration(remainingMillis)} 남음"
    } else {
        "처리 ${formatExportDuration(safeElapsedMillis)}"
    }
    return "완성본을 만드는 중... $percent% · $timing · 완료 후 시사회로 이동 · $attemptLabel"
}

internal fun formatExportDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis.coerceAtLeast(0) / 1_000).coerceAtMost(99 * 60 + 59)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
