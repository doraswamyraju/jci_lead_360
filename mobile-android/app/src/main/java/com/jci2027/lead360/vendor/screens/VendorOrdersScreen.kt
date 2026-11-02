package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.VendorOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrdersScreen(
    ordersList: List<VendorOrder>,
    onBack: () -> Unit = {},
    onOrderClick: (VendorOrder) -> Unit = {}
) {
    val royalBlue = Color(0xFF0D47A1)
    var selectedFilter by remember { mutableStateOf("All") } // All, In Production, Completed

    val filteredOrders = when (selectedFilter) {
        "In Production" -> ordersList.filter { it.status == "In Production" }
        "Completed" -> ordersList.filter { it.status in listOf("Completed", "Delivered") }
        else -> ordersList
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Orders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = royalBlue)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Pills (All, In Production, Completed) matching Reference Bottom Column 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "All (${ordersList.size})" to "All",
                    "In Production (${ordersList.count { it.status == "In Production" }})" to "In Production",
                    "Completed (${ordersList.count { it.status in listOf("Completed", "Delivered") }})" to "Completed"
                ).forEach { (label, key) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) royalBlue else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    order.orderId,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = royalBlue
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (order.status) {
                                        "Delivered", "Completed" -> Color(0xFFDCFCE7)
                                        "In Production" -> Color(0xFFEFF6FF)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ) {
                                    Text(
                                        order.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (order.status) {
                                            "Delivered", "Completed" -> Color(0xFF15803D)
                                            "In Production" -> royalBlue
                                            else -> Color(0xFFB45309)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                order.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🏛️", fontSize = 12.sp)
                                    Text(
                                        order.chapterName,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Text(
                                    "₹${String.format("%,.0f", order.amount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("📅", fontSize = 11.sp)
                                    Text(order.orderDate, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }

                                Text(
                                    "› View & Download Assets",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = royalBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
