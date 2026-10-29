package com.jci2027.lead360.navigation

sealed class Screen(val id: String, val title: String, val icon: String) {
    object Flyers : Screen("flyers", "Flyers", "⚡")
    object Frames : Screen("frames", "Frames", "📸")
    object Lgb : Screen("lgb", "LGB Directory", "👥")
    object Calendar : Screen("calendar", "Calendar", "📅")
    object Profile : Screen("profile", "Profile", "👤")
}
