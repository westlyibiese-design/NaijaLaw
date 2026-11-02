package com.westly.naijalaw.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = NaijaGreen,
    onPrimary = DarkText,
    secondary = NaijaGold,
    onSecondary = NaijaGreenDeep,
    tertiary = NaijaGold,
    onTertiary = NaijaGreenDeep,
    background = LightBackground,
    onBackground = LightText,
    surface = LightBackground,
    onSurface = LightText,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightCardBorder,
)

private val DarkColors = darkColorScheme(
    primary = NaijaGold,
    onPrimary = NaijaGreenDeep,
    secondary = NaijaGold,
    onSecondary = NaijaGreenDeep,
    tertiary = NaijaGold,
    onTertiary = NaijaGreenDeep,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkBackground,
    onSurface = DarkText,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCardBorder,
)

@Composable
fun NaijaLawTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NaijaTypography,
        content = content,
    )
}
