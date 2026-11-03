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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.data.LawLoadState
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
    // Follows the phone for now. Phase 9 adds the user's own light or dark choice here.
    val darkTheme = isSystemInDarkTheme()

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
                if (splashDone && loadState is LawLoadState.Failed) {
                    LoadErrorMessage()
                } else {
                    AppNavigation()
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
 *  - After the splash: icons that are readable on the page background
 *    (light icons in dark theme, dark icons in light theme).
 *    Phase 5 changes the status bar icons to white for the green header.
 */
@Composable
private fun SystemBars(darkTheme: Boolean, splashVisible: Boolean) {
    val activity = LocalContext.current as ComponentActivity
    DisposableEffect(darkTheme, splashVisible) {
        val transparent = android.graphics.Color.TRANSPARENT
        val lightIcons = darkTheme   // same rule for the splash and for the page background
        val style = if (lightIcons) {
            SystemBarStyle.dark(transparent)
        } else {
            SystemBarStyle.light(transparent, transparent)
        }
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
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
