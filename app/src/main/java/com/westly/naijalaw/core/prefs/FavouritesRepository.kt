package com.westly.naijalaw.core.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.favouritesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "favourites",
)

/** The saved section ids (for example "cca-316"), stored on the phone only. */
class FavouritesRepository(private val context: Context) {

    private val idsKey = stringSetPreferencesKey("favourite_ids")

    val ids: Flow<Set<String>> = context.favouritesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs[idsKey] ?: emptySet() }

    /** Adds the id if it is not saved, removes it if it is. */
    suspend fun toggle(id: String) {
        context.favouritesDataStore.edit { prefs ->
            val current = prefs[idsKey] ?: emptySet()
            prefs[idsKey] = if (id in current) current - id else current + id
        }
    }
}
