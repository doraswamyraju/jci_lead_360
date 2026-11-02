package com.jci2027.lead360.vendor.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.VendorOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrderDetailScreen(
    order: VendorOrder,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val royalBlue = Color(0xFF0D47A1)
    var currentStatus by remember { mutableStateOf(order.status) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order Details",
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
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val nextStatus = when (currentStatus) {
                                "Confirmed" -> "In Production"
                                "In Production" -> "Ready"
                                "Ready" -> "Delivered"
                                else -> "Delivered"
                            }
                            currentStatus = nextStatus
                            Toast.makeText(context, "Order updated to: $nextStatus", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = royalBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Text(
                                text = when (currentStatus) {
                                    "Confirmed" -> "Start Production"
                                    "In Production" -> "Mark as Ready"
                                    "Ready" -> "Mark as Delivered"
                                    else -> "Order Completed"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Order ID, Title & Amount
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
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
                            fontSize = 17.sp,
                            color = royalBlue
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (currentStatus) {
                                "Delivered", "Completed" -> Color(0xFFDCFCE7)
                                "In Production" -> Color(0xFFEFF6FF)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                currentStatus,
                                color = when (currentStatus) {
                                    "Delivered", "Completed" -> Color(0xFF15803D)
                                    "In Production" -> royalBlue
                                    else -> Color(0xFFB45309)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        order.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🏛️", fontSize = 13.sp)
                            Text(
                                order.chapterName,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            "₹${String.format("%,.0f", order.amount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Production Milestone Step Tracker matching Reference Column 3 Bottom
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Production Milestones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        MilestoneNode("Confirmed", "10 Jan", isDone = true, isCurrent = currentStatus == "Confirmed")
                        MilestoneLine(isDone = currentStatus in listOf("In Production", "Ready", "Delivered", "Completed"))
                        MilestoneNode("In Production", "11 Jan", isDone = currentStatus in listOf("In Production", "Ready", "Delivered", "Completed"), isCurrent = currentStatus == "In Production")
                        MilestoneLine(isDone = currentStatus in listOf("Ready", "Delivered", "Completed"))
                        MilestoneNode("Ready", "-", isDone = currentStatus in listOf("Ready", "Delivered", "Completed"), isCurrent = currentStatus == "Ready")
                        MilestoneLine(isDone = currentStatus in listOf("Delivered", "Completed"))
                        MilestoneNode("Delivered", "-", isDone = currentStatus in listOf("Delivered", "Completed"), isCurrent = currentStatus in listOf("Delivered", "Completed"))
                    }
                }
            }

            // Downloadable Assets & Documents Section (Requirement #2)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
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
                            "Production Assets & Artwork",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "${order.assets.size} Files",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    order.assets.forEach { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📄", fontSize = 20.sp)
                                Column {
                                    Text(
                                        asset.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1
                                    )
                                    Text(
                                        asset.size,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Downloading ${asset.name}...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Download",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = royalBlue
                                )
                            }
                        }
                    }
                }
            }

            // Order Items & Delivery Breakdown
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Order Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        order.itemsSummary,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("₹${String.format("%,.0f", order.amount * 0.82)}", color = Color(0xFF0F172A), fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GST (18%)", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("₹${String.format("%,.0f", order.amount * 0.18)}", color = Color(0xFF0F172A), fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                        Text("₹${String.format("%,.0f", order.amount)}", fontWeight = FontWeight.Black, color = royalBlue, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MilestoneNode(label: String, date: String, isDone: Boolean, isCurrent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isDone) Color(0xFF0D47A1)
                    else if (isCurrent) Color(0xFF60A5FA)
                    else Color(0xFFCBD5E1)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = if (isDone || isCurrent) Color(0xFF0F172A) else Color(0xFF94A3B8)
        )
        Text(date, fontSize = 9.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun RowScope.MilestoneLine(isDone: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(top = 11.dp)
            .background(if (isDone) Color(0xFF0D47A1) else Color(0xFFE2E8F0))
    )
}
