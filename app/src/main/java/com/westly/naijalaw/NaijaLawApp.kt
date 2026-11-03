package com.westly.naijalaw

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.data.LawLoadState
import com.westly.naijalaw.core.prefs.ThemeMode
import com.westly.naijalaw.core.theme.NaijaLawTheme
import com.westly.naijalaw.feature.splash.SplashScreen
import com.westly.naijalaw.navigation.AppNavigation

/**
 * Root of the app: theme, then the animated splash on top of the navigation.
 *
 * The splash is composed first. The rest of the app is composed one frame later,
 * behind the splash, so the splash is always the first thing drawn.
 */
@Composable
fun NaijaLawApp() {
    // The user's Settings choices (read before the first frame, see AppData.start).
    val preferences by AppData.preferences.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (preferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    // Survives rotation and theme changes, so the splash plays only once.
    var splashDone by rememberSaveable { mutableStateOf(false) }

    // The splash leaves only when its animation is finished AND this is true.
    val dataReady by AppData.ready.collectAsState()
    val loadState by AppData.state.collectAsState()

    var appComposed by remember { mutableStateOf(splashDone) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        appComposed = true
    }

    SystemBars(darkTheme = darkTheme, splashVisible = !splashDone)

    NaijaLawTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (appComposed) {
                // Safety net: after the splash, a failed load shows a message instead of the app.
                // The text size is controlled inside the app: the phone's own font size is not used,
                // so a very large phone setting can never break the layout.
                val currentDensity = LocalDensity.current
                val appDensity = remember(currentDensity.density, preferences.textScale) {
                    Density(density = currentDensity.density, fontScale = preferences.textScale)
                }
                CompositionLocalProvider(LocalDensity provides appDensity) {
                    if (splashDone && loadState is LawLoadState.Failed) {
                        LoadErrorMessage()
                    } else {
                        AppNavigation()
                    }
                }
            }
            if (!splashDone) {
                SplashScreen(
                    darkTheme = darkTheme,
                    dataReady = dataReady,
                    onFinished = { splashDone = true },
                )
            }
        }
    }
}

/**
 * Edge to edge with the right icon colours.
 *  - On the splash: light icons on the dark splash, dark icons on the light splash.
 *  - After the splash: white status bar icons (the header is always green), and navigation bar
 *    icons that are readable on the page background.
 */
@Composable
private fun SystemBars(darkTheme: Boolean, splashVisible: Boolean) {
    val activity = LocalContext.current as ComponentActivity
    DisposableEffect(darkTheme, splashVisible) {
        val transparent = android.graphics.Color.TRANSPARENT
        // Navigation bar: light icons in dark theme, dark icons in light theme (splash and app).
        val navigationStyle = if (darkTheme) {
            SystemBarStyle.dark(transparent)
        } else {
            SystemBarStyle.light(transparent, transparent)
        }
        // Status bar: follows the theme on the splash; white icons on the green header afterwards.
        val statusStyle = if (splashVisible && !darkTheme) {
            SystemBarStyle.light(transparent, transparent)
        } else {
            SystemBarStyle.dark(transparent)
        }
        activity.enableEdgeToEdge(statusBarStyle = statusStyle, navigationBarStyle = navigationStyle)
        onDispose { }
    }
}

/** Full-screen message shown only if the law file could not be loaded. It should never appear. */
@Composable
private fun LoadErrorMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.load_error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}
