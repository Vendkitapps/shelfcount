package com.vendkit.shelfcount.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Yellow = Color(0xFFFFD43B)

private val Light = lightColorScheme(
    primary = Color(0xFF0F766E), onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF2EC), onPrimaryContainer = Color(0xFF053B37),
    secondary = Color(0xFF8A6D00), secondaryContainer = Color(0xFFFFF1B8), onSecondaryContainer = Color(0xFF3A2E00),
    background = Color(0xFFF6F7F5), onBackground = Color(0xFF0A0C0D),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF0A0C0D),
    surfaceVariant = Color(0xFFE7ECEA), onSurfaceVariant = Color(0xFF4A5552),
    outline = Color(0xFFC3CCC9), error = Color(0xFFB3261E),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF2DD4BF), onPrimary = Color(0xFF00201D),
    primaryContainer = Color(0xFF0F4F49), onPrimaryContainer = Color(0xFFCCF2EC),
    secondary = Yellow, secondaryContainer = Color(0xFF4A3B00), onSecondaryContainer = Color(0xFFFFF1B8),
    background = Color(0xFF0A0C0D), onBackground = Color(0xFFF4F7F5),
    surface = Color(0xFF15191A), onSurface = Color(0xFFF4F7F5),
    surfaceVariant = Color(0xFF222829), onSurfaceVariant = Color(0xFFA9B4B1),
    outline = Color(0xFF3A4442), error = Color(0xFFFFB4AB),
)

@Composable
fun ShelfCountTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
