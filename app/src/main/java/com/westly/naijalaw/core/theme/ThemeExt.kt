package com.westly.naijalaw.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance

/**
 * True when the dark colours are active (works for the phone setting now and the user's own choice in Phase 9).
 * The page background is white in the light theme and dark green in the dark theme.
 */
@Composable
@ReadOnlyComposable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
