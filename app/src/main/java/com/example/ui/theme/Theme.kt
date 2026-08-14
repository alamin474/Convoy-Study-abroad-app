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
    primary = ConvoyNavyLight,
    onPrimary = Color.White,
    primaryContainer = ConvoyNavyPrimary,
    onPrimaryContainer = Color.White,
    secondary = ConvoyGoldLight,
    onSecondary = Color.Black,
    tertiary = ConvoyEmeraldLight,
    background = ConvoyDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = ConvoyDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ConvoyDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = ConvoyNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = ConvoyNavyPrimary,
    secondary = ConvoyGoldAccent,
    onSecondary = Color.White,
    tertiary = ConvoyEmerald,
    background = ConvoySlateBackground,
    onBackground = ConvoySlateTextPrimary,
    surface = ConvoySlateSurface,
    onSurface = ConvoySlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = ConvoySlateTextSecondary,
    outline = ConvoySlateBorder
)

@Composable
fun ConvoyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional Convoy brand colors
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ConvoyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
