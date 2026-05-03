package com.app.ocrscanner.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Accent,
    onSecondary = OnAccent,
    secondaryContainer = AccentSoft,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = SurfaceSoftLight,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    error = Danger,
    errorContainer = DangerSoft,
)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = Color(0xFF0C2461),
    onPrimaryContainer = PrimaryContainer,
    secondary = Accent,
    onSecondary = OnAccent,
    secondaryContainer = Color(0xFF023850),
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceSoftDark,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark,
    error = Danger,
    errorContainer = DangerSoft,
)

@Composable
fun ScanlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScanlyTypography,
        content = content,
    )
}
