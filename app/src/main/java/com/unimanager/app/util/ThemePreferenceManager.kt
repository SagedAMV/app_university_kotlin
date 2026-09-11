package com.unimanager.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesName
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Theme Preference Manager
 * يحفظ تفضيل الوضع الليلي/النهاري في DataStore
 */
class ThemePreferenceManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val THEME_MODE_KEY = booleanPreferencesName("dark_theme")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesName("dynamic_color")

    /**
     * الحصول على تفضيل الوضع الليلي
     */
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE_KEY] ?: false // default = system
        }

    /**
     * الحصول على تفضيل Dynamic Color
     */
    val useDynamicColor: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_KEY] ?: true // default = true
        }

    /**
     * حفظ تفضيل الوضع الليلي
     */
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = isDark
        }
    }

    /**
     * حفظ تفضيل Dynamic Color
     */
    suspend fun setDynamicColor(useDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = useDynamic
        }
    }
}
