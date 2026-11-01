package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            .background(Color(0xFFF8FAFC)) // Clean, executive Light Mode
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PRESIDENTIAL HERO GREETING CARD (LIGHT LUXURY BLUE & WHITE) ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Color(0xFFBAE6FD),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF0F9FF), Color.White)
                        )
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "🇮🇳 JCI INDIA 2027",
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Text(
                                    "PRESIDENT PORTAL",
                                    color = Color(0xFF0369A1),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Welcome, President",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                        Text(
                            presidentName,
                            color = Color(0xFF0F172A),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            chapterName,
                            color = Color(0xFF0097D8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE))
                            .border(1.5.dp, Color(0xFF0097D8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 22.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // Quick Action Bar inside Hero
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroActionLightButton(
                        emoji = "🚀",
                        label = "Projects",
                        bgColor = Color(0xFFE0F2FE),
                        textColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("projects") }
                    )
                    HeroActionLightButton(
                        emoji = "⚡",
                        label = "Flyer Studio",
                        bgColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("flyers") }
                    )
                    HeroActionLightButton(
                        emoji = "👥",
                        label = "Members",
                        bgColor = Color(0xFFDCFCE7),
                        textColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("members") }
                    )
                }
            }
        }

        // --- 2. EXECUTIVE KPI SNAPSHOT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExecutiveLightKpiCard(
                modifier = Modifier.weight(1f),
                title = "Projects",
                value = "4 Active",
                subtitle = "6 Open Tasks",
                emoji = "🚀",
                accentColor = Color(0xFF0284C7),
                onClick = { onNavigateTab("projects") }
            )
            ExecutiveLightKpiCard(
                modifier = Modifier.weight(1f),
                title = "Members",
                value = "48 Active",
                subtitle = "2 In Queue",
                emoji = "👥",
                accentColor = Color(0xFF10B981),
                onClick = { onNavigateTab("members") }
            )
            ExecutiveLightKpiCard(
                modifier = Modifier.weight(1f),
                title = "Next Event",
                value = "15 Jan",
                subtitle = "LGB Meet #1",
                emoji = "📅",
                accentColor = Color(0xFFD97706),
                onClick = { onNavigateTab("calendar") }
            )
        }

        // --- 3. HIGH IMPACT FAST ACCESS SHORTCUTS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTab("scan_card") }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📇", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Scan Card", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                        Text("OCR Biz Directory", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTab("store") }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛍️", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("JCI Store", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                        Text("Vendor Marketplace", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        // --- 4. CHAPTER WEBSITE & MEMBER PORTAL BANNER (LIGHT THEME) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(16.dp))
                .clickable { onNavigateTab("website_builder") }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF0FDF4), Color.White)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Chapter Website & Member Portal",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Deploy in 60s • Multiple Templates",
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
                            "ONLINE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "Official chapter domain with Auto Flyer Sync, WhatsApp automated RSVP reminders, and secure Member Login Portal.",
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PortalBadgeLight(text = "✨ Auto Flyer Sync")
                    PortalBadgeLight(text = "💬 WhatsApp Bot")
                    PortalBadgeLight(text = "🔐 Member Portal")
                }
            }
        }

        // --- 5. EXECUTIVE WORKSPACE SHORTCUTS (CORE REPORT SPECIFICATIONS) ---
        Text(
            "PRESIDENT OPERATING WORKSPACE",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val workspaceTools = listOf(
            WorkspaceItemLight(
                title = "Projects & Tasks",
                subtitle = "Workspaces, milestones, committees & deliverables",
                emoji = "🚀",
                color = Color(0xFF0284C7),
                tabId = "projects"
            ),
            WorkspaceItemLight(
                title = "Members & Directory",
                subtitle = "Self-service QR reg, ID cards, approvals & scanner",
                emoji = "👥",
                color = Color(0xFF10B981),
                tabId = "members"
            ),
            WorkspaceItemLight(
                title = "Vendor Store",
                subtitle = "Momentos, badges, printing, banners & quotes",
                emoji = "🛍️",
                color = Color(0xFFF59E0B),
                tabId = "store"
            ),
            WorkspaceItemLight(
                title = "1-Tap Flyer Studio",
                subtitle = "Templates with auto-mapped photo & LO crest",
                emoji = "🎨",
                color = Color(0xFFEC4899),
                tabId = "flyers"
            ),
            WorkspaceItemLight(
                title = "Chapter Website Suite",
                subtitle = "Domain, auto-posts, WhatsApp bot & portal",
                emoji = "🌐",
                color = Color(0xFF0D9488),
                tabId = "website_builder"
            ),
            WorkspaceItemLight(
                title = "2027 Calendar & Events",
                subtitle = "JCI national days, projects, & agendas",
                emoji = "📅",
                color = Color(0xFF6366F1),
                tabId = "calendar"
            ),
            WorkspaceItemLight(
                title = "LGB & PST Directory",
                subtitle = "Manage PST and Vice Presidents contacts",
                emoji = "🪪",
                color = Color(0xFF8B5CF6),
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

data class WorkspaceItemLight(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val tabId: String
)

@Composable
fun HeroActionLightButton(
    emoji: String,
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExecutiveLightKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(title, color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PortalBadgeLight(text: String) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Text(
            text,
            color = Color(0xFF334155),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
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
