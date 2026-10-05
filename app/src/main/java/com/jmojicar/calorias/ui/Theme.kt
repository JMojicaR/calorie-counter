package com.jmojicar.calorias.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),      // green (food/health)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E4B0),
    secondary = Color(0xFF00695C),
    tertiary = Color(0xFFEF6C00),     // orange (accent)
    background = Color(0xFFFBFAF6),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1B)
)

@Composable
fun CaloriasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
