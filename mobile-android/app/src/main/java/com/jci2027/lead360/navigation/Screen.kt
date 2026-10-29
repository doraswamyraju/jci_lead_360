package com.jci2027.lead360.navigation

sealed class Screen(val id: String, val title: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Home", "🏛️")
    object Flyers : Screen("flyers", "Flyers", "⚡")
    object AiFlyer : Screen("ai_flyer", "AI Studio", "✨")
    object Calendar : Screen("calendar", "Calendar", "📅")
    object Lgb : Screen("lgb", "LGB & PST", "👥")
    object Frames : Screen("frames", "Frames", "📸")
    object Profile : Screen("profile", "Profile", "👤")
}
