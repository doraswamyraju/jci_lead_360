package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.VendorOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrdersScreen(
    onBack: () -> Unit = {},
    onOrderClick: (VendorOrder) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") } // All, In Production, Completed

    val ordersList = remember {
        listOf(
            VendorOrder(
                orderId = "#ORD10248",
                title = "Installation Kit (Backdrop, Banners)",
                chapterName = "JCI Tirupati Innovations",
                orderDate = "10 Jan 2027",
                amount = 48500.0,
                status = "In Production",
                itemsSummary = "20 ft stage backdrop, 2 podium banners, 100 induction dossiers",
                confirmedDate = "10 Jan 2027",
                inProductionDate = "11 Jan 2027",
                readyDate = "14 Jan 2027",
                deliveredDate = "Pending"
            ),
            VendorOrder(
                orderId = "#ORD10237",
                title = "Mementoes – 15 Nos",
                chapterName = "JCI Chittoor",
                orderDate = "08 Jan 2027",
                amount = 18750.0,
                status = "Confirmed",
                itemsSummary = "15 Golden crystal shields with custom engraving",
                confirmedDate = "08 Jan 2027",
                inProductionDate = "Pending",
                readyDate = "-",
                deliveredDate = "-"
            ),
            VendorOrder(
                orderId = "#ORD10210",
                title = "Brochures – 500 Nos",
                chapterName = "JCI Nellore",
                orderDate = "05 Jan 2027",
                amount = 7500.0,
                status = "Completed",
                itemsSummary = "500 Tri-fold brochures for youth conclave",
                confirmedDate = "05 Jan 2027",
                inProductionDate = "06 Jan 2027",
                readyDate = "07 Jan 2027",
                deliveredDate = "08 Jan 2027"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Orders", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Pills (All, In Production, Completed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All (8)", "In Production (3)", "Completed (5)").forEach { tabText ->
                    val tabKey = tabText.substringBefore(" ")
                    val isSelected = selectedFilter == tabKey
                    Surface(
                        onClick = { selectedFilter = tabKey },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0066CC) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0066CC) else Color(0xFFCBD5E1)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            tabText,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Order Cards List
            val displayedList = ordersList.filter {
                if (selectedFilter == "In Production") it.status == "In Production"
                else if (selectedFilter == "Completed") it.status == "Completed"
                else true
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedList) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    order.orderId,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )

                                val (statusBg, statusFg) = when (order.status) {
                                    "In Production" -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
                                    "Confirmed" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                                    else -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                }

                                Surface(
                                    color = statusBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        order.status,
                                        color = statusFg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                order.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )

                            Text(
                                "🏛️ ${order.chapterName}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📅 ${order.orderDate}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    "₹${order.amount.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

