package com.hanclip.android.core.media

import android.content.Context
import android.content.SharedPreferences

/**
 * Unified Byungki Common Player Policy & Settings.
 * Shared across image and video viewers according to COMMON.json.
 */
object ByungkiPlayerPolicy {

    // --- Image Policy Constants ---
    val INTERVAL_OPTIONS_SECONDS = listOf(1, 2, 3, 5, 10, 15, 30)
    const val DEFAULT_INTERVAL_SECONDS = 5
    const val DEFAULT_REPEAT_MODE_NAME = "ALL_REPEAT"

    enum class ImageRepeatMode(val label: String) {
        ALL_REPEAT("전체 반복"),
        SHUFFLE("임의 재생"),
        ONE_REPEAT("한 항목 반복"),
        ONCE("한 번 재생");

        companion object {
            fun fromName(name: String?): ImageRepeatMode {
                return entries.find { it.name.equals(name, ignoreCase = true) } ?: ALL_REPEAT
            }
        }
    }

    // --- Video Policy Constants ---
    const val DEFAULT_VIDEO_REPEAT = true
    const val DEFAULT_VIDEO_ASPECT = "fit" // "fit" or "fill"

    enum class VideoAspectMode(val label: String) {
        FIT("화면에 맞추기"),
        FILL("화면 채우기");

        companion object {
            fun fromMode(mode: String?): VideoAspectMode {
                return if (mode.equals("fill", ignoreCase = true)) FILL else FIT
            }
        }
    }

    // --- Zoom and Pan Epsilons ---
    const val IMAGE_MIN_ZOOM = 1.0f
    const val IMAGE_MAX_ZOOM = 6.0f

    const val VIDEO_MIN_ZOOM = 0.5f
    const val VIDEO_MAX_ZOOM = 4.0f

    const val ZOOM_RESET_EPSILON = 0.01f
    const val PAN_RESET_EPSILON_LOGICAL = 1.0f

    // --- Gesture & Navigation Thresholds (in dp) ---
    const val GESTURE_START_SLOP_DP = 12f
    const val DOWNWARD_DISMISS_DISTANCE_DP = 120f
    const val IMAGE_HORIZONTAL_NAV_DISTANCE_DP = 60f
    const val VOLUME_FULL_RANGE_LOGICAL_DP = 360f

    // --- Controls Timing & Sizes (in dp / ms) ---
    const val CONTROLS_HIDE_DELAY_MS = 2500L
    const val BUTTON_VISUAL_DP = 44f
    const val BUTTON_TOUCH_TARGET_DP = 48f
    const val ICON_SIZE_DP = 20f
    const val TITLE_TEXT_SP = 16f
    const val SECONDARY_TEXT_SP = 12f
    const val SPACING_DP = 8f

    // --- Standard Labels ---
    const val LABEL_CLOSE = "닫기"
    const val LABEL_SHARE = "공유"
    const val LABEL_PREVIOUS = "이전"
    const val LABEL_NEXT = "다음"
    const val LABEL_PLAY = "재생"
    const val LABEL_PAUSE = "일시정지"
    const val LABEL_ALL_REPEAT = "전체 반복"
    const val LABEL_SHUFFLE = "임의 재생"
    const val LABEL_ONE_REPEAT = "한 항목 반복"
    const val LABEL_ONCE = "한 번 재생"
    const val LABEL_FIT = "화면에 맞추기"
    const val LABEL_FILL = "화면 채우기"
    const val LABEL_RETRY = "다시 시도"

    // --- SharedPreferences Keys ---
    private const val PREFS_NAME = "byungki_common_player_prefs"
    private const val KEY_IMAGE_INTERVAL = "image_interval_seconds"
    private const val KEY_IMAGE_REPEAT_MODE = "image_repeat_mode"
    private const val KEY_VIDEO_REPEAT = "video_repeat_enabled"
    private const val KEY_VIDEO_ASPECT = "video_aspect_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getImageInterval(context: Context): Int {
        val interval = getPrefs(context).getInt(KEY_IMAGE_INTERVAL, DEFAULT_INTERVAL_SECONDS)
        return if (interval in INTERVAL_OPTIONS_SECONDS) interval else DEFAULT_INTERVAL_SECONDS
    }

    fun setImageInterval(context: Context, intervalSeconds: Int) {
        getPrefs(context).edit().putInt(KEY_IMAGE_INTERVAL, intervalSeconds).apply()
    }

    fun getImageRepeatMode(context: Context): ImageRepeatMode {
        val name = getPrefs(context).getString(KEY_IMAGE_REPEAT_MODE, DEFAULT_REPEAT_MODE_NAME)
        return ImageRepeatMode.fromName(name)
    }

    fun setImageRepeatMode(context: Context, mode: ImageRepeatMode) {
        getPrefs(context).edit().putString(KEY_IMAGE_REPEAT_MODE, mode.name).apply()
    }

    fun getVideoRepeat(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIDEO_REPEAT, DEFAULT_VIDEO_REPEAT)
    }

    fun setVideoRepeat(context: Context, repeat: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIDEO_REPEAT, repeat).apply()
    }

    fun getVideoAspect(context: Context): String {
        return getPrefs(context).getString(KEY_VIDEO_ASPECT, DEFAULT_VIDEO_ASPECT) ?: DEFAULT_VIDEO_ASPECT
    }

    fun setVideoAspect(context: Context, aspect: String) {
        getPrefs(context).edit().putString(KEY_VIDEO_ASPECT, aspect).apply()
    }
}
