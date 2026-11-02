package com.jci2027.lead360.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Official JCI Brand Identity Colors
 * Derived directly from the official JCI Shield Logo:
 * - JCI Deep Navy (#0C1427 / #101633)
 * - JCI Royal Blue (#005BAA / #0072CE)
 * - JCI Sky Blue (#0097D8 / #00A3E0)
 * - JCI Teal / Mint Accent (#36BBA6 / #49C5B6)
 */
object JciColors {
    // Primary Brand Shades
    val DeepNavy = Color(0xFF0F182E)       // "JCI" letters & shield core
    val DeepNavyDark = Color(0xFF0A0F1D)   // TopAppBar / Drawer Darkest
    val RoyalBlue = Color(0xFF005BAA)      // Official Primary Blue
    val SkyBlue = Color(0xFF0097D8)        // Shield outer left ribbon
    val ShieldTeal = Color(0xFF36BBA6)     // Shield outer right ribbon
    val ShieldTealLight = Color(0xFFE6F8F5)

    // Accents & Gradients
    val GradientStart = Color(0xFF0F182E)
    val GradientMid = Color(0xFF005BAA)
    val GradientEnd = Color(0xFF0097D8)
    val GradientTeal = Color(0xFF36BBA6)

    val OfficialJciGradient = Brush.linearGradient(
        listOf(DeepNavy, RoyalBlue, SkyBlue)
    )

    val OfficialShieldGradient = Brush.horizontalGradient(
        listOf(SkyBlue, ShieldTeal)
    )

    val HeroExecutiveGradient = Brush.linearGradient(
        listOf(DeepNavy, Color(0xFF132247), RoyalBlue)
    )

    // Backgrounds & Surfaces
    val BackgroundLight = Color(0xFFF6F8FC)
    val CardSurface = Color.White
    val BorderSubtle = Color(0xFFE2E8F0)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val TextLight = Color(0xFFF8FAFC)
}

