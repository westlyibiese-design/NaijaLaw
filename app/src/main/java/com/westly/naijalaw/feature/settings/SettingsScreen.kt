package com.westly.naijalaw.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.AppData
import com.westly.naijalaw.core.prefs.ThemeMode
import com.westly.naijalaw.core.prefs.UserPreferences
import com.westly.naijalaw.core.theme.NaijaGreen
import com.westly.naijalaw.core.ui.AppHeader
import kotlin.math.roundToInt

/**
 * Settings: the text size of the whole app (5 steps) and the theme (follow phone, light, dark).
 * Both apply at once and are remembered. No bottom menu on this screen.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences by AppData.preferences.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(title = stringResource(R.string.settings_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(title = stringResource(R.string.settings_text_size)) {
                TextSizeControls(step = preferences.textSizeStep)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(title = stringResource(R.string.settings_theme)) {
                ThemeOption(
                    label = stringResource(R.string.settings_theme_system),
                    selected = preferences.themeMode == ThemeMode.SYSTEM,
                    onSelect = { AppData.setThemeMode(ThemeMode.SYSTEM) },
                )
                ThemeOption(
                    label = stringResource(R.string.settings_theme_light),
                    selected = preferences.themeMode == ThemeMode.LIGHT,
                    onSelect = { AppData.setThemeMode(ThemeMode.LIGHT) },
                )
                ThemeOption(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = preferences.themeMode == ThemeMode.DARK,
                    onSelect = { AppData.setThemeMode(ThemeMode.DARK) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/** A rounded card with a green header strip, like the cards on the About and Section screens. */
@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
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
                .background(NaijaGreen)
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

/** Small "A", slider with 5 steps, large "A", and a live preview line under it. */
@Composable
private fun TextSizeControls(step: Int) {
    val accent = MaterialTheme.colorScheme.primary            // green (light) / gold (dark)
    val sliderDescription = stringResource(R.string.settings_text_size)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "A",
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            Slider(
                value = step.toFloat(),
                onValueChange = { AppData.setTextSizeStep(it.roundToInt()) },
                valueRange = 0f..(UserPreferences.TEXT_SIZE_STEPS - 1).toFloat(),
                steps = UserPreferences.TEXT_SIZE_STEPS - 2,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline,
                    activeTickColor = MaterialTheme.colorScheme.surfaceVariant,
                    inactiveTickColor = accent,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = sliderDescription },
            )
            Text(
                text = "A",
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // The whole app already uses the chosen size, so this line shows it live.
        Text(
            text = stringResource(R.string.settings_text_preview),
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** One radio row: the whole row is tappable. */
@Composable
private fun ThemeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = accent,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
