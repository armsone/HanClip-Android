package com.hanclip.android.core.settings

import android.content.Context
import com.hanclip.android.core.model.CopyrightIconColorMode
import com.hanclip.android.core.model.WatermarkPlatform
import com.hanclip.android.core.model.WatermarkPosition
import com.hanclip.android.core.model.WatermarkSettings

object CopyrightWatermarkStore {
    private const val PreferencesName = "hanclip_settings"
    private const val KeyLogoEnabled = "hanClipCopyrightLogoEnabled"
    private const val KeyAddress = "hanClipCopyrightAddress"
    private const val KeyAddressPrefix = "hanClipCopyrightAddress."
    private const val KeyPlatform = "hanClipCopyrightPlatform"
    private const val KeyLogoColor = "hanClipCopyrightLogoColor"
    private const val KeyLogoShadowColor = "hanClipCopyrightLogoShadowColor"
    private const val KeyLogoShadowOpacity = "hanClipCopyrightLogoShadowOpacity"
    private const val KeyPosition = "hanClipCopyrightPosition"
    private const val KeyIconColorMode = "hanClipCopyrightIconColorMode"
    private const val KeyIconColor = "hanClipCopyrightIconColor"
    private const val KeyCustomIconPath = "hanClipCopyrightCustomIconPath"

    fun load(context: Context): WatermarkSettings {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        return WatermarkSettings(
            logoEnabled = preferences.getBoolean(KeyLogoEnabled, true),
            address = preferences.getString(
                addressKey(platform = WatermarkPlatform.fromStoredValue(
                    preferences.getString(KeyPlatform, WatermarkPlatform.HanClip.storedValue)
                )),
                preferences.getString(KeyAddress, "")
            ).orEmpty(),
            platform = WatermarkPlatform.fromStoredValue(
                preferences.getString(KeyPlatform, WatermarkPlatform.HanClip.storedValue)
            ),
            logoColorHex = preferences.getString(KeyLogoColor, "#007644") ?: "#007644",
            logoShadowColorHex = preferences.getString(KeyLogoShadowColor, "#29AB87")
                ?: "#29AB87",
            logoShadowOpacity = preferences.getFloat(KeyLogoShadowOpacity, 0.5f)
                .toDouble()
                .coerceIn(0.0, 1.0),
            copyrightPosition = enumValueOrDefault(
                preferences.getString(KeyPosition, null),
                WatermarkPosition.BottomTrailing
            ),
            copyrightIconColorMode = CopyrightIconColorMode.fromStoredValue(
                preferences.getString(KeyIconColorMode, null)
            ),
            copyrightIconColorHex = preferences.getString(KeyIconColor, "#007644") ?: "#007644",
            customCopyrightIconPath = preferences.getString(KeyCustomIconPath, "").orEmpty()
        )
    }

    fun save(context: Context, settings: WatermarkSettings) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KeyLogoEnabled, settings.logoEnabled)
            .putString(KeyAddress, settings.address)
            .putString(addressKey(settings.platform), settings.address)
            .putString(KeyPlatform, settings.platform.storedValue)
            .putString(KeyLogoColor, settings.logoColorHex)
            .putString(KeyLogoShadowColor, settings.logoShadowColorHex)
            .putFloat(KeyLogoShadowOpacity, settings.logoShadowOpacity.coerceIn(0.0, 1.0).toFloat())
            .putString(KeyPosition, settings.copyrightPosition.name)
            .putString(KeyIconColorMode, settings.copyrightIconColorMode.name)
            .putString(KeyIconColor, settings.copyrightIconColorHex)
            .putString(KeyCustomIconPath, settings.customCopyrightIconPath)
            .apply()
    }

    fun loadAddress(context: Context, platform: WatermarkPlatform): String {
        return context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(addressKey(platform), "")
            .orEmpty()
    }

    private fun addressKey(platform: WatermarkPlatform): String {
        return "$KeyAddressPrefix${platform.storedValue}"
    }
}

fun WatermarkSettings.withCopyrightWatermark(copyright: WatermarkSettings): WatermarkSettings {
    return copy(
        logoEnabled = copyright.logoEnabled,
        address = copyright.address,
        platform = copyright.platform,
        logoColorHex = copyright.logoColorHex,
        logoShadowColorHex = copyright.logoShadowColorHex,
        logoShadowOpacity = copyright.logoShadowOpacity,
        copyrightPosition = copyright.copyrightPosition,
        copyrightIconColorMode = copyright.copyrightIconColorMode,
        copyrightIconColorHex = copyright.copyrightIconColorHex,
        customCopyrightIconPath = copyright.customCopyrightIconPath
    )
}

fun WatermarkSettings.resetCopyrightWatermark(
    storedAddress: String,
    defaultTextColorHex: String,
    defaultShadowColorHex: String
): WatermarkSettings {
    return copy(
        logoEnabled = true,
        address = storedAddress,
        logoColorHex = defaultTextColorHex,
        logoShadowColorHex = defaultShadowColorHex,
        logoShadowOpacity = 0.5,
        copyrightPosition = WatermarkPosition.BottomTrailing,
        copyrightIconColorMode = CopyrightIconColorMode.Original,
        copyrightIconColorHex = "#007644"
    )
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T {
    return value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
