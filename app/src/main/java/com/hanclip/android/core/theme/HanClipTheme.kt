package com.hanclip.android.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF072931),
    onPrimary = Color.White,
    primaryContainer = Color(0x14007E81),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF007E81),
    onSecondary = Color.White,
    tertiary = Color(0xFFE45D42),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0x14007E81),
    onSurfaceVariant = Color(0x941A1A1A),
    outline = Color(0x2E072931)
)

@Composable
fun HanClipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    HanClipThemeStore.revision
    val mode = HanClipThemeStore.load(LocalContext.current)
    val palette = mode.currentPalette
    val usesDarkColors = mode == HanClipThemeMode.Dark ||
        (mode == HanClipThemeMode.Automatic && darkTheme)
    val colors = if (usesDarkColors) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.Black,
            secondary = palette.secondary,
            onSecondary = Color.White,
            tertiary = Color(0xFFE45D42),
            background = palette.solidPanel,
            onBackground = palette.text,
            surface = palette.solidPanel,
            onSurface = palette.text,
            surfaceVariant = palette.panel,
            onSurfaceVariant = palette.subText,
            outline = palette.border
        )
    } else {
        LightColors.copy(
            primary = palette.primary,
            secondary = palette.secondary,
            background = palette.solidPanel,
            onBackground = palette.text,
            surface = palette.solidPanel,
            onSurface = palette.text,
            surfaceVariant = palette.panel,
            onSurfaceVariant = palette.subText,
            outline = palette.border
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = HanClipTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
