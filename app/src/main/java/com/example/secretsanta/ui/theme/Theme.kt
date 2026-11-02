package com.example.secretsanta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Red40,
    onPrimary = Neutral99,
    primaryContainer = Red90,
    onPrimaryContainer = Red10,
    secondary = Green40,
    onSecondary = Neutral99,
    secondaryContainer = Green90,
    onSecondaryContainer = Green10,
    tertiary = Gold40,
    onTertiary = Neutral99,
    tertiaryContainer = Gold90,
    onTertiaryContainer = Gold10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = Neutral90,
    outline = NeutralVariant50,
)

private val DarkColors = darkColorScheme(
    primary = Red80,
    onPrimary = Red30,
    primaryContainer = Red30,
    onPrimaryContainer = Red90,
    secondary = Green80,
    onSecondary = Green30,
    secondaryContainer = Green30,
    onSecondaryContainer = Green90,
    tertiary = Gold80,
    onTertiary = Gold30,
    tertiaryContainer = Gold30,
    onTertiaryContainer = Gold90,
    background = Neutral6,
    onBackground = Neutral90,
    surface = Neutral6,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral20,
    outline = NeutralVariant50,
)

@Composable
fun SecretSantaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // The festive palette is part of the app identity, so Material You is opt-in.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
