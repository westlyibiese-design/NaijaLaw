package com.westly.naijalaw.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.westly.naijalaw.core.theme.NaijaGreen

// All the icons below are drawn in code on a 24 x 24 grid. They are decorative
// (the law name is always next to them), so they have no content description.

private const val ICON_GRID = 24f
private const val DOCUMENT_PAGE_PATH =
    "M6.5 3H14L19 8V19.5Q19 21 17.5 21H6.5Q5 21 5 19.5V4.5Q5 3 6.5 3Z"

/**
 * The icon of a law, chosen by [LawInfo.iconKey][com.westly.naijalaw.core.data.LawInfo.iconKey]:
 * "scales", "gavel", "document" or "magnifier".
 *
 * @param color colour of the icon (white on the green tile).
 * @param cutout colour of the details inside the document icon (the tile colour behind it).
 */
@Composable
fun LawIcon(
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color = Color.White,
    cutout: Color = NaijaGreen,
) {
    val documentPage = remember { PathParser().parsePathString(DOCUMENT_PAGE_PATH).toPath() }
    when (iconKey) {
        "scales" -> ScalesMark(modifier = modifier.width(size + 6.dp), brush = SolidColor(color))
        "gavel" -> Canvas(modifier = modifier.size(size)) { drawGavel(color) }
        "document" -> Canvas(modifier = modifier.size(size)) { drawDocument(documentPage, color, cutout) }
        "magnifier" -> Canvas(modifier = modifier.size(size)) { drawMagnifier(color) }
        else -> Box(modifier = modifier.size(size))
    }
}

private fun DrawScope.drawGavel(color: Color) {
    val round = StrokeCap.Round
    withTransform({ scale(size.width / ICON_GRID, size.height / ICON_GRID, Offset.Zero) }) {
        // head, turned 45 degrees
        withTransform({ rotate(45f, Offset(14.5f, 8.5f)) }) {
            drawRoundRect(
                color = color,
                topLeft = Offset(9.5f, 5.3f),
                size = Size(10f, 6.4f),
                cornerRadius = CornerRadius(1.3f, 1.3f),
            )
        }
        // handle
        drawLine(color, Offset(12.3f, 10.7f), Offset(5f, 18f), strokeWidth = 2.4f, cap = round)
        // base
        drawLine(color, Offset(4f, 21.2f), Offset(16f, 21.2f), strokeWidth = 2.4f, cap = round)
    }
}

private fun DrawScope.drawDocument(page: Path, color: Color, cutout: Color) {
    val round = StrokeCap.Round
    withTransform({ scale(size.width / ICON_GRID, size.height / ICON_GRID, Offset.Zero) }) {
        drawPath(path = page, color = color)
        drawLine(cutout, Offset(8.5f, 11f), Offset(15.5f, 11f), strokeWidth = 1.5f, cap = round)
        drawLine(cutout, Offset(8.5f, 14f), Offset(15.5f, 14f), strokeWidth = 1.5f, cap = round)
        drawLine(cutout, Offset(8.5f, 17f), Offset(13f, 17f), strokeWidth = 1.5f, cap = round)
    }
}

private fun DrawScope.drawMagnifier(color: Color) {
    withTransform({ scale(size.width / ICON_GRID, size.height / ICON_GRID, Offset.Zero) }) {
        drawCircle(
            color = color,
            radius = 6.7f,
            center = Offset(10f, 10f),
            style = Stroke(width = 2.4f),
        )
        drawLine(
            color,
            Offset(15f, 15f),
            Offset(20.5f, 20.5f),
            strokeWidth = 2.8f,
            cap = StrokeCap.Round,
        )
    }
}
