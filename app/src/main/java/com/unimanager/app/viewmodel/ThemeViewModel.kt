package com.unimanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.util.ThemePreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * مصدر وحيد لتفضيلات المظهر عبر التطبيق.
 * ThemePreferenceManager مُحقون عبر Hilt بنسخة واحدة، مما يمنع فتح
 * أكثر من DataStore لنفس الملف (IllegalStateException).
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferences: ThemePreferenceManager
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = preferences.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val useDynamicColor: StateFlow<Boolean> = preferences.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkTheme(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferences.setDynamicColor(enabled) }
    }
}
