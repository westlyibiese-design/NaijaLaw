package com.westly.naijalaw.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.core.data.LawSection
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreenDeep

/**
 * One section as a card: gold number badge, thin divider, title, first sentence of the Pidgin
 * explanation, and a [trailing] part (a green or gold chevron by default; Phase 8 passes a star).
 */
@Composable
fun SectionCard(
    section: LawSection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = { SectionChevron() },
) {
    val shape = RoundedCornerShape(16.dp)
    val preview = remember(section.pidgin) { firstSentence(section.pidgin) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
            .heightIn(min = 80.dp)
            .padding(12.dp),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = section.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (preview.isNotEmpty()) {
                Text(
                    text = preview,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing()
    }
}

/** The gold badge. It grows with the text, so "A1A" or "233B" never clips. */
@Composable
fun NumberBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 60.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NaijaGold)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = NaijaGreenDeep,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Chevron on the right: green in the light theme, gold in the dark theme. */
@Composable
fun SectionChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp),
    )
}

/** Everything up to the first ". " (the full stop stays), or the whole text. Blank gives "". */
internal fun firstSentence(pidgin: String): String {
    val text = pidgin.trim().replace('\n', ' ')
    if (text.isEmpty()) return ""
    val end = text.indexOf(". ")
    return if (end >= 0) text.substring(0, end + 1) else text
}
