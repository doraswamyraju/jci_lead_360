package com.jci2027.lead360.vendor.screens

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
import com.jci2027.lead360.vendor.model.VendorKpiSummary
import com.jci2027.lead360.vendor.model.VendorProfile

@Composable
fun VendorHomeScreen(
    profile: VendorProfile = VendorProfile(),
    kpi: VendorKpiSummary = VendorKpiSummary(),
    onNavigateTab: (String) -> Unit = {},
    onViewRfqs: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TOP HEADER WITH JCI VENDOR IDENTITY ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0097D8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏪", fontSize = 22.sp)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Hello, ${profile.businessName} 👋",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        "JCI Approved Official Vendor",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
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
                    color = Color(0xFF15803D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // --- 2. HERO BANNER: Supply JCI Materials ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B192C)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                Color(0xFF0097D8)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Supply JCI Materials",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "for Chapters & Events",
                                color = Color(0xFFBAE6FD),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 28.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✓ Mementoes", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("✓ Banners", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("✓ Certificates", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✓ Merchandise", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("✓ Printing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("✓ More", color = Color(0xFFBAE6FD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. 4 KPI TILES (New RFQs, Orders, In Production, This Month) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VendorKpiTile(
                modifier = Modifier.weight(1f),
                icon = "📩",
                count = "${kpi.newRfqsCount}",
                label = "New RFQs",
                bgColor = Color(0xFFE0F2FE),
                onClick = { onNavigateTab("rfqs") }
            )
            VendorKpiTile(
                modifier = Modifier.weight(1f),
                icon = "🛒",
                count = "${kpi.ordersCount}",
                label = "Orders",
                bgColor = Color(0xFFFEF3C7),
                onClick = { onNavigateTab("orders") }
            )
            VendorKpiTile(
                modifier = Modifier.weight(1f),
                icon = "🚚",
                count = "${kpi.inProductionCount}",
                label = "In Production",
                bgColor = Color(0xFFE0E7FF),
                onClick = { onNavigateTab("orders") }
            )
            VendorKpiTile(
                modifier = Modifier.weight(1f),
                icon = "₹",
                count = "2.48L",
                label = "This Month",
                bgColor = Color(0xFFDCFCE7),
                onClick = { onNavigateTab("earnings") }
            )
        }

        // --- 4. CALL TO ACTION: View New Requests (RFQs) ---
        Button(
            onClick = onViewRfqs,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066CC)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("View New Requests (RFQs)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- 5. FAST SHORTCUTS TO ESSENTIAL VENDOR MODULES ---
        Text(
            "QUICK ACTIONS & TOOLS",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VendorShortcutCard(
                modifier = Modifier.weight(1f),
                title = "My Catalog",
                subtitle = "Manage JCI Mementoes, Banners & Merchandise",
                emoji = "📦",
                onClick = { onNavigateTab("catalog") }
            )
            VendorShortcutCard(
                modifier = Modifier.weight(1f),
                title = "Create Quote",
                subtitle = "Draft custom requirement or bulk order proposal",
                emoji = "📝",
                onClick = { onNavigateTab("create_quote") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VendorShortcutCard(
                modifier = Modifier.weight(1f),
                title = "Order Tracking",
                subtitle = "Active production milestones & deliveries",
                emoji = "⏱️",
                onClick = { onNavigateTab("orders") }
            )
            VendorShortcutCard(
                modifier = Modifier.weight(1f),
                title = "Payouts & GST",
                subtitle = "Settlements, invoices & transaction history",
                emoji = "💳",
                onClick = { onNavigateTab("earnings") }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun VendorKpiTile(
    modifier: Modifier = Modifier,
    icon: String,
    count: String,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 15.sp)
            }
            Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
            Text(label, fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun VendorShortcutCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp, maxLines = 2)
        }
    }
}

