package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorEarningsScreen(
    thisMonthEarnings: String = "₹2,48,500",
    completedEarnings: String = "₹1,85,000",
    inProgressEarnings: String = "₹52,500",
    pendingEarnings: String = "₹11,000",
    onBack: () -> Unit = {},
    onPayoutsClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    onDownloadReportsClick: () -> Unit = {}
) {
    val navyBlue = Color(0xFF0D47A1)
    val backgroundLight = Color(0xFFF7F9FC)
    val cardBg = Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Earnings & Payouts",
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
            // Main Highlight Earnings Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "This Month Earnings",
                            color = Color(0xFFBBDEFB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = thisMonthEarnings,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mini Graph Visual Representation
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Box(modifier = Modifier.width(6.dp).height(16.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.5f)))
                        Box(modifier = Modifier.width(6.dp).height(24.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.7f)))
                        Box(modifier = Modifier.width(6.dp).height(32.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.85f)))
                        Box(modifier = Modifier.width(6.dp).height(40.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                    }
                }
            }

            // 3-Pillar Breakdown Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EarningsPillarCard(
                    modifier = Modifier.weight(1f),
                    amount = completedEarnings,
                    label = "Completed",
                    accentColor = Color(0xFF10B981),
                    badgeBg = Color(0xFFD1FAE5)
                )

                EarningsPillarCard(
                    modifier = Modifier.weight(1f),
                    amount = inProgressEarnings,
                    label = "In Progress",
                    accentColor = Color(0xFFF59E0B),
                    badgeBg = Color(0xFFFEF3C7)
                )

                EarningsPillarCard(
                    modifier = Modifier.weight(1f),
                    amount = pendingEarnings,
                    label = "Pending",
                    accentColor = Color(0xFF8B5CF6),
                    badgeBg = Color(0xFFEDE9FE)
                )
            }

            // Options List matching Reference
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
                    EarningsActionItem(
                        emoji = "💳",
                        title = "Payouts & Settlements",
                        subtitle = "Bank transfers, cycle schedule & ledger",
                        onClick = onPayoutsClick
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    EarningsActionItem(
                        emoji = "🧾",
                        title = "Transaction History",
                        subtitle = "Comprehensive invoice breakdown & receipts",
                        onClick = onTransactionsClick
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    EarningsActionItem(
                        emoji = "📥",
                        title = "Download Reports",
                        subtitle = "Export monthly GST statements & CSV data",
                        onClick = onDownloadReportsClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EarningsPillarCard(
    modifier: Modifier = Modifier,
    amount: String,
    label: String,
    accentColor: Color,
    badgeBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun EarningsActionItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Text("›", fontSize = 22.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
    }
}
