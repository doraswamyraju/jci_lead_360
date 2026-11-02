package com.jci2027.lead360.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.theme.JciColors

@Composable
fun DashboardOverviewScreen(
    chapterName: String = "JCI Tirupati Innovations",
    presidentName: String = "JC Doraswamy Raju",
    onNavigateTab: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JciColors.BackgroundLight)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. EXECUTIVE PRESTIGE HERO CARD (Official JCI Shield & Navy/Sky/Teal Gradient)
        // ==========================================
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JciColors.DeepNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                JciColors.DeepNavy,
                                Color(0xFF10284C),
                                JciColors.RoyalBlue,
                                JciColors.SkyBlue
                            )
                        )
                    )
            ) {
                // Official JCI Shield Dual-Accent Accent Arc (Sky Blue & Teal ribbons)
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-40).dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    JciColors.SkyBlue.copy(alpha = 0.25f),
                                    JciColors.ShieldTeal.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Bar inside Hero: Organization identity + Live Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    "🇮🇳 JCI INDIA 2027",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JciColors.ShieldTeal.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JciColors.ShieldTeal.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(JciColors.ShieldTeal, CircleShape)
                                    )
                                    Text(
                                        "PRESIDENT PORTAL",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // JCI Emblem Monogram
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, JciColors.ShieldTeal),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🛡️", fontSize = 20.sp)
                            }
                        }
                    }

                    // Welcome + President details
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Welcome Back,",
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            presidentName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "🏛️ $chapterName",
                                color = Color(0xFFE0F2FE),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("•", color = Color(0xFF93C5FD), fontSize = 12.sp)
                            Text(
                                "Zone XXVI",
                                color = Color(0xFFBAE6FD),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.16f), thickness = 1.dp)

                    // Hero Quick Action Buttons with official Brand accents
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroExecutiveButton(
                            emoji = "⚡",
                            label = "Flyer Studio",
                            accent = JciColors.SkyBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("flyers") }
                        )
                        HeroExecutiveButton(
                            emoji = "🗂️",
                            label = "JCI Resources",
                            accent = JciColors.ShieldTeal,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("resources") }
                        )
                        HeroExecutiveButton(
                            emoji = "📇",
                            label = "Scan Card",
                            accent = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("scan_card") }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. QUICK HORIZONTAL ACTION CHIPS (JCI Shield Brand Colors)
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionQuickPill(
                emoji = "🎨",
                title = "Logo Maker",
                accent = JciColors.SkyBlue,
                onClick = { onNavigateTab("resources") }
            )
            ActionQuickPill(
                emoji = "👥",
                title = "Directory (48)",
                accent = JciColors.ShieldTeal,
                onClick = { onNavigateTab("members") }
            )
            ActionQuickPill(
                emoji = "📅",
                title = "Events Calendar",
                accent = Color(0xFFF59E0B),
                onClick = { onNavigateTab("calendar") }
            )
            ActionQuickPill(
                emoji = "🛍️",
                title = "Vendor Store",
                accent = JciColors.RoyalBlue,
                onClick = { onNavigateTab("store") }
            )
            ActionQuickPill(
                emoji = "🌐",
                title = "LO Website",
                accent = Color(0xFF6366F1),
                onClick = { onNavigateTab("website_builder") }
            )
        }

        // ==========================================
        // 3. EXECUTIVE KPI METRICS (Modern Card Grid with Delta Badges)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModernExecutiveKpiCard(
                modifier = Modifier.weight(1f),
                title = "Projects",
                value = "4 Active",
                badge = "+2 This Mo",
                badgeColor = JciColors.SkyBlue,
                emoji = "🚀",
                onClick = { onNavigateTab("projects") }
            )
            ModernExecutiveKpiCard(
                modifier = Modifier.weight(1f),
                title = "Members",
                value = "48 Active",
                badge = "2 Pending",
                badgeColor = JciColors.ShieldTeal,
                emoji = "👥",
                onClick = { onNavigateTab("members") }
            )
            ModernExecutiveKpiCard(
                modifier = Modifier.weight(1f),
                title = "Next Meet",
                value = "15 Jan",
                badge = "GBM #1",
                badgeColor = Color(0xFFD97706),
                emoji = "📅",
                onClick = { onNavigateTab("calendar") }
            )
        }

        // ==========================================
        // 4. JCI RESOURCES & BRAND SUITE HIGHLIGHT BANNER
        // ==========================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JciColors.SkyBlue.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTab("resources") }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF0F9FF), Color(0xFFF5FBFA), Color(0xFFFFFFFF))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(JciColors.OfficialShieldGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗂️", fontSize = 22.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "JCI Official Resources",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = JciColors.DeepNavy
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = JciColors.SkyBlue.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "BRAND 2027",
                                        color = JciColors.RoyalBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Logo Maker • Letterheads • Cards • Badges",
                                fontSize = 12.sp,
                                color = JciColors.TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text("›", fontSize = 26.sp, color = JciColors.SkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ==========================================
        // 5. 100% EFFICIENCY ACTION TRACKER
        // ==========================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎯", fontSize = 16.sp)
                            Text(
                                "100% Efficiency Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = JciColors.DeepNavy
                            )
                        }
                        Text(
                            "Q1 National & Zone Milestones",
                            fontSize = 12.sp,
                            color = JciColors.TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = JciColors.ShieldTealLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, JciColors.ShieldTeal.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "78% Completed",
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = JciColors.ShieldTeal,
                    trackColor = Color(0xFFE2E8F0)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("✓ 7 Mandatory Goals Met", fontSize = 12.sp, color = JciColors.ShieldTeal, fontWeight = FontWeight.SemiBold)
                    Text("2 Due Next Week", fontSize = 12.sp, color = JciColors.TextMuted, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ==========================================
        // 6. EXECUTIVE ACTION TILES
        // ==========================================
        Text(
            "Leadership Modules",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = JciColors.DeepNavy
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExecutiveActionTile(
                modifier = Modifier.weight(1f),
                emoji = "🚀",
                title = "Projects Hub",
                subtitle = "Manage chapter tasks",
                accentColor = JciColors.RoyalBlue,
                onClick = { onNavigateTab("projects") }
            )
            ExecutiveActionTile(
                modifier = Modifier.weight(1f),
                emoji = "🪪",
                title = "LGB & PST",
                subtitle = "Officer portfolio",
                accentColor = JciColors.ShieldTeal,
                onClick = { onNavigateTab("lgb") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExecutiveActionTile(
                modifier = Modifier.weight(1f),
                emoji = "🛍️",
                title = "Vendor Store",
                subtitle = "Order official mementos",
                accentColor = JciColors.SkyBlue,
                onClick = { onNavigateTab("store") }
            )
            ExecutiveActionTile(
                modifier = Modifier.weight(1f),
                emoji = "📸",
                title = "Photo Frames",
                subtitle = "Greeting overlays",
                accentColor = Color(0xFF8B5CF6),
                onClick = { onNavigateTab("frames") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HeroExecutiveButton(
    emoji: String,
    label: String,
    accent: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActionQuickPill(
    emoji: String,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 16.sp)
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = JciColors.DeepNavy
            )
        }
    }
}

@Composable
private fun ModernExecutiveKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 18.sp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = JciColors.DeepNavy
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = JciColors.TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ExecutiveActionTile(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = JciColors.DeepNavy
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = JciColors.TextMuted
                )
            }
        }
    }
}
