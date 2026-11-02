package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.VendorProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorBusinessProfileScreen(
    profile: VendorProfile = VendorProfile(),
    onBack: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    val navyBlue = Color(0xFF0D47A1)
    val backgroundLight = Color(0xFFF7F9FC)
    val cardBg = Color.White
    val textDark = Color(0xFF1E293B)
    val textMuted = Color(0xFF64748B)
    val verifiedGreen = Color(0xFF10B981)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Business",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navyBlue
                )
            )
        },
        containerColor = backgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏪", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.businessName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = textDark
                            )
                            if (profile.isApproved) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = verifiedGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "JCI Vendor",
                            fontSize = 13.sp,
                            color = textMuted
                        )
                    }

                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = navyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Menu Options matching Reference
            val menuItems = listOf(
                BusinessMenuItem(
                    id = "business_details",
                    emoji = "🏢",
                    title = "Business Details",
                    subtitle = "Company name, address, contact persons"
                ),
                BusinessMenuItem(
                    id = "product_catalog",
                    emoji = "📦",
                    title = "Product Catalog",
                    subtitle = "Manage items, prices and availability"
                ),
                BusinessMenuItem(
                    id = "price_lists",
                    emoji = "📄",
                    title = "Price Lists",
                    subtitle = "Wholesale & chapter rate cards"
                ),
                BusinessMenuItem(
                    id = "service_areas",
                    emoji = "📍",
                    title = "Service Areas (Delivery)",
                    subtitle = "Covered zones, pins & transit turnaround"
                ),
                BusinessMenuItem(
                    id = "gallery",
                    emoji = "🖼️",
                    title = "Gallery (Past Works)",
                    subtitle = "Portfolio photos & event installations"
                ),
                BusinessMenuItem(
                    id = "documents",
                    emoji = "📑",
                    title = "Documents (GST, PAN, etc.)",
                    subtitle = "Tax verification & banking accounts"
                )
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.emoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = textDark
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }

                            Text("›", fontSize = 22.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                        }

                        if (index < menuItems.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class BusinessMenuItem(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String
)
