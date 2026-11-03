package com.westly.naijalaw.feature.splash

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.westly.naijalaw.R
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreenDeep
import com.westly.naijalaw.core.theme.SplashDarkBackground
import com.westly.naijalaw.core.theme.SplashLightBackground
import com.westly.naijalaw.core.ui.SCALES_ASPECT_RATIO
import com.westly.naijalaw.core.ui.ScalesGoldBrush
import com.westly.naijalaw.core.ui.ScalesShapes
import com.westly.naijalaw.core.ui.drawScales

/*
 * Native Compose copy of animation/splash_dark.html and animation/splash_light.html.
 *
 * Everything is a function of ONE clock (milliseconds since the first frame), so the delays,
 * durations and easings can be compared with the HTML line by line. The numbers in the comments
 * are the values from the HTML file.
 */

// ---------- Timeline (ms), from the CSS ----------
private const val SIDE_BAND_DELAY = 100f      // inL / inR 1.1s, delay .1s
private const val CENTRE_BAND_DELAY = 250f    // inC 1.1s, delay .25s
private const val BAND_MS = 1100f
private const val DROP_DELAY = 900f           // drop .8s, delay .9s
private const val DROP_MS = 800f
private const val SWING_DELAY = 1700f         // swing / swingL / swingR 1.1s, delay 1.7s
private const val SWING_MS = 1100f
private const val LOGO_DELAY = 2300f          // rise .5s, delay 2.3s
private const val LOGO_MS = 500f
private const val TRACK_DELAY = 2400f         // fadeIn .4s, delay 2.4s (track and version)
private const val TRACK_MS = 400f
private const val FILL_DELAY = 2550f          // load .95s, delay 2.55s
private const val FILL_MS = 950f
private const val TAG_DELAY = 2750f           // rise .5s, delay 2.75s
private const val TAG_MS = 500f

private const val ANIMATION_END_MS = 3500f    // the loading line is full
private const val HOLD_MS = 300f              // hold the final frame
private const val SHOW_FOR_MS = ANIMATION_END_MS + HOLD_MS
private const val EXIT_MS = 300f              // fade-out

/** When the phone has animations turned off: show the final frame this long, then go on. */
private const val REDUCED_MOTION_SHOW_MS = 800f

/** A time far after the end of the timeline, used to draw the final frame. */
private const val FINISHED_MS = 100_000f

// ---------- Easings (the CSS cubic-bezier values) ----------
private val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)            // ease-out
private val EaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)       // ease-in-out
private val BandEase: Easing = CubicBezierEasing(0.22f, 0.8f, 0.3f, 1f)       // --ease
private val DropEase: Easing = CubicBezierEasing(0.3f, 0.7f, 0.35f, 1f)       // drop
private val LoadEase: Easing = CubicBezierEasing(0.45f, 0.05f, 0.3f, 1f)      // load

// swing keyframes: 0%, 22%, 48%, 72%, 100%
private val SwingAt = floatArrayOf(0f, 0.22f, 0.48f, 0.72f, 1f)
private val BeamDegrees = floatArrayOf(0f, -7f, 6f, -3f, 0f)    // @keyframes swing
private val PanDegrees = floatArrayOf(0f, 7f, -6f, 3f, 0f)      // @keyframes swingL and swingR

private val FooterHeight = 30.dp

private fun Float.unit(): Float = coerceIn(0f, 1f)

private fun mix(from: Float, to: Float, fraction: Float): Float = from + (to - from) * fraction

/** 0..1 progress of an animation that starts at [delay] and lasts [duration]. */
private fun progress(t: Float, delay: Float, duration: Float): Float = ((t - delay) / duration).unit()

/** CSS keyframes with the same easing between every two keyframes. */
private fun keyframes(p: Float, at: FloatArray, values: FloatArray, easing: Easing): Float {
    for (i in 1 until at.size) {
        if (p <= at[i]) {
            val fraction = (p - at[i - 1]) / (at[i] - at[i - 1])
            return mix(values[i - 1], values[i], easing.transform(fraction))
        }
    }
    return values[values.size - 1]
}

// ---------------------------------------------------------------------------
// Colours of the two splash versions
// ---------------------------------------------------------------------------

private fun rgba(r: Int, g: Int, b: Int, a: Float): Color = Color(r / 255f, g / 255f, b / 255f, a)

