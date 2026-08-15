package com.hanclip.android.core.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class HanClipThemeMode(
    val storageValue: String,
    val displayName: String,
    val palette: HanClipPalette
) {
    Automatic(
        "automatic",
        "자동",
        HanClipPalette(
            primary = Color(0xFF072931),
            secondary = Color(0xFF007E81),
            background = Brush.verticalGradient(listOf(Color(0xFFF5F5F5), Color.White)),
            text = Color(0xFF1A1A1A),
            subText = Color(0x941A1A1A),
            solidPanel = Color.White,
            panel = Color(0x14007E81),
            chip = Color(0x29007E81),
            border = Color(0x2E072931)
        )
    ),
    Light(
        "light",
        "라이트",
        HanClipPalette(
            primary = Color(0xFF002228),
            secondary = Color(0xFF005C60),
            background = Brush.verticalGradient(listOf(Color(0xFFD2E7E5), Color(0xFFFAFEFD))),
            text = Color(0xFF00070C),
            subText = Color(0x9400070C),
            solidPanel = Color(0xFFE8F3F2),
            panel = Color(0x2E005C60),
            chip = Color(0x47005C60),
            border = Color(0x5C002228)
        )
    ),
    Dark(
        "dark",
        "다크",
        HanClipPalette(
            primary = Color(0xFF67E8F9),
            secondary = Color(0xFF527387),
            background = Brush.verticalGradient(listOf(Color(0xFF11181F), Color(0xFF0A0E12))),
            text = Color(0xFFE8EEF2),
            subText = Color(0xFFC9D3DC),
            solidPanel = Color(0xFF17222C),
            panel = Color(0x0FFFFFFF),
            chip = Color(0x14FFFFFF),
            border = Color(0x4D67E8F9)
        )
    ),
    BlossomGlow(
        "blossomGlow",
        "블로섬 글로우",
        HanClipPalette(
            primary = Color(0xFFB23E5D),
            secondary = Color(0xFF8B6897),
            background = Brush.verticalGradient(listOf(Color(0xFFF7EBF1), Color(0xFFFFF8FA))),
            text = Color(0xFF2D1F28),
            subText = Color(0xA62D1F28),
            solidPanel = Color(0xFFF6EDF1),
            panel = Color(0x1A8B6897),
            chip = Color(0x2E8B6897),
            border = Color(0x33B23E5D)
        )
    ),
    GrayscalePlay(
        "grayscalePlay",
        "그레이스케일 플레이",
        HanClipPalette(
            primary = Color(0xFF1C1C1E),
            secondary = Color(0xFF787880),
            background = Brush.verticalGradient(listOf(Color(0xFFE2E2E5), Color(0xFFF7F7F8))),
            text = Color(0xFF121214),
            subText = Color(0x99121214),
            solidPanel = Color(0xFFF0F0F2),
            panel = Color(0x1F787880),
            chip = Color(0x2E787880),
            border = Color(0x3D1C1C1E)
        )
    ),
    PixelPop(
        "pixelPop",
        "픽셀 팝",
        HanClipPalette(
            primary = Color(0xFF2652FF),
            secondary = Color(0xFFDC2F65),
            background = Brush.verticalGradient(listOf(Color(0xFFE8EFFF), Color(0xFFF9FBFF))),
            text = Color(0xFF0F1630),
            subText = Color(0xFF56617F),
            solidPanel = Color(0xFFF0F4FF),
            panel = Color(0x162652FF),
            chip = Color(0x26DC2F65),
            border = Color(0x3D2652FF)
        )
    );

    companion object {
        val baseModes = listOf(Automatic, Light, Dark)
        val customModes = listOf(BlossomGlow, GrayscalePlay, PixelPop)
        val visibleModes = baseModes + customModes

        fun fromStoredValueOrNull(value: String): HanClipThemeMode? =
            entries.firstOrNull { it.storageValue == value }

        fun fromStoredValue(value: String?): HanClipThemeMode {
            return entries.firstOrNull { it.storageValue == value }
                ?: when (value) {
                    "readableComfort" -> Light
                    "rosyBrown", "electricCobalt" -> Automatic
                    else -> Automatic
                }
        }
    }
}

data class HanClipPalette(
    val primary: Color,
    val secondary: Color,
    val background: Brush,
    val text: Color,
    val subText: Color,
    val solidPanel: Color,
    val panel: Color,
    val chip: Color,
    val border: Color
)

private val AutomaticDarkPalette = HanClipPalette(
    primary = Color(0xFF67E8F9),
    secondary = Color(0xFF527387),
    background = Brush.verticalGradient(listOf(Color(0xFF353834), Color(0xFF373A36))),
    text = Color.White,
    subText = Color(0xA8FFFFFF),
    solidPanel = Color(0xFF373A36),
    panel = Color(0x14527387),
    chip = Color(0x24527387),
    border = Color(0x3867E8F9)
)

val HanClipThemeMode.currentPalette: HanClipPalette
@Composable get() = if (this == HanClipThemeMode.Automatic && isSystemInDarkTheme()) {
        AutomaticDarkPalette
    } else {
        palette
    }

object HanClipThemeStore {
    private const val PreferencesName = "hanclip_home_theme_preferences"
    private const val ThemeModeKey = "hanClipThemeMode"
    private const val CustomThemeOrderKey = "hanClipCustomThemeOrder"
    internal var revision by mutableIntStateOf(0)
        private set

    fun load(context: Context): HanClipThemeMode {
        val raw = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(ThemeModeKey, null)
        return HanClipThemeMode.fromStoredValue(raw)
    }

    fun save(context: Context, mode: HanClipThemeMode) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(ThemeModeKey, mode.storageValue)
            .apply()
        revision += 1
    }

    fun loadVisibleOrder(context: Context): List<HanClipThemeMode> {
        val storedModes = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(CustomThemeOrderKey, null)
            .orEmpty()
            .split(',')
            .mapNotNull(HanClipThemeMode::fromStoredValueOrNull)
            .filter { it in HanClipThemeMode.customModes }
            .distinct()
        return HanClipThemeMode.baseModes + storedModes +
            HanClipThemeMode.customModes.filterNot(storedModes::contains)
    }

    fun saveCustomOrder(context: Context, modes: List<HanClipThemeMode>) {
        val normalized = modes.filter { it in HanClipThemeMode.customModes }.distinct() +
            HanClipThemeMode.customModes.filterNot(modes::contains)
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(CustomThemeOrderKey, normalized.joinToString(",") { it.storageValue })
            .apply()
    }
}
