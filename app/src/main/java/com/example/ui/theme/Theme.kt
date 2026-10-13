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

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppAccent,
    onPrimary = Color.Black,
    primaryContainer = WhatsAppDarkBubbleOutgoing,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppTeal,
    onSecondary = Color.White,
    background = WhatsAppDarkBackground,
    onBackground = WhatsAppDarkTextPrimary,
    surface = WhatsAppDarkSurface,
    onSurface = WhatsAppDarkTextPrimary,
    surfaceVariant = WhatsAppDarkCard,
    onSurfaceVariant = WhatsAppDarkTextSecondary,
    tertiary = N8nOrange
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppPrimary,
    onPrimary = Color.White,
    primaryContainer = WhatsAppPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppTeal,
    onSecondary = Color.White,
    background = WhatsAppChatBg,
    onBackground = WhatsAppTextPrimary,
    surface = Color.White,
    onSurface = WhatsAppTextPrimary,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = WhatsAppTextSecondary,
    tertiary = N8nOrange
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded WhatsApp emerald & teal by default
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