/** Mix of two colours the way CSS gradients do it (premultiplied alpha). */
private fun mixPremultiplied(a: Color, b: Color, fraction: Float): Color {
    val alpha = mix(a.alpha, b.alpha, fraction)
    if (alpha <= 0f) return Color.Transparent
    val red = mix(a.red * a.alpha, b.red * b.alpha, fraction) / alpha
    val green = mix(a.green * a.alpha, b.green * b.alpha, fraction) / alpha
    val blue = mix(a.blue * a.alpha, b.blue * b.alpha, fraction) / alpha
    return Color(red.unit(), green.unit(), blue.unit(), alpha)
}

private fun colourAt(stops: Array<Pair<Float, Color>>, position: Float): Color {
    for (i in 1 until stops.size) {
        if (position <= stops[i].first) {
            val before = stops[i - 1]
            val after = stops[i]
            return mixPremultiplied(before.second, after.second, (position - before.first) / (after.first - before.first))
        }
    }
    return stops[stops.size - 1].second
}

/** CSS mask-image: linear-gradient(180deg, #000 0%, #000 55%, transparent 92%) */
private fun maskAt(position: Float): Float = when {
    position <= 0.55f -> 1f
    position >= 0.92f -> 0f
    else -> 1f - (position - 0.55f) / (0.92f - 0.55f)
}

/** A vertical band gradient with the fade-out mask of the HTML already built into it. */
private fun maskedBandBrush(stops: Array<Pair<Float, Color>>): Brush {
    val samples = floatArrayOf(0f, 0.45f, 0.55f, 0.65f, 0.75f, 0.85f, 0.92f, 1f)
    val masked = Array(samples.size) { i ->
        val colour = colourAt(stops, samples[i])
        samples[i] to colour.copy(alpha = colour.alpha * maskAt(samples[i]))
    }
    return Brush.verticalGradient(*masked)
}

private class SplashPalette(
    val stage: Color,
    val footerBackground: Color,
    val footerText: Color,
    val title: Color,
    val version: Color,
    val track: Color,
    val sideBand: Brush,
    val centreBand: Brush,
)

private val DarkPalette = SplashPalette(
    stage = SplashDarkBackground,                                   // --bg #03301a
    footerBackground = Color(0xFF021F11),                           // --bg-dark #021f11
    footerText = Color.White.copy(alpha = 0.6f),
    title = Color.White,
    version = Color.White.copy(alpha = 0.85f),
    track = Color.White.copy(alpha = 0.18f),
    sideBand = maskedBandBrush(
        arrayOf(0f to rgba(10, 110, 52, 0.45f), 1f to rgba(10, 110, 52, 0.2f)),
    ),
    centreBand = maskedBandBrush(
        arrayOf(
            0f to Color(0xFF04782F),
            0.45f to Color(0xFF055A28),
            1f to rgba(255, 255, 255, 0.04f),
        ),
    ),
)

private val LightPalette = SplashPalette(
    stage = SplashLightBackground,                                  // --bg #ffffff
    footerBackground = Color.White,
    footerText = Color(0xFF6B7280),
    title = NaijaGreenDeep,                                         // --green #05461f
    version = Color(0xFF6B7280),
    track = Color(0xFFE6E8EA),
    sideBand = maskedBandBrush(
        arrayOf(0f to rgba(150, 215, 178, 0.75f), 1f to rgba(150, 215, 178, 0.3f)),
    ),
    centreBand = maskedBandBrush(
        arrayOf(0f to rgba(235, 248, 240, 0.9f), 1f to rgba(255, 255, 255, 0.2f)),
    ),
)

// The three bottom waves (viewBox 0 0 400 100), gold, green, dark green
private const val WAVE_GOLD_PATH = "M0 18Q150 100 260 82T400 0V100H0Z"
private const val WAVE_GREEN_PATH = "M0 40Q150 112 260 94T400 22V100H0Z"
private const val WAVE_DARK_PATH = "M0 62Q150 120 260 108T400 50V100H0Z"
private val WaveGreen = Color(0xFF05743A)
private val WaveDarkGreen = Color(0xFF035A2C)

