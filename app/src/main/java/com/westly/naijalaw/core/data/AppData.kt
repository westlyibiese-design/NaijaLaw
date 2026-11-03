package com.westly.naijalaw.core.data

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    /** Loading, Ready (with the Criminal Code Act index) or Failed. */
    val state: StateFlow<LawLoadState> = loadState.asStateFlow()

    /** True as soon as loading is over, whether it worked or not, so the splash never hangs. */
    val ready: StateFlow<Boolean> = loadState
        .map { it !is LawLoadState.Loading }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Safe to call more than once; only the first call starts loading. */
    fun start(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            loadState.value = try {
                LawLoadState.Ready(LawLoader.load(appContext, LawCatalog.CRIMINAL_CODE_ASSET))
            } catch (e: Exception) {
                LawLoadState.Failed
            }
        }
    }
}
