package com.example.fintechpagoflex.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FintechOrange,
    onPrimary = Color.White,
    primaryContainer = FintechOrangeContainerLight,
    onPrimaryContainer = FintechOrangeOnContainerLight,
    inversePrimary = FintechOrangeDark,
    secondary = DarkCharcoal,
    onSecondary = Color.White,
    secondaryContainer = CharcoalContainerLight,
    onSecondaryContainer = CharcoalOnContainerLight,
    tertiary = SteelBlue,
    onTertiary = Color.White,
    tertiaryContainer = SteelBlueContainerLight,
    onTertiaryContainer = SteelBlueOnContainerLight,
    background = SurfaceOffWhite,
    onBackground = OnSurfaceLight,
    surface = SurfaceOffWhite,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = FintechOrange,
    inverseSurface = DarkCharcoal,
    inverseOnSurface = Color.White,
    error = StatusWarning,
    onError = Color.White,
    errorContainer = StatusWarningContainer,
    onErrorContainer = StatusOnWarningContainer,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    scrim = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = FintechOrange,
    onPrimary = Color.White,
    primaryContainer = FintechOrangeContainerDark,
    onPrimaryContainer = FintechOrangeOnContainerDark,
    inversePrimary = FintechOrangeDark,
    secondary = DarkCharcoalLight,
    onSecondary = Color.White,
    secondaryContainer = CharcoalContainerDark,
    onSecondaryContainer = CharcoalOnContainerDark,
    tertiary = SteelBlueLight,
    onTertiary = Color(0xFF003549),
    tertiaryContainer = SteelBlueContainerDark,
    onTertiaryContainer = SteelBlueOnContainerDark,
    background = SurfaceDarkGraphite,
    onBackground = OnSurfaceDark,
    surface = SurfaceDarkGraphite,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = FintechOrange,
    inverseSurface = SurfaceOffWhite,
    inverseOnSurface = OnSurfaceLight,
    error = StatusWarning,
    onError = Color.White,
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = StatusWarningContainer,
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
    scrim = Color.Black
)

@Composable
fun FinTechPAGOFLEXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
