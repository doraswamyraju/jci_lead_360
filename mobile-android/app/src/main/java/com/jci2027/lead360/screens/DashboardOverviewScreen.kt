package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardOverviewScreen(
    userName: String,
    chapterName: String,
    onNavigateTab: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Hero Greeting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x330097D8)
                ) {
                    Text("JCI TENURE 2027", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Welcome, $userName", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("Chapter President • $chapterName", color = Color(0xFF94A3B8), fontSize = 13.sp)

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigateTab("flyers") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚡ 1-Tap Flyer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onNavigateTab("calendar") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📅 Calendar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Milestone Quick Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickKpiCard("100% Efficiency", "Active", "⭐", Color(0xFFF59E0B), Modifier.weight(1f))
            QuickKpiCard("LGB Leaders", "7 Members", "👥", Color(0xFF0097D8), Modifier.weight(1f))
            QuickKpiCard("Upcoming", "15 Jan", "📅", Color(0xFF10B981), Modifier.weight(1f))
        }

        // Quick Module Launcher Grid
        Text(
            text = "EXECUTIVE WORKSPACE MODULES",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF475569)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleLauncherCard("1-Tap Flyers", "Create & share 300 DPI graphics", "⚡", Color(0xFF0097D8), Modifier.weight(1f)) {
                onNavigateTab("flyers")
            }
            ModuleLauncherCard("Photo Frames", "Birthday & greeting overlays", "📸", Color(0xFF8B5CF6), Modifier.weight(1f)) {
                onNavigateTab("frames")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleLauncherCard("LGB Directory", "Manage PST & chapter board", "👥", Color(0xFF10B981), Modifier.weight(1f)) {
                onNavigateTab("lgb")
            }
            ModuleLauncherCard("2027 Calendar", "Milestones & day compliance", "📅", Color(0xFFF37023), Modifier.weight(1f)) {
                onNavigateTab("calendar")
            }
        }

    }
}

@Composable
fun QuickKpiCard(title: String, value: String, icon: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Text(title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ModuleLauncherCard(
    title: String,
    description: String,
    icon: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(description, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2)
        }
    }
}

@Composable
fun NotificationCenterModal(onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔔 Notification Center", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                IconButton(onClick = onClose) {
                    Text("✕", fontSize = 16.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val notifications = listOf(
                Pair("⭐ 100% Efficiency Notice", "1st LOM Meeting flyer template is ready for WhatsApp sharing."),
                Pair("📅 Compliance Reminder", "Adopt chapter plan of action before 15th Jan 2027."),
                Pair("🎉 Welcome to LEAD360", "Hostinger isolated production port 5087 active.")
            )

            notifications.forEach { (title, msg) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
                        Text(msg, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

