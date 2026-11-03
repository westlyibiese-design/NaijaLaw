package com.westly.naijalaw.core.data

import android.content.Context
import com.westly.naijalaw.core.prefs.FavouritesRepository
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    /** Loading, Ready (with the Criminal Code Act index) or Failed. */
    val state: StateFlow<LawLoadState> = loadState.asStateFlow()

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
}
