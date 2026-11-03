package com.westly.naijalaw.feature.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.theme.NaijaGold
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.theme.NaijaGreenDark
import com.westly.naijalaw.core.theme.NaijaGreenDeep
import com.westly.naijalaw.core.theme.isDarkTheme
import com.westly.naijalaw.core.ui.DownloadBadgeIcon
import com.westly.naijalaw.core.ui.ScalesMark
import com.westly.naijalaw.core.ui.SectionChevron
import com.westly.naijalaw.core.ui.ShieldCheckBadgeIcon

private val LightGreyStrip = Color(0xFFDDE1DE)
private val DarkGreyStrip = Color(0xFF3A5445)

/**
 * The About tab. A flat green bar with a back arrow (which goes to Home), the app mark,
 * name, tagline and real version, three cards and two info rows. It scrolls when it does not fit.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkTheme()
    val context = LocalContext.current
    // The real versionName from the installed app (empty if it cannot be read: then the line is hidden).
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FlatTopBar(title = stringResource(R.string.about_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(NaijaGreen),
                contentAlignment = Alignment.Center,
            ) {
                ScalesMark(modifier = Modifier.width(68.dp))
            }

            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.tagline),
                color = if (dark) NaijaGold else NaijaGreenDeep,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 28.sp,
                ),
            )
            if (versionName.isNotBlank()) {
                Text(
                    text = stringResource(R.string.version_format, versionName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                title = stringResource(R.string.about_card1_title),
                body = stringResource(R.string.about_card1_body),
                stripColor = NaijaGreen,
                stripTextColor = Color.White,
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = stringResource(R.string.about_card2_title),
                body = stringResource(R.string.about_card2_body),
                stripColor = NaijaGold,
                stripTextColor = NaijaGreenDeep,
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = stringResource(R.string.about_card3_title),
                body = stringResource(R.string.about_card3_body),
                stripColor = if (dark) DarkGreyStrip else LightGreyStrip,
                stripTextColor = if (dark) Color.White else NaijaGreenDeep,
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(text = stringResource(R.string.about_row_offline)) { DownloadBadgeIcon() }
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(text = stringResource(R.string.about_row_noai)) { ShieldCheckBadgeIcon() }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsRow(onClick = onSettingsClick)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** A flat (not curved) green bar: back arrow at the left, centred title. */
@Composable
private fun FlatTopBar(title: String, onBack: () -> Unit) {
    val barColor = if (isDarkTheme()) NaijaGreenDark else NaijaGreen
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(barColor)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 4.dp),
        ) {
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
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 52.dp),
            )
        }
    }
}

/** A rounded card with a coloured header strip and body text. */
@Composable
private fun InfoCard(
    title: String,
    body: String,
    stripColor: Color,
    stripTextColor: Color,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(stripColor)
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                color = stripTextColor,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
            )
        }
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

/** A round green icon and one line of text. */
@Composable
private fun InfoRow(text: String, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        icon()
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
            modifier = Modifier.weight(1f),
        )
    }
}

/** A card-style row with a gear icon, the word Settings and a chevron. Opens the Settings screen. */
@Composable
private fun SettingsRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decorative: the word "Settings" next to it says what this is.
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = stringResource(R.string.settings_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        )
        SectionChevron()
    }
}
