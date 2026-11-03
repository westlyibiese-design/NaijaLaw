package com.westly.naijalaw.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * The NaijaLaw scales of justice, copied from the SVG in animation/splash_dark.html.
 * All shapes live in a 260 x 215 box (the SVG viewBox) and are scaled to the Canvas width.
 *
 * Use [ScalesMark] for the still picture. The splash uses [drawScales] directly,
 * because it also swings the beam and the two pans.
 */
const val SCALES_VIEW_WIDTH = 260f
const val SCALES_VIEW_HEIGHT = 215f
const val SCALES_ASPECT_RATIO = SCALES_VIEW_WIDTH / SCALES_VIEW_HEIGHT

/** Gold gradient of the SVG (#f6bd20 at the top, #e8a50b at the bottom), in viewBox units. */
val ScalesGoldBrush: Brush = Brush.verticalGradient(
    colors = listOf(Color(0xFFF6BD20), Color(0xFFE8A50B)),
    startY = 0f,
    endY = SCALES_VIEW_HEIGHT,
)

private val BeamStroke = Stroke(width = 7f, cap = StrokeCap.Round)
private val ChainStroke = Stroke(width = 3f, cap = StrokeCap.Round)

private val BeamPivot = Offset(130f, 58f)
private val LeftPanPivot = Offset(45f, 74f)
private val RightPanPivot = Offset(215f, 74f)

private fun parsePath(pathData: String): Path = PathParser().parsePathString(pathData).toPath()

/** All the paths, built once. Create it with remember { ScalesShapes() }. */
class ScalesShapes {
    val finial: Path = parsePath("M130 0C138 10 142 17 138 25C135 30 125 30 122 25C118 17 122 10 130 0Z")
    val stem: Path = Path().apply {
        addRoundRect(RoundRect(125f, 28f, 135f, 54f, CornerRadius(3f, 3f)))
    }
    val pillar: Path = parsePath("M121 62H139L136 160Q134 172 150 180H110Q126 172 124 160Z")
    val base: Path = parsePath("M92 180H168Q178 184 178 196V205H82V196Q82 184 92 180Z")

    val beam: Path = parsePath("M45 74Q75 62 108 58H152Q185 62 215 74")

    val leftChains: Path = parsePath("M45 74L5 134M45 74L85 134M45 74V134")
    val leftPan: Path = parsePath("M3 134H87C87 160 68 172 45 172C22 172 3 160 3 134Z")

    val rightChains: Path = parsePath("M215 74L175 134M215 74L255 134M215 74V134")
    val rightPan: Path = parsePath("M173 134H257C257 160 238 172 215 172C192 172 173 160 173 134Z")
}

/**
 * Draws the scales so that they fill the width of the DrawScope.
 *
 * @param beamDegrees how far the beam is turned around its centre (0 = level).
 * @param leftPanDegrees extra turn of the left pan around its hook (inside the beam turn).
 * @param rightPanDegrees extra turn of the right pan around its hook (inside the beam turn).
 */
fun DrawScope.drawScales(
    shapes: ScalesShapes,
    brush: Brush = ScalesGoldBrush,
    beamDegrees: Float = 0f,
    leftPanDegrees: Float = 0f,
    rightPanDegrees: Float = 0f,
) {
    val scale = size.width / SCALES_VIEW_WIDTH
    withTransform({ scale(scale, scale, Offset.Zero) }) {
        drawPath(path = shapes.finial, brush = brush)
        drawPath(path = shapes.stem, brush = brush)
        drawPath(path = shapes.pillar, brush = brush)
        drawPath(path = shapes.base, brush = brush)

        withTransform({ rotate(beamDegrees, BeamPivot) }) {
            drawPath(path = shapes.beam, brush = brush, style = BeamStroke)
            drawCircle(brush = brush, radius = 10f, center = BeamPivot)

            withTransform({ rotate(leftPanDegrees, LeftPanPivot) }) {
                drawCircle(brush = brush, radius = 6f, center = LeftPanPivot)
                drawPath(path = shapes.leftChains, brush = brush, style = ChainStroke)
                drawPath(path = shapes.leftPan, brush = brush)
            }
            withTransform({ rotate(rightPanDegrees, RightPanPivot) }) {
                drawCircle(brush = brush, radius = 6f, center = RightPanPivot)
                drawPath(path = shapes.rightChains, brush = brush, style = ChainStroke)
                drawPath(path = shapes.rightPan, brush = brush)
            }
        }
    }
}

/**
 * The still scales picture. Give it a width (or a size); the height follows the shape.
 * Pass [description] when the picture means something to the user; leave it null when decorative.
 */
@Composable
fun ScalesMark(
    modifier: Modifier = Modifier,
    brush: Brush = ScalesGoldBrush,
    description: String? = null,
) {
    val shapes = remember { ScalesShapes() }
    val semanticsModifier = if (description != null) {
        Modifier.semantics { contentDescription = description }
    } else {
        Modifier
    }
    Canvas(
        modifier = modifier
            .aspectRatio(SCALES_ASPECT_RATIO)
            .then(semanticsModifier),
    ) {
        drawScales(shapes = shapes, brush = brush)
    }
}
