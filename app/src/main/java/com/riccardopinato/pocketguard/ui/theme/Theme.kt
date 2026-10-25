package com.riccardopinato.pocketguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF12C2E9),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDDF8FF),
        onPrimaryContainer = Color(0xFF00313B),
        secondary = Color(0xFF5B6CFF),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3E7FF),
        onSecondaryContainer = Color(0xFF162166),
        tertiary = Color(0xFF3DDA91),
        onTertiary = Color(0xFF052A18),
        tertiaryContainer = Color(0xFFDDFBED),
        onTertiaryContainer = Color(0xFF082C1B),
        error = Color(0xFFFF5F6D),
        onError = Color.White,
        background = Color(0xFFF4F8FF),
        onBackground = Color(0xFF0B1220),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0E1628),
        surfaceVariant = Color(0xFFEAF1FF),
        onSurfaceVariant = Color(0xFF536078),
        outline = Color(0xFFD5DDEF)
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF62E5FF),
        onPrimary = Color(0xFF003540),
        primaryContainer = Color(0xFF004A59),
        onPrimaryContainer = Color(0xFFD8F8FF),
        secondary = Color(0xFFA4ADFF),
        onSecondary = Color(0xFF1D2875),
        secondaryContainer = Color(0xFF293A9E),
        onSecondaryContainer = Color(0xFFE2E6FF),
        tertiary = Color(0xFF63E6A5),
        onTertiary = Color(0xFF08331E),
        tertiaryContainer = Color(0xFF0F4E2E),
        onTertiaryContainer = Color(0xFFD8FBE9),
        error = Color(0xFFFF8C94),
        onError = Color(0xFF5E111B),
        background = Color(0xFF09101D),
        onBackground = Color(0xFFE8EEF9),
        surface = Color(0xFF121A2B),
        onSurface = Color(0xFFEAF0FF),
        surfaceVariant = Color(0xFF1B263B),
        onSurfaceVariant = Color(0xFFB8C4DD),
        outline = Color(0xFF33415C)
    )

@Composable
fun PocketGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
