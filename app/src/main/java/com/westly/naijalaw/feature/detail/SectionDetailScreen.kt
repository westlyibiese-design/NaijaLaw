package com.westly.naijalaw.feature.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.data.LawLoadState
import com.westly.naijalaw.core.data.LawSection
import com.westly.naijalaw.core.theme.LawSerif
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.theme.isDarkTheme
import com.westly.naijalaw.core.ui.AppHeader
import com.westly.naijalaw.core.ui.NumberBadge

private val LawTextStyle = TextStyle(
    fontFamily = LawSerif,
    fontStyle = FontStyle.Italic,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private const val COLLAPSED_LINES = 6
private val ParagraphIndent = 12.dp
private val DarkOriginalHeader = Color(0xFF35503F)

/**
 * The page of one section: Pidgin explanation first, the original law text below it,
 * and a star at the top right that saves the section on the phone.
 * An unknown section shows "no results" with the back arrow (it never crashes).
 *
 * @param lawId kept for the route contract; only the Criminal Code Act has data for now.
 */
@Composable
fun SectionDetailScreen(
    @Suppress("UNUSED_PARAMETER") lawId: String,
    sectionId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadState by AppData.state.collectAsState()
    val section: LawSection? = (loadState as? LawLoadState.Ready)?.index?.byId?.get(sectionId)

    if (section == null) {
        Column(modifier = modifier.fillMaxSize()) {
            AppHeader(onBack = onBack)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val favouriteIds by AppData.favouriteIds.collectAsState()
    val isFavourite = section.id in favouriteIds

    val titleRes = if (section.isAct) R.string.act_section_title_format else R.string.section_title_format

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(
            title = stringResource(titleRes, section.displayNumber),
            onBack = onBack,
            trailing = {
                FavouriteStar(
                    saved = isFavourite,
                    onClick = { AppData.toggleFavourite(section.id) },
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TitleRow(section = section)

            Spacer(modifier = Modifier.height(16.dp))

            // Pidgin explanation first
            val dark = isDarkTheme()
            LawCard(
                title = stringResource(R.string.pidgin_card_title),
                headerColor = NaijaGreen,
                bodyColor = if (dark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
            ) {
                val pidgin = if (section.pidgin.isBlank()) {
                    stringResource(R.string.pidgin_missing)
                } else {
                    section.pidgin.trim()
                }
                Text(
                    text = pidgin,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Original law text below it
            LawCard(
                title = stringResource(R.string.original_card_title),
                headerColor = if (dark) DarkOriginalHeader else NaijaGreen,
                bodyColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                OriginalLawText(
                    text = section.text,
                    resetKey = section.id,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.easy_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(
                    fontFamily = LawSerif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/** The star at the right of the header: outlined when not saved, filled when saved. */
@Composable
private fun FavouriteStar(saved: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (saved) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = stringResource(if (saved) R.string.fav_remove else R.string.fav_add),
            tint = NaijaGold,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** Gold badge, thin gold divider and the section title (wraps to several lines if long). */
@Composable
private fun TitleRow(section: LawSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberBadge(text = section.displayNumber)
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(1.5.dp)
                .fillMaxHeight(0.75f)
                .background(NaijaGold),
        )
        Text(
            text = section.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
            modifier = Modifier.weight(1f),
        )
    }
}

/** A rounded card with a coloured header strip and a body. */
@Composable
private fun LawCard(
    title: String,
    headerColor: Color,
    bodyColor: Color,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bodyColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                color = Color.White,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
            )
        }
        content()
    }
}

/**
 * The original law text in serif italic. "\n" starts a new paragraph (8 dp apart); a paragraph
 * that starts with "(" is indented. Collapsed to about 6 lines; "Show more" appears only when the
 * text really is longer than that (we measure the real line count, not the characters).
 */
@Composable
private fun OriginalLawText(text: String, resetKey: String) {
    val paragraphs = remember(text) {
        text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    }
    var expanded by rememberSaveable(resetKey) { mutableStateOf(false) }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onBackground

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Width left for the text after the 16 dp padding on each side.
        val availableWidth = with(density) { (maxWidth - 32.dp).roundToPx() }.coerceAtLeast(0)
        val indentPx = with(density) { ParagraphIndent.roundToPx() }

        // How many lines each paragraph needs at this width.
        val lineCounts: List<Int> = remember(paragraphs, availableWidth, density) {
            paragraphs.map { paragraph ->
                val width = if (paragraph.startsWith("(")) availableWidth - indentPx else availableWidth
                measurer.measure(
                    text = AnnotatedString(paragraph),
                    style = LawTextStyle,
                    constraints = Constraints(maxWidth = width.coerceAtLeast(0)),
                ).lineCount
            }
        }

        // For the collapsed view: how many lines of each paragraph may show (0 = hidden).
        val shownLines: IntArray = remember(lineCounts) {
            val result = IntArray(lineCounts.size)
            var remaining = COLLAPSED_LINES
            for (i in lineCounts.indices) {
                val needed = lineCounts[i]
                if (remaining <= 0) {
                    result[i] = 0
                } else if (needed <= remaining) {
                    result[i] = needed
                    remaining -= needed
                } else {
                    result[i] = remaining
                    remaining = 0
                }
            }
            result
        }
        val tooLong = lineCounts.indices.any { shownLines[it] < lineCounts[it] }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = if (tooLong) 0.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (i in paragraphs.indices) {
                val collapsedHere = tooLong && !expanded
                if (collapsedHere && shownLines[i] == 0) continue
                val indent = if (paragraphs[i].startsWith("(")) ParagraphIndent else 0.dp
                if (collapsedHere && shownLines[i] < lineCounts[i]) {
                    Text(
                        text = paragraphs[i],
                        color = textColor,
                        style = LawTextStyle,
                        maxLines = shownLines[i],
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent),
                    )
                } else {
                    Text(
                        text = paragraphs[i],
                        color = textColor,
                        style = LawTextStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent),
                    )
                }
            }

            if (tooLong) {
                ShowMoreRow(expanded = expanded, onClick = { expanded = !expanded })
            }
        }
    }
}

/** "Show more" with a small down arrow, or "Show less" with an up arrow. */
@Composable
private fun ShowMoreRow(expanded: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary   // green (light) / gold (dark)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (expanded) R.string.show_less else R.string.show_more),
            color = accent,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
        )
        Canvas(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(16.dp, 10.dp),
        ) {
            val stroke = 2.5.dp.toPx()
            val tipY = if (expanded) 0f else size.height
            val baseY = if (expanded) size.height else 0f
            drawLine(
                color = accent,
                start = Offset(0f, baseY),
                end = Offset(size.width / 2f, tipY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = accent,
                start = Offset(size.width, baseY),
                end = Offset(size.width / 2f, tipY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}
