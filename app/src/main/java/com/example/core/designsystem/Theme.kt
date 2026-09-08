package com.example.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val FalsareeColorScheme = lightColorScheme(
    primary = FalsareeBluePrimary,
    onPrimary = Color.White,
    primaryContainer = FalsareeOrangeLight,
    onPrimaryContainer = FalsareeOrangeDark,
    secondary = FalsareeBlueNavy,
    onSecondary = Color.White,
    secondaryContainer = FalsareeBlueLight,
    onSecondaryContainer = FalsareeBlueDark,
    tertiary = FalsareeBlueCyan,
    onTertiary = FalsareeBlueNavy,
    background = FalsareeGray50,
    onBackground = FalsareeGray900,
    surface = Color.White,
    onSurface = FalsareeGray900,
    surfaceVariant = FalsareeGray100,
    onSurfaceVariant = FalsareeGray600,
    outline = FalsareeGray200,
    error = FalsareeRedPrimary,
    onError = Color.White,
    errorContainer = FalsareeRedLight,
    onErrorContainer = FalsareeRedDark
)

@Composable
fun FalsareeTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalFalsareeColors provides FalsareeColorTokens()
    ) {
        FalsareeRtlProvider {
            MaterialTheme(
                colorScheme = FalsareeColorScheme,
                typography = FalsareeArabicTypography,
                shapes = FalsareeShapes,
                content = content
            )
        }
    }
}
