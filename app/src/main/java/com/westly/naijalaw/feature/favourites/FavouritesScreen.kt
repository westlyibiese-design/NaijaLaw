package com.westly.naijalaw.feature.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.data.LawLoadState
import com.westly.naijalaw.core.data.LawSection
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.ui.AppHeader
import com.westly.naijalaw.core.ui.SectionCard

/**
 * The Favourites tab: the saved sections in the law's own order, each with a filled gold star.
 * Tapping a card opens the section page; tapping the star removes it from the list at once.
 *
 * @param onSectionClick called with the section id when a card is tapped.
 */
@Composable
fun FavouritesScreen(
    onSectionClick: (sectionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadState by AppData.state.collectAsState()
    val favouriteIds by AppData.favouriteIds.collectAsState()

    // File order of the saved sections. Saved ids that do not exist any more are ignored.
    val saved: List<LawSection> = remember(loadState, favouriteIds) {
        val index = (loadState as? LawLoadState.Ready)?.index
        if (index == null || favouriteIds.isEmpty()) {
            emptyList()
        } else {
            index.sections.filter { it.id in favouriteIds }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(title = stringResource(R.string.favourites_title), tall = true)

        if (saved.isEmpty()) {
            EmptyFavourites()
        } else {
            Text(
                text = stringResource(R.string.favourites_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(saved, key = { it.id }) { section ->
                    SectionCard(
                        section = section,
                        onClick = { onSectionClick(section.id) },
                        trailing = {
                            IconButton(onClick = { AppData.toggleFavourite(section.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.fav_remove),
                                    tint = NaijaGold,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/** Nothing saved yet: an outlined gold star in a soft circle and two lines of text. */
@Composable
private fun EmptyFavourites() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(NaijaGold.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                // Decorative: the text under it says what this screen means.
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = NaijaGold,
                    modifier = Modifier.size(60.dp),
                )
            }
            Text(
                text = stringResource(R.string.favourites_empty_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = stringResource(R.string.favourites_empty_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
