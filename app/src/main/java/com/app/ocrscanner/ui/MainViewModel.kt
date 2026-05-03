package com.app.ocrscanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Activity-scoped ViewModel that resolves the user's theme preference into a
 * simple dark-theme flag consumed by [MainActivity].
 *
 * null = follow the system setting
 * true = force dark
 * false = force light
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    prefs: UserPreferencesRepository,
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean?> = prefs.preferences
        .map { p ->
            when (p.theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
