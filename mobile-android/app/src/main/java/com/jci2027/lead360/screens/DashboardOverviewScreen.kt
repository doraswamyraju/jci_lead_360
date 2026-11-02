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
            .background(Color(0xFFF8FAFC)) // Ultra-clean, modern executive background
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. EXECUTIVE PRESTIGE HERO CARD (Fintech/Neobank aesthetic)
        // ==========================================
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep midnight slate
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0B192C),
                                Color(0xFF1E3E62),
                                Color(0xFF0097D8).copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Subtle decorative background circles
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-40).dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "🇮🇳 JCI INDIA 2027",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF34D399), CircleShape)
                                    )
                                    Text(
                                        "PRESIDENT PORTAL",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Executive Monogram Avatar
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👑", fontSize = 18.sp)
                            }
                        }
                    }

                    // Welcome + President details
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Welcome Back,",
                            color = Color(0xFF94A3B8),
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
                                color = Color(0xFF7DD3FC),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("•", color = Color(0xFF64748B), fontSize = 12.sp)
                            Text(
                                "Zone XXVI",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

                    // Hero Quick Action Buttons (Glassmorphic)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroExecutiveButton(
                            emoji = "⚡",
                            label = "Flyer Studio",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("flyers") }
                        )
                        HeroExecutiveButton(
                            emoji = "🗂️",
                            label = "JCI Resources",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("resources") }
                        )
                        HeroExecutiveButton(
                            emoji = "📇",
                            label = "Scan Card",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab("scan_card") }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. QUICK HORIZONTAL ACTION CHIPS (Stripe / Apple Style)
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
                accent = Color(0xFF0097D8),
                onClick = { onNavigateTab("resources") }
            )
            ActionQuickPill(
                emoji = "👥",
                title = "Directory (${48})",
                accent = Color(0xFF10B981),
                onClick = { onNavigateTab("members") }
            )
            ActionQuickPill(
                emoji = "📅",
                title = "Events Calendar",
                accent = Color(0xFFF59E0B),
                onClick = { onNavigateTab("calendar") }
            )
            ActionQuickPill(
                emoji = "🌐",
                title = "LO Website",
                accent = Color(0xFF8B5CF6),
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
                badgeColor = Color(0xFF0284C7),
                emoji = "🚀",
                onClick = { onNavigateTab("projects") }
            )
            ModernExecutiveKpiCard(
                modifier = Modifier.weight(1f),
                title = "Members",
                value = "48 Active",
                badge = "2 Pending",
                badgeColor = Color(0xFF10B981),
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
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTab("resources") }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF0F9FF), Color(0xFFFFFFFF))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0097D8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗂️", fontSize = 22.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "JCI Resources & Identity Hub",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                "Official Logo, Letterhead, Visiting Card & Certs",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            "OPEN →",
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BrandFeatureTag(text = "🎨 7 Logo Styles", isLive = true)
                    BrandFeatureTag(text = "📄 Letterhead", isLive = false)
                    BrandFeatureTag(text = "🪪 Visiting Card", isLive = false)
                    BrandFeatureTag(text = "🏆 Certificates", isLive = false)
                }
            }
        }

        // ==========================================
        // 5. CHAPTER WEBSITE & MEMBER PORTAL BANNER
        // ==========================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTab("website_builder") }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF0FDF4), Color.White)
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                "Official LO Web & Mobile Portal",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Deploy in 60s • Auto Flyer Sync • RSVP Bot",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 6. PRESIDENT OPERATING WORKSPACE (Clean Apple-Style Grid)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PRESIDENTIAL WORKSPACE",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                "9 Core Tools",
                color = Color(0xFF0097D8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val workspaceTools = listOf(
            WorkspaceItemLight(
                title = "Projects & Tasks",
                subtitle = "Milestones, committees & deliverables",
                emoji = "🚀",
                color = Color(0xFF0284C7),
                tabId = "projects"
            ),
            WorkspaceItemLight(
                title = "Members & Network",
                subtitle = "QR registration, approvals & live directory",
                emoji = "👥",
                color = Color(0xFF10B981),
                tabId = "members"
            ),
            WorkspaceItemLight(
                title = "1-Tap Flyer Studio",
                subtitle = "Auto-mapped photo & LO crest templates",
                emoji = "🎨",
                color = Color(0xFFEC4899),
                tabId = "flyers"
            ),
            WorkspaceItemLight(
                title = "JCI Resources",
                subtitle = "Logo, Letterhead, Cards & Certs",
                emoji = "🗂️",
                color = Color(0xFF0097D8),
                tabId = "resources"
            ),
            WorkspaceItemLight(
                title = "Scan Card (OCR)",
                subtitle = "Visiting card scanner & Rolodex",
                emoji = "📇",
                color = Color(0xFF8B5CF6),
                tabId = "scan_card"
            ),
            WorkspaceItemLight(
                title = "Vendor Store",
                subtitle = "Momentos, badges, printing & quotes",
                emoji = "🛍️",
                color = Color(0xFFF59E0B),
                tabId = "store"
            ),
            WorkspaceItemLight(
                title = "Chapter Website",
                subtitle = "Domain, auto-posts & member portal",
                emoji = "🌐",
                color = Color(0xFF0D9488),
                tabId = "website_builder"
            ),
            WorkspaceItemLight(
                title = "2027 Calendar",
                subtitle = "JCI national days, projects & meets",
                emoji = "📅",
                color = Color(0xFF6366F1),
                tabId = "calendar"
            ),
            WorkspaceItemLight(
                title = "LGB & PST Team",
                subtitle = "Manage PST and Vice Presidents",
                emoji = "🪪",
                color = Color(0xFF06B6D4),
                tabId = "lgb"
            ),
            WorkspaceItemLight(
                title = "Photo Frames & DP",
                subtitle = "Square & circular presidential frames",
                emoji = "🖼️",
                color = Color(0xFFD946EF),
                tabId = "frames"
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            workspaceTools.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        WorkspaceLightCard(
                            modifier = Modifier.weight(1f),
                            item = item,
                            onClick = { onNavigateTab(item.tabId) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// REUSABLE MODERN COMPONENTS
// -------------------------------------------------------------

@Composable
fun HeroExecutiveButton(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ActionQuickPill(
    emoji: String,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 12.sp)
            }
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun ModernExecutiveKpiCard(
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
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(title, color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    badge,
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BrandFeatureTag(text: String, isLive: Boolean) {
    Surface(
        color = if (isLive) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLive) Color(0xFFBAE6FD) else Color(0xFFE2E8F0))
    ) {
        Text(
            text,
            color = if (isLive) Color(0xFF0284C7) else Color(0xFF64748B),
            fontSize = 9.sp,
            fontWeight = if (isLive) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

data class WorkspaceItemLight(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val tabId: String
)

@Composable
fun WorkspaceLightCard(
    modifier: Modifier = Modifier,
    item: WorkspaceItemLight,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 18.sp)
            }
            Text(
                item.title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                item.subtitle,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}
