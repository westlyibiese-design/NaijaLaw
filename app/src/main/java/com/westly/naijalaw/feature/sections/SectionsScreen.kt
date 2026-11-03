package com.westly.naijalaw.feature.sections

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.data.LawCatalog
import com.westly.naijalaw.core.data.LawIndex
import com.westly.naijalaw.core.data.LawLoadState
import com.westly.naijalaw.core.data.LawSection
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.theme.isDarkTheme
import com.westly.naijalaw.core.ui.AppHeader
import com.westly.naijalaw.core.ui.AppSearchBar
import com.westly.naijalaw.core.ui.SectionCard
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Topic chips: ONE place to change which chapters belong to which chip.
// A chip selects sections by chapterNumber (null = the Act's own sections).
// Sections in chapters that are in no list appear only under "All" and in search.
// ---------------------------------------------------------------------------

internal class SectionTopic(
    @StringRes val label: Int,
    val chapters: Set<String>,
    val includesActSections: Boolean = false,
) {
    fun matches(section: LawSection): Boolean {
        val chapter = section.chapterNumber
        return if (chapter == null) includesActSections else chapter in chapters
    }
}

internal val SectionTopics: List<SectionTopic> = listOf(
    SectionTopic(R.string.chip_killing, setOf("26", "27", "28")),
    SectionTopic(R.string.chip_assault, setOf("25", "29")),
    SectionTopic(R.string.chip_stealing, setOf("34", "35", "36", "37", "39")),
    SectionTopic(R.string.chip_fraud, setOf("38", "40", "43", "44", "45", "46", "47")),
    SectionTopic(R.string.chip_public_order, setOf("6", "6A", "7", "8", "9", "10", "11")),
    SectionTopic(R.string.chip_corruption, setOf("12", "13", "49")),
    SectionTopic(R.string.chip_morality, setOf("21", "21A", "30")),
    SectionTopic(R.string.chip_property, setOf("41", "42")),
    SectionTopic(
        label = R.string.chip_general,
        chapters = setOf("1", "2", "3", "4", "5", "53", "54", "55"),
        includesActSections = true,
    ),
)

// Chip colours (from 02_sections_list.png)
private val DarkChipFill = Color(0xFF1E8A4C)
private val DarkChipBorder = Color(0xFF1F7A44)
private val LightChipBorder = Color(0xFFC9D3CC)

/**
 * The list of sections of one law, with a search bar and topic chips.
 * Only the Criminal Code Act has data. For any other law, or when the data is missing, it shows "no results".
 *
 * @param onSectionClick called with the section id when a card is tapped.
 */
@Composable
fun SectionsScreen(
    lawId: String,
    onBack: () -> Unit,
    onSectionClick: (sectionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val law = LawCatalog.byId(lawId)
    val loadState by AppData.state.collectAsState()
    val index: LawIndex? = if (lawId == LawCatalog.CRIMINAL_CODE_ID) {
        (loadState as? LawLoadState.Ready)?.index
    } else {
        null
    }

    // These survive going to a section page and coming back.
    var query by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(0) }   // 0 = All, 1..9 = SectionTopics[i - 1]
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(title = law?.name, onBack = onBack)

        if (index == null) {
            NoResults()
            return@Column
        }

        // Search and the selected chip work together. The search text is pre-built in Phase 4,
        // so this is cheap for 546 sections and runs only when the query or the chip changes.
        val results = remember(index, query, selectedChip) {
            val found = index.search(query)
            if (selectedChip == 0) {
                found
            } else {
                val topic = SectionTopics[selectedChip - 1]
                found.filter { topic.matches(it) }
            }
        }

        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            AppSearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    scope.launch { listState.scrollToItem(0) }
                },
                hint = stringResource(R.string.sections_search_hint),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            )

            TopicChips(
                selected = selectedChip,
                onSelect = {
                    selectedChip = it
                    scope.launch { listState.scrollToItem(0) }
                },
            )

            if (results.isEmpty()) {
                NoResults()
            } else {
                val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp + navigationBottom,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(results, key = { it.id }) { section ->
                        SectionCard(section = section, onClick = { onSectionClick(section.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoResults() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** One horizontally scrolling row: All, then the 9 topics. */
@Composable
private fun TopicChips(selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            TopicChip(
                label = stringResource(R.string.chip_all),
                selected = selected == 0,
                onClick = { onSelect(0) },
            )
        }
        itemsIndexed(SectionTopics, key = { _, topic -> topic.label }) { position, topic ->
            TopicChip(
                label = stringResource(topic.label),
                selected = selected == position + 1,
                onClick = { onSelect(position + 1) },
            )
        }
    }
}

/** Fully rounded chip: green fill with white text when selected, outlined when not. */
@Composable
private fun TopicChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val dark = isDarkTheme()
    val fill = if (dark) DarkChipFill else NaijaGreen
    val borderColor = if (dark) DarkChipBorder else LightChipBorder

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(if (selected) fill else Color.Transparent)
            .border(1.5.dp, if (selected) fill else borderColor, CircleShape)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            softWrap = false,
        )
    }
}
