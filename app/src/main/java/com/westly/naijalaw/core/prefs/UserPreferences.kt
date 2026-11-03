package com.westly.naijalaw.core.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Light, dark, or follow the phone. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * What the user chose in Settings.
 *
 * @param themeMode SYSTEM (default), LIGHT or DARK.
 * @param textSizeStep 0..4 (small to large). 2 (the middle step) is the default.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSizeStep: Int = DEFAULT_TEXT_SIZE_STEP,
) {
    /** The text scale of the app (the phone's own font size setting is not used). */
    val textScale: Float get() = textScaleFor(textSizeStep)

    companion object {
        const val TEXT_SIZE_STEPS = 5
        const val DEFAULT_TEXT_SIZE_STEP = 2

        // Multipliers for the 5 steps, small to large. The middle one is 1.0 (normal).
        private val TEXT_SCALES = floatArrayOf(0.85f, 0.92f, 1.0f, 1.15f, 1.3f)

        fun textScaleFor(step: Int): Float =
            TEXT_SCALES[step.coerceIn(0, TEXT_SIZE_STEPS - 1)]
    }
}

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

/** Saves and reads [UserPreferences] on the phone only. */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SIZE_STEP = intPreferencesKey("text_size_step")
    }

    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            val savedMode = prefs[Keys.THEME_MODE]
            UserPreferences(
                themeMode = ThemeMode.values().firstOrNull { it.name == savedMode } ?: ThemeMode.SYSTEM,
                textSizeStep = (prefs[Keys.TEXT_SIZE_STEP] ?: UserPreferences.DEFAULT_TEXT_SIZE_STEP)
                    .coerceIn(0, UserPreferences.TEXT_SIZE_STEPS - 1),
            )
        }

    /** Writes both choices. */
    suspend fun save(preferences: UserPreferences) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = preferences.themeMode.name
            prefs[Keys.TEXT_SIZE_STEP] = preferences.textSizeStep
                .coerceIn(0, UserPreferences.TEXT_SIZE_STEPS - 1)
        }
    }
}
