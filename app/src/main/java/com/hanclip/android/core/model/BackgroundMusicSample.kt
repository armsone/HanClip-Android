package com.hanclip.android.core.model

import androidx.annotation.RawRes
import com.hanclip.android.R

enum class BackgroundMusicSample(
    val id: String,
    val title: String,
    val detail: String,
    @RawRes val rawResourceId: Int
) {
    DailyLoop(
        id = "daily-loop",
        title = "햇살 한 컷",
        detail = "잔잔한 생활 이야기",
        rawResourceId = R.raw.daily_loop
    ),
    TravelJoy(
        id = "travel-joy",
        title = "여행의 설렘",
        detail = "밝은 피아노와 퍼커션 여행",
        rawResourceId = R.raw.travel_joy
    ),
    AdClassicalDrama(
        id = "ad-classical-drama",
        title = "광고 클래식 드라마",
        detail = "오스티나토와 텐션",
        rawResourceId = R.raw.ad_classical_drama
    ),
    GolfLetsGo(
        id = "golf-lets-go",
        title = "골프치러 가자",
        detail = "경쾌한 출발과 기대감",
        rawResourceId = R.raw.golf_lets_go
    ),
    JiwooFirstSnowOriginal(
        id = "jiwoo-first-snow-original",
        title = "지우에게 첫눈이란",
        detail = "첫눈을 본 5살 아이의 감정",
        rawResourceId = R.raw.jiwoo_first_snow_original
    ),
    JiwooFirstSnow(
        id = "jiwoo-first-snow",
        title = "베이비 워킹",
        detail = "작고 경쾌한 첫걸음",
        rawResourceId = R.raw.jiwoo_first_snow
    );

    companion object {
        fun fromId(id: String?): BackgroundMusicSample? {
            return entries.firstOrNull { it.id == id }
        }
    }
}
