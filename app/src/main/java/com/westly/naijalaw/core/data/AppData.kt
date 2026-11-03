package com.westly.naijalaw.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The app's data holder.
 *
 * Phase 3: nothing to load yet, so [ready] is always true.
 * Phase 4 fills this in with the real loading of the law file and keeps the name [ready],
 * so the splash screen does not have to change.
 */
object AppData {
    private val readyState = MutableStateFlow(true)

    /** True when everything the app needs has loaded. The splash waits for this. */
    val ready: StateFlow<Boolean> = readyState.asStateFlow()
}
