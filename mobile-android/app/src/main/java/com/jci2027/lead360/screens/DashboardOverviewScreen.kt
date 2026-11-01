package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardOverviewScreen(
    chapterName: String = "JCI Secunderabad Deccan",
    presidentName: String = "JC HGF Rakesh Sharma",
    onNavigateTab: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PRESIDENTIAL HERO GREETING CARD ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF6366F1))),
                    RoundedCornerShape(20.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    ),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF059669).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Text(
                                    "OFFICIAL LEADER SUITE",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Welcome, President",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Text(
                            presidentName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            chapterName,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 24.sp)
                    }
                }

                Divider(color = Color(0xFF334155))

                // Action Bar inside Hero
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroActionEmojiButton(
                        emoji = "⚡",
                        label = "1-Tap Flyer",
                        accentColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("flyers") }
                    )
                    HeroActionEmojiButton(
                        emoji = "🌐",
                        label = "Chapter Site",
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("website_builder") }
                    )
                    HeroActionEmojiButton(
                        emoji = "📅",
                        label = "Events",
                        accentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab("calendar") }
                    )
                }
            }
        }

        // --- 2. EXECUTIVE KPI SNAPSHOT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExecutiveEmojiKpiCard(
                modifier = Modifier.weight(1f),
                title = "Projects",
                value = "4 Active",
                subtitle = "Goal: 24/yr",
                emoji = "🚀",
                accentColor = Color(0xFF38BDF8)
            )
            ExecutiveEmojiKpiCard(
                modifier = Modifier.weight(1f),
                title = "PST & LGB",
                value = "7 Officers",
                subtitle = "100% Assigned",
                emoji = "👥",
                accentColor = Color(0xFFA855F7)
            )
            ExecutiveEmojiKpiCard(
                modifier = Modifier.weight(1f),
                title = "Next Event",
                value = "15 Jan",
                subtitle = "LGB Meet #1",
                emoji = "📌",
                accentColor = Color(0xFFF59E0B)
            )
        }

        // --- 3. CHAPTER WEBSITE & MEMBER PORTAL HIGHLIGHT CARD ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onNavigateTab("website_builder") }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Chapter Website & Member Portal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Deploy in 60s • Unlimited Templates",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "FREE FOR PST",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Launch your chapter's official web domain with Auto Flyer Sync, Official WhatsApp reminders, and secure Member Login Portal.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PortalBadge(text = "✨ Auto Flyer Sync")
                    PortalBadge(text = "💬 WhatsApp API")
                    PortalBadge(text = "🔐 Member Login Portal")
                }
            }
        }

        // --- 4. EXECUTIVE WORKSPACE SHORTCUTS ---
        Text(
            "LEADERSHIP TOOLS",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val workspaceTools = listOf(
            WorkspaceItem(
                title = "1-Tap Flyer Studio",
                subtitle = "Templates with your photo & logo auto-mapped",
                emoji = "🎨",
                color = Color(0xFF38BDF8),
                tabId = "flyers"
            ),
            WorkspaceItem(
                title = "Chapter Website Suite",
                subtitle = "Domain, auto-posts, WhatsApp bot & portal",
                emoji = "🌐",
                color = Color(0xFF10B981),
                tabId = "website_builder"
            ),
            WorkspaceItem(
                title = "LGB & Officer Directory",
                subtitle = "Manage PST and Vice Presidents contacts",
                emoji = "🪪",
                color = Color(0xFFA855F7),
                tabId = "lgb"
            ),
            WorkspaceItem(
                title = "2027 Calendar & Events",
                subtitle = "National JCI days, projects, & agendas",
                emoji = "📅",
                color = Color(0xFFF59E0B),
                tabId = "calendar"
            ),
            WorkspaceItem(
                title = "Photo Frames & DP",
                subtitle = "Square & circular presidential frames",
                emoji = "🖼️",
                color = Color(0xFFEC4899),
                tabId = "frames"
            ),
            WorkspaceItem(
                title = "President Profile & Setup",
                subtitle = "Update Chapter name, pin, and badges",
                emoji = "⚙️",
                color = Color(0xFF64748B),
                tabId = "profile"
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            workspaceTools.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        WorkspaceCard(
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

data class WorkspaceItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val tabId: String
)

@Composable
fun HeroActionEmojiButton(
    emoji: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ExecutiveEmojiKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 14.sp)
            }
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(title, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PortalBadge(text: String) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Text(
            text,
            color = Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun WorkspaceCard(
    modifier: Modifier = Modifier,
    item: WorkspaceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 18.sp)
            }
            Text(
                item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                item.subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}
