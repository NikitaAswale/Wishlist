package com.example.wishlist.ui.theme

import android.content.Context

/**
 * Persists the user's selected [ThemeMode] using SharedPreferences so the
 * choice survives app restarts. No third-party dependencies required.
 */
object ThemePreferenceStore {
    private const val PREFS_NAME = "wishlist_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.storageKey)
        return ThemeMode.entries.firstOrNull { it.storageKey == stored } ?: ThemeMode.SYSTEM
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.storageKey)
            .apply()
    }
}
