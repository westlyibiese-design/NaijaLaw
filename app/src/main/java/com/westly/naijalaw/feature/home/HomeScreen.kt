package com.westly.naijalaw.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.LawCatalog
import com.westly.naijalaw.core.data.LawInfo
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.theme.isDarkTheme
import com.westly.naijalaw.core.ui.AppHeader
import com.westly.naijalaw.core.ui.LawIcon
import com.westly.naijalaw.core.ui.ScalesMark
import kotlinx.coroutines.launch

// Colours that only the Home screen needs (taken from 01_home.png)
private val DarkTileGreen = Color(0xFF1E8A4C)
private val DarkSearchOutline = Color(0xFF1F7A44)

/**
 * The Home screen: logo header, search bar and the list of laws.
 *
 * @param onLawClick called with the law id when an available law is tapped.
 */
@Composable
fun HomeScreen(
    onLawClick: (lawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkTheme()
    var query by rememberSaveable { mutableStateOf("") }
    val laws = remember(query) {
        val text = query.trim()
        if (text.isEmpty()) {
            LawCatalog.laws
        } else {
            LawCatalog.laws.filter { it.name.contains(text, ignoreCase = true) }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(R.string.law_coming_soon_message)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(tall = true, tallContent = { HomeLogoRow(dark = dark) })

            SearchBar(
                query = query,
                onQueryChange = { query = it },
                dark = dark,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )

            if (laws.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_laws_match),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(laws, key = { it.id }) { law ->
                        LawCard(
                            law = law,
                            dark = dark,
                            onClick = {
                                if (law.available) {
                                    onLawClick(law.id)
                                } else {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                                }
                            },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

/** Gold scales, a thin gold divider, then "NaijaLaw" and the tagline. */
@Composable
private fun HomeLogoRow(dark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScalesMark(modifier = Modifier.width(68.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(NaijaGold),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
            )
            Text(
                text = stringResource(R.string.tagline),
                color = if (dark) NaijaGold else Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 24.sp,
                ),
            )
        }
    }
}

/** Fully rounded search field: grey fill in light theme, green outline in dark theme. */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val hint = stringResource(R.string.home_search_hint)
    val borderModifier = if (dark) {
        Modifier.border(BorderStroke(1.5.dp, DarkSearchOutline), CircleShape)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(if (dark) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
            .then(borderModifier)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(fontSize = 18.sp, fontStyle = FontStyle.Italic),
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** One law: green icon tile, name, subtitle and (when available) a chevron. */
@Composable
private fun LawCard(
    law: LawInfo,
    dark: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val tileColor = if (dark) DarkTileGreen else NaijaGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (law.available) 1f else 0.55f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .heightIn(min = 88.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center,
        ) {
            LawIcon(iconKey = law.iconKey, size = 38.dp, cutout = tileColor)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = law.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
            )
            Text(
                text = stringResource(if (law.available) R.string.law_tap_to_read else R.string.law_coming_soon),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
            )
        }
        if (law.available) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,   // green in light theme, gold in dark theme
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
