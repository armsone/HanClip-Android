package com.hanclip.android.feature.editor

import com.hanclip.android.core.model.EndingInfoCardTheme
import com.hanclip.android.core.model.WatermarkSettings

internal fun captionSettingsDraftOnOpen(settings: WatermarkSettings): WatermarkSettings =
    if (settings.isEnabled) settings else settings.copy(isEnabled = true)

internal fun resetEndingSettingsDraft(settings: WatermarkSettings): WatermarkSettings {
    val defaults = WatermarkSettings()
    return settings.copy(
        includesEndingInfoCard = defaults.includesEndingInfoCard,
        endingInfoCardDuration = defaults.endingInfoCardDuration,
        endingInfoCardTheme = EndingInfoCardTheme.Caption,
        endingInfoCardVariation = defaults.endingInfoCardVariation,
        fontName = defaults.fontName,
        textColorHex = defaults.textColorHex,
        shadowEnabled = defaults.shadowEnabled,
        shadowOpacity = defaults.shadowOpacity,
        shadowColorHex = defaults.shadowColorHex,
        lineSpacing = defaults.lineSpacing,
        lineSpacingScale = defaults.lineSpacingScale,
        fontSize = defaults.fontSize
    )
}
