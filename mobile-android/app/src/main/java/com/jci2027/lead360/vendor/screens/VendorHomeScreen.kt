package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
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
import com.jci2027.lead360.vendor.model.VendorKpiSummary
import com.jci2027.lead360.vendor.model.VendorProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorHomeScreen(
    profile: VendorProfile = VendorProfile(),
    kpi: VendorKpiSummary = VendorKpiSummary(),
    onNavigateTab: (String) -> Unit = {},
    onViewRfqs: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Official Deep Royal JCI Header Theme
    val royalBlue = Color(0xFF0D47A1)
    val royalBlueDark = Color(0xFF0A367C)
    val accentCyan = Color(0xFF00A3E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(scrollState)
    ) {
        // --- 1. TOP APP BAR MATCHING REFERENCE HEADER ---
        Surface(
            color = royalBlue,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand: JCI LEAD360 Vendor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛡️", fontSize = 22.sp)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "JCI ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "LEAD360",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Text(
                                "Vendor Portal",
                                color = Color(0xFF90CAF9),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Right Actions: Notification Bell + Sign Out
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 2. VENDOR IDENTITY GREETING PILL ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏪", fontSize = 22.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Hello, ${profile.businessName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text("✨", fontSize = 14.sp)
                            }
                            Text(
                                "JCI Approved Vendor",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDCFCE7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            "Vendor",
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // --- 3. SUPPLY JCI MATERIALS HERO BANNER (Pixel Perfect to Reference Column 2) ---
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(royalBlueDark, royalBlue, Color(0xFF1E88E5))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Supply JCI Materials",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Text(
                                "for Chapters & Events",
                                color = Color(0xFFBBDEFB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("✓ Mementoes", color = Color.White, fontSize = 11.sp)
                                Text("✓ Banners", color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("✓ Certificates", color = Color.White, fontSize = 11.sp)
                                Text("✓ Merchandise", color = Color.White, fontSize = 11.sp)
                            }
                        }

                        // Gold Trophy Icon Emblem
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 34.sp)
                        }
                    }
                }
            }

            // --- 4. 4 KPI STATS TILES (Reference Match: 12 New RFQs, 8 Orders, 3 In Production, ₹2.48L This Month) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiSquareTile(
                    modifier = Modifier.weight(1f),
                    emoji = "📑",
                    count = "${kpi.newRfqsCount}",
                    label = "New RFQs",
                    accentBg = Color(0xFFEFF6FF),
                    onClick = { onNavigateTab("rfqs") }
                )
                KpiSquareTile(
                    modifier = Modifier.weight(1f),
                    emoji = "🛒",
                    count = "${kpi.ordersCount}",
                    label = "Orders",
                    accentBg = Color(0xFFFEF3C7),
                    onClick = { onNavigateTab("orders") }
                )
                KpiSquareTile(
                    modifier = Modifier.weight(1f),
                    emoji = "🚚",
                    count = "${kpi.inProductionCount}",
                    label = "In Production",
                    accentBg = Color(0xFFE0F2FE),
                    onClick = { onNavigateTab("orders") }
                )
                KpiSquareTile(
                    modifier = Modifier.weight(1f),
                    emoji = "₹",
                    count = kpi.monthlyEarnings.replace(",500", "L").replace("₹", "₹"),
                    label = "This Month",
                    accentBg = Color(0xFFFCE7F3),
                    onClick = { onNavigateTab("earnings") }
                )
            }

            // --- 5. VIEW NEW REQUESTS (RFQs) PRIMARY ACTION BUTTON ---
            Button(
                onClick = onViewRfqs,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = royalBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "View New Requests (RFQs)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("›", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // --- 6. QUICK TOOL TILES ---
            Text(
                "Vendor Management Tools",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📦",
                    title = "My Catalog",
                    subtitle = "Manage inventory",
                    onClick = { onNavigateTab("catalog") }
                )
                QuickToolCard(
                    modifier = Modifier.weight(1f),
                    emoji = "➕",
                    title = "Add Product",
                    subtitle = "New catalog item",
                    onClick = { onNavigateTab("create_catalog") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📝",
                    title = "Create Quote",
                    subtitle = "Send custom offer",
                    onClick = { onNavigateTab("quote") }
                )
                QuickToolCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🏢",
                    title = "My Business",
                    subtitle = "Profile & rate card",
                    onClick = { onNavigateTab("profile") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun KpiSquareTile(
    modifier: Modifier = Modifier,
    emoji: String,
    count: String,
    label: String,
    accentBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickToolCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
