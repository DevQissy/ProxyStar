package com.devqissy.proxystar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProxyStarColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF04283A),
    secondary = Purple,
    onSecondary = Color.White,
    tertiary = Blue,
    background = Bg0,
    onBackground = TextBright,
    surface = Card0,
    onSurface = TextBright,
    surfaceVariant = Card0,
    onSurfaceVariant = TextDim,
    error = Red,
    onError = Color.White,
    outline = TextFaint
)

@Composable
fun ProxyStarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProxyStarColors,
        typography = Typography,
        content = content
    )
}