package com.jci2027.lead360.navigation

sealed class Screen(val id: String, val title: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Home", "🏛️")
    object Projects : Screen("projects", "Projects", "🚀")
    object Members : Screen("members", "Members", "👥")
    object Flyers : Screen("flyers", "Flyers", "⚡")
    object Store : Screen("store", "Store", "🛍️")
    object WebsitePortal : Screen("website_portal", "LO Website", "🌐")
    object Calendar : Screen("calendar", "Calendar", "📅")
    object Lgb : Screen("lgb", "LGB & PST", "🪪")
    object Frames : Screen("frames", "Frames", "📸")
    object Profile : Screen("profile", "Profile", "👤")
}
