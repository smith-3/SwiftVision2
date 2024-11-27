package com.stellaridea.swiftvision.ui.theme

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

// Colores para el esquema oscuro
private val DarkColorSchemeCustom = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.Black, // Texto sobre componentes primarios en modo oscuro
    secondary = SoftBlue,
    onSecondary = Color.White, // Texto sobre componentes secundarios en modo oscuro
    background = DarkBackground,
    onBackground = Color.White, // Texto sobre el fondo oscuro
    surface = DarkBackground,
    onSurface = Color.White, // Texto sobre superficies en modo oscuro

    // Personalización adicional
    primaryContainer = EmeraldGreen.copy(alpha = 0.1f), // Contenedor para elementos primarios con un tono sutil
    onPrimaryContainer = EmeraldGreen, // Color de texto dentro del contenedor primario
    secondaryContainer = SoftBlue.copy(alpha = 0.1f), // Contenedor para secundarios en modo oscuro
    onSecondaryContainer = SoftBlue,
    surfaceVariant = Color.Black, // Fondo de TextField o Input en modo oscuro
    onSurfaceVariant = Color.White, // Texto sobre TextField en modo oscuro
    error = ErrorColor,
    onError = Color.White,
    outline = EmeraldGreen.copy(alpha = 0.7f), // Color de borde para inputs o outlines

)

// Colores para el esquema claro
private val LightColorSchemeCustom = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White, // Texto sobre componentes primarios en modo claro
    secondary = SoftBlue,
    onSecondary = Color.Black, // Texto sobre componentes secundarios en modo claro
    background = LightBackground,
    onBackground = Color.Black, // Texto sobre fondo claro
    surface = Color(0xFFF3F4F6), // Superficie en modo claro
    onSurface = Color.Black, // Texto sobre superficies en modo claro

    // Personalización adicional
    primaryContainer = EmeraldGreen.copy(alpha = 0.1f),
    onPrimaryContainer = EmeraldGreen,
    secondaryContainer = SoftBlue.copy(alpha = 0.1f),
    onSecondaryContainer = SoftBlue,
    surfaceVariant = TextFieldBackgroundLight, // Fondo de TextField o Input en modo claro
    onSurfaceVariant = Color.Black, // Texto sobre TextField en modo claro
    error = ErrorColor,
    onError = Color.White,
    outline = EmeraldGreen.copy(alpha = 0.7f)
)

@Composable
fun SwiftVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Cambia a true para soportar colores dinámicos en Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorSchemeCustom
        else -> LightColorSchemeCustom
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
