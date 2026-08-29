package com.example.wishlist.ui.theme

/**
 * User-selectable appearance modes for the app.
 */
enum class ThemeMode(val label: String, val storageKey: String) {
    SYSTEM("System", "system"),
    LIGHT("Light", "light"),
    DARK("Dark", "dark")
}
