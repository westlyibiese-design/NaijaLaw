package com.westly.naijalaw.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.theme.NaijaGreenDark
import com.westly.naijalaw.core.theme.isDarkTheme

private val LightHeaderTop = Color(0xFF0D7D43)
private val DarkHeaderTop = Color(0xFF04371F)

/**
 * The curved green header shared by Home, Sections list, Section page and Favourites.
 * It draws behind the status bar and adds the status bar height to its own padding.
 * Its bottom edge is curved, with a thin gold curved line along that edge.
 *
 * @param title the title text, or null for none.
 * @param onBack shows a back arrow when not null.
 * @param trailing something at the right end (the star in Phase 7).
 * @param tall taller version. With [tallContent] it shows that content (the Home logo) instead of the title row.
 * @param centerTitle centre the title (true) or put it at the left (false).
 */
@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    tall: Boolean = false,
    centerTitle: Boolean = true,
    tallContent: (@Composable () -> Unit)? = null,
) {
    val dark = isDarkTheme()
    val curveDepth = if (tall) 34.dp else 26.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawHeaderBody(dark = dark, depth = curveDepth.toPx()) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = curveDepth * 0.9f),
        ) {
            if (tall && tallContent != null) {
                tallContent()
            } else {
                HeaderTitleRow(
                    title = title,
                    onBack = onBack,
                    trailing = trailing,
                    centerTitle = centerTitle,
                    extraPadding = tall,
                )
            }
        }
    }
}

@Composable
private fun HeaderTitleRow(
    title: String?,
    onBack: (() -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    centerTitle: Boolean,
    extraPadding: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (extraPadding) 88.dp else 56.dp)
            .padding(horizontal = 4.dp),
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White,
                )
            }
        }
        if (title != null) {
            val startPadding = if (centerTitle || onBack != null) 52.dp else 16.dp
            val endPadding = if (centerTitle || trailing != null) 52.dp else 16.dp
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
                modifier = Modifier
                    .align(if (centerTitle) Alignment.Center else Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(start = startPadding, end = endPadding),
            )
        }
        if (trailing != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
            ) {
                trailing()
            }
        }
    }
}

/**
 * Green body whose bottom edge is a soft curve (high at the left, lowest left of the middle,
 * rising at the right, as in the design images), with a thin gold line along the edge.
 */
private fun DrawScope.drawHeaderBody(dark: Boolean, depth: Float) {
    val w = size.width
    val h = size.height

    // Outer edge (the gold line's lower edge)
    val leftY = h - 0.6f * depth
    val rightY = h - depth
    val controlX = 0.4f * w
    val controlY = h + 0.8f * depth

    // Inner edge (where the green body ends): the same curve raised by the gold thickness
    val goldLeft = 5.dp.toPx()
    val goldMiddle = 8.dp.toPx()
    val goldRight = 1.5.dp.toPx()

    val body = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w, rightY - goldRight)
        quadraticBezierTo(controlX, controlY - goldMiddle, 0f, leftY - goldLeft)
        close()
    }
    val gold = Path().apply {
        moveTo(0f, leftY)
        quadraticBezierTo(controlX, controlY, w, rightY)
        lineTo(w, rightY - goldRight)
        quadraticBezierTo(controlX, controlY - goldMiddle, 0f, leftY - goldLeft)
        close()
    }

    val brush = if (dark) {
        Brush.verticalGradient(listOf(DarkHeaderTop, NaijaGreenDark), startY = 0f, endY = h)
    } else {
        Brush.verticalGradient(listOf(LightHeaderTop, NaijaGreen), startY = 0f, endY = h)
    }
    drawPath(path = body, brush = brush)
    drawPath(path = gold, color = NaijaGold)
}
