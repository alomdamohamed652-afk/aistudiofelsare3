package com.example.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FalsareeColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = StatusOrangeLight,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondaryLight,
    onSecondaryContainer = Color.White,
    tertiary = BrandAccent,
    onTertiary = BrandSecondary,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBackground,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = StatusRed,
    onError = Color.White,
    errorContainer = StatusRedLight,
    onErrorContainer = StatusRed
)

@Composable
fun FalsareeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FalsareeColorScheme,
        typography = FalsareeTypography,
        content = content
    )
}
