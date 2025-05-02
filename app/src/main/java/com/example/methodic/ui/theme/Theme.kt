package com.example.methodic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Modern color scheme inspired by productivity apps
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),        // Blue for primary actions
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF26A69A),      // Teal for secondary actions
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = Color(0xFF7C4DFF),       // Purple for accent elements
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF4527A0),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF616161),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),        // Light blue for dark theme
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF80CBC4),      // Light teal for dark theme
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00796B),
    onSecondaryContainer = Color(0xFFE0F2F1),
    tertiary = Color(0xFFB388FF),       // Light purple for dark theme
    onTertiary = Color(0xFF4527A0),
    tertiaryContainer = Color(0xFF5E35B1),
    onTertiaryContainer = Color(0xFFEDE7F6),
    background = Color(0xFF121212),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF212121),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),
    outline = Color(0xFF757575)
)

@Composable
fun MethodicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}