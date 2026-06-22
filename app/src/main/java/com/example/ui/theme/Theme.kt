package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryTN,
    secondary = SecondaryTN,
    tertiary = HighContrastYellow,
    background = DarkSlate,
    surface = DarkSlate
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TernaryLight,
    background = Color(0xFFF3F5F7), // Bento light background
    surface = Color(0xFFFFFFFF), // White cards
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF1E293B), // Slate 800
    outline = Color(0xFFE2E8F0) // Slate 200 border
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
