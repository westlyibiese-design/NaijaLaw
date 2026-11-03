package com.westly.naijalaw.core.data

import android.content.Context
import com.westly.naijalaw.core.prefs.FavouritesRepository
import com.westly.naijalaw.core.prefs.PreferencesRepository
import com.westly.naijalaw.core.prefs.ThemeMode
import com.westly.naijalaw.core.prefs.UserPreferences
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The app's data, loaded once on a background thread while the splash plays.
 * It lives as long as the app process, so turning dark mode on or off never reloads anything.
 */
object AppData {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)

    private val loadState = MutableStateFlow<LawLoadState>(LawLoadState.Loading)

    // Favourites (saved section ids)
    private val favouritesState = MutableStateFlow<Set<String>>(emptySet())
    private val favouritesLoaded = MutableStateFlow(false)
    private var favouritesRepository: FavouritesRepository? = null

    // The user's Settings choices (theme and text size)
    private val preferencesState = MutableStateFlow(UserPreferences())
    private var preferencesRepository: PreferencesRepository? = null

    /** Loading, Ready (with the Criminal Code Act index) or Failed. */
    val state: StateFlow<LawLoadState> = loadState.asStateFlow()

    /** The user's Settings choices. Changing them updates the whole app at once. */
    val preferences: StateFlow<UserPreferences> = preferencesState.asStateFlow()

    /** The ids of the saved sections. Updates by itself whenever a star is tapped. */
    val favouriteIds: StateFlow<Set<String>> = favouritesState.asStateFlow()

    /**
     * True as soon as the law file AND the saved favourites have finished loading,
     * whether it worked or not, so the splash never hangs.
     */
    val ready: StateFlow<Boolean> = combine(loadState, favouritesLoaded) { law, favourites ->
        law !is LawLoadState.Loading && favourites
    }.stateIn(scope, SharingStarted.Eagerly, false)

    /** Safe to call more than once; only the first call starts loading. */
    fun start(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        val repository = FavouritesRepository(appContext)
        favouritesRepository = repository

        // One tiny blocking read, so the splash and the theme are right from the very first frame.
        val preferencesRepo = PreferencesRepository(appContext)
        preferencesRepository = preferencesRepo
        preferencesState.value = try {
            runBlocking { preferencesRepo.preferences.first() }
        } catch (e: Exception) {
            UserPreferences()
        }

        scope.launch {
            loadState.value = try {
                LawLoadState.Ready(LawLoader.load(appContext, LawCatalog.CRIMINAL_CODE_ASSET))
            } catch (e: Exception) {
                LawLoadState.Failed
            }
        }

        scope.launch {
            try {
                // Keeps listening, so the star always shows the saved state.
                repository.ids.collect { ids ->
                    favouritesState.value = ids
                    favouritesLoaded.value = true
                }
            } catch (e: Exception) {
                // If the saved list cannot be read, carry on with an empty list.
                favouritesLoaded.value = true
            }
        }
    }

    /** Saves the section if it is not saved, removes it if it is. */
    fun toggleFavourite(id: String) {
        val repository = favouritesRepository ?: return
        scope.launch {
            try {
                repository.toggle(id)
            } catch (e: Exception) {
                // Nothing to do: the star simply stays as it was.
            }
        }
    }

    /** Light, dark or follow the phone. Applies at once and is remembered. */
    fun setThemeMode(mode: ThemeMode) {
        preferencesState.update { it.copy(themeMode = mode) }
        savePreferences()
    }

    /** Text size step 0..4. Applies at once and is remembered. */
    fun setTextSizeStep(step: Int) {
        preferencesState.update { it.copy(textSizeStep = step.coerceIn(0, UserPreferences.TEXT_SIZE_STEPS - 1)) }
        savePreferences()
    }

    // Saves the newest values when the write runs, so quick slider moves always end with the last one.
    private fun savePreferences() {
        val repository = preferencesRepository ?: return
        scope.launch {
            try {
                repository.save(preferencesState.value)
            } catch (e: Exception) {
                // The choice still works until the app is closed.
            }
        }
    }
}