private class WavePaths {
    val gold: Path = PathParser().parsePathString(WAVE_GOLD_PATH).toPath()
    val green: Path = PathParser().parsePathString(WAVE_GREEN_PATH).toPath()
    val dark: Path = PathParser().parsePathString(WAVE_DARK_PATH).toPath()
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun animationsTurnedOff(context: Context): Boolean = try {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
} catch (e: Exception) {
    false
}

/** The real versionName from the package info, for example "1.0". */
@Suppress("DEPRECATION")
private fun readVersionName(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
} catch (e: Exception) {
    ""
}

// ---------------------------------------------------------------------------
// The splash screen
// ---------------------------------------------------------------------------

/**
 * The animated opening screen. It plays once.
 *
 * @param darkTheme dark version when true, light version when false.
 * @param dataReady the splash leaves only when its animation is finished AND this is true.
 * @param onFinished called once, after the splash has faded out.
 */
@Composable
fun SplashScreen(
    darkTheme: Boolean,
    dataReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val context = LocalContext.current
    val reduceMotion = remember { animationsTurnedOff(context) }

    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentDataReady by rememberUpdatedState(dataReady)

    // The one clock (ms) and the progress of the fade-out (0..1).
    // They are read only inside draw and graphicsLayer blocks, so they never recompose anything.
    val clock = remember { mutableFloatStateOf(0f) }
    val leave = remember { mutableFloatStateOf(0f) }
    val time: () -> Float = remember(reduceMotion) {
        { if (reduceMotion) FINISHED_MS else clock.floatValue }
    }

    LaunchedEffect(reduceMotion) {
        val showFor = if (reduceMotion) REDUCED_MOTION_SHOW_MS else SHOW_FOR_MS
        val start = withFrameNanos { it }
        var leaveStartedAt = -1f
        while (true) {
            val now = withFrameNanos { it }
            val t = (now - start) / 1_000_000f
            clock.floatValue = t
            if (leaveStartedAt < 0f && t >= showFor && currentDataReady) {
                leaveStartedAt = t
            }
            if (leaveStartedAt >= 0f) {
                val p = ((t - leaveStartedAt) / EXIT_MS).unit()
                leave.floatValue = p
                if (p >= 1f) {
                    currentOnFinished()
                    break
                }
            }
        }
    }

    val appName = stringResource(R.string.app_name)
    val tagline = stringResource(R.string.tagline)
    val copyright = stringResource(R.string.copyright)
    val versionName = remember { readVersionName(context) }
    val versionText = if (versionName.isBlank()) "" else stringResource(R.string.version_format, versionName)

    val scalesShapes = remember { ScalesShapes() }
    val wavePaths = remember { WavePaths() }
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.stage)
            .graphicsLayer { alpha = 1f - EaseInOut.transform(leave.floatValue) }
            .semantics { contentDescription = appName },
    ) {
        // ----- Stage: bands, scales, texts, loader, waves -----
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val stageWidth = maxWidth
            val stageHeight = maxHeight
            // CSS units: 1vw = 1 % of the width, 1vh = 1 % of the screen height
            val vw = stageWidth / 100f
            val vh = (stageHeight + FooterHeight) / 100f

            // Sizes from the CSS (clamp / min values), in dp and sp so they look the same on every phone
            val iconWidth = minOf(vw * 52f, vh * 32f, 300.dp)
            val logoSize = minOf(vw * 15f, vh * 9f).coerceIn(40.dp, 96.dp)
            val logoMarginTop = minOf(vh * 3f, 24.dp)
            val tagSize = minOf(vw * 7f, vh * 4.4f).coerceIn(20.dp, 44.dp)
            val tagMarginTop = minOf(vh * 2f, 16.dp)
            val versionSize = (vh * 2.2f).coerceIn(13.dp, 18.dp)
            val loaderWidth = minOf(vw * 62f, 420.dp)
            val loaderGap = minOf(vh * 2f, 16.dp)
            val loaderBottom = stageHeight * 0.11f + vh * 6f
            val wavesHeight = (stageHeight * 0.11f).coerceAtLeast(60.dp)

            // 1. The three vertical bands
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBands(palette, time())
            }

            // 2. Scales, "NaijaLaw" and the tagline, centred in the upper 78 % of the stage
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(stageHeight * 0.78f)
                    .padding(horizontal = vw * 5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .width(iconWidth)
                        .height(iconWidth / SCALES_ASPECT_RATIO)
                        .graphicsLayer {
                            // @keyframes drop: -70vh -> 2.5 % -> 0, opacity 0 -> 1 in the first 15 %
                            val p = progress(time(), DROP_DELAY, DROP_MS)
                            alpha = DropEase.transform((p / 0.15f).unit())
                            val settle = 0.025f * size.height
                            translationY = if (p < 0.7f) {
                                mix(-(vh * 70f).toPx(), settle, DropEase.transform(p / 0.7f))
                            } else {
                                mix(settle, 0f, DropEase.transform((p - 0.7f) / 0.3f))
                            }
                        },
                ) {
                    val swing = progress(time(), SWING_DELAY, SWING_MS)
                    val pan = keyframes(swing, SwingAt, PanDegrees, EaseInOut)
                    drawScales(
                        shapes = scalesShapes,
                        brush = ScalesGoldBrush,
                        beamDegrees = keyframes(swing, SwingAt, BeamDegrees, EaseInOut),
                        leftPanDegrees = pan,
                        rightPanDegrees = pan,
                    )
                }

                Spacer(modifier = Modifier.height(logoMarginTop))
                val logoFontSize = with(density) { logoSize.toSp() }
                Text(
                    text = appName,
                    color = palette.title,
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = logoFontSize,
                        lineHeight = logoFontSize,
                        letterSpacing = (-0.02).em,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None,
                        ),
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        val p = EaseOut.transform(progress(time(), LOGO_DELAY, LOGO_MS))
                        alpha = p
                        translationY = 12.dp.toPx() * (1f - p)   // @keyframes rise
                    },
                )

                Spacer(modifier = Modifier.height(tagMarginTop))
                Text(
                    text = tagline,
                    color = NaijaGold,
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { tagSize.toSp() },
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        val p = EaseOut.transform(progress(time(), TAG_DELAY, TAG_MS))
                        alpha = p
                        translationY = 12.dp.toPx() * (1f - p)
                    },
                )
            }

            // 3. Loading line and "Version 1.0"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = loaderBottom)
                    .width(loaderWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(loaderGap),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                ) {
                    val fade = EaseOut.transform(progress(time(), TRACK_DELAY, TRACK_MS))
                    if (fade > 0f) {
                        val radius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        drawRoundRect(
                            color = palette.track,
                            size = size,
                            cornerRadius = radius,
                            alpha = fade,
                        )
                        val fill = LoadEase.transform(progress(time(), FILL_DELAY, FILL_MS))
                        if (fill > 0f) {
                            drawRoundRect(
                                color = NaijaGold,
                                size = Size(size.width * fill, size.height),
                                cornerRadius = radius,
                                alpha = fade,
                            )
                        }
                    }
                }
                Text(
                    text = versionText,
                    color = palette.version,
                    style = TextStyle(fontSize = with(density) { versionSize.toSp() }),
                    modifier = Modifier.graphicsLayer {
                        alpha = EaseOut.transform(progress(time(), TRACK_DELAY, TRACK_MS))
                    },
                )
            }

            // 4. The curved gold and green waves at the bottom
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(wavesHeight),
            ) {
                withTransform({ scale(size.width / 400f, size.height / 100f, Offset.Zero) }) {
                    drawPath(path = wavePaths.gold, color = NaijaGold)
                    drawPath(path = wavePaths.green, color = WaveGreen)
                    drawPath(path = wavePaths.dark, color = WaveDarkGreen)
                }
            }
        }

        // ----- Footer, kept above the system navigation bar -----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.footerBackground)
                .navigationBarsPadding()
                .height(FooterHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = copyright,
                color = palette.footerText,
                style = TextStyle(
                    fontSize = with(density) { 12.dp.toSp() },
                    letterSpacing = 0.02.em,
                ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bands
// ---------------------------------------------------------------------------

/** .b1 left 22 % / 17 % wide, .b2 left 39 % / 22 % wide, .b3 left 61 % / 17 % wide. */
private fun DrawScope.drawBands(palette: SplashPalette, t: Float) {
    val width = size.width
    val height = size.height
    val sideWidth = width * 0.17f
    val centreWidth = width * 0.22f

    // b1: inL, slides in from the left (translateX -120 % of its own width)
    val left = BandEase.transform(progress(t, SIDE_BAND_DELAY, BAND_MS))
    if (left > 0f) {
        drawRect(
            brush = palette.sideBand,
            topLeft = Offset(width * 0.22f - sideWidth * 1.2f * (1f - left), 0f),
            size = Size(sideWidth, height),
            alpha = left,
        )
    }

    // b3: inR, slides in from the right
    val right = BandEase.transform(progress(t, SIDE_BAND_DELAY, BAND_MS))
    if (right > 0f) {
        drawRect(
            brush = palette.sideBand,
            topLeft = Offset(width * 0.61f + sideWidth * 1.2f * (1f - right), 0f),
            size = Size(sideWidth, height),
            alpha = right,
        )
    }

    // b2: inC, eases in from slightly above (translateY -8 % of its height)
    val centre = BandEase.transform(progress(t, CENTRE_BAND_DELAY, BAND_MS))
    if (centre > 0f) {
        drawRect(
            brush = palette.centreBand,
            topLeft = Offset(width * 0.39f, -height * 0.08f * (1f - centre)),
            size = Size(centreWidth, height),
            alpha = centre,
        )
    }
}
