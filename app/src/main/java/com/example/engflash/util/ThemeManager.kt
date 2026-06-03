package com.example.engflash.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("engflash_theme_prefs", Context.MODE_PRIVATE)
    
    // true = Dark, false = Light, null = System Default
    private val _themeMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    val themeMode: StateFlow<Boolean?> = _themeMode.asStateFlow()

    fun setThemeMode(isDark: Boolean?) {
        _themeMode.value = isDark
        if (isDark == null) {
            prefs.edit().remove("is_dark_mode").apply()
        } else {
            prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        }
    }
}
