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
    var currentStatus by remember { mutableStateOf(order.status) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Order Details", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            currentStatus = if (currentStatus == "In Production") "Ready" else "Delivered"
                            Toast.makeText(context, "Order status updated to $currentStatus!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066CC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✓", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                if (currentStatus == "In Production") "Mark as Ready" else "Mark as Delivered",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.orderId, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))

                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                currentStatus,
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(order.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF334155))
                    Text("🏛️ ${order.chapterName}", fontSize = 12.sp, color = Color(0xFF64748B))

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Order Value", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("₹${order.amount.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    }
                }
            }

            // Milestone Tracker (Confirmed -> In Production -> Ready -> Delivered)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("PRODUCTION TIMELINE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MilestoneNode("Confirmed", "10 Jan", isCompleted = true)
                        MilestoneLine(isCompleted = true)
                        MilestoneNode("In Production", "11 Jan", isCompleted = true)
                        MilestoneLine(isCompleted = currentStatus == "Ready" || currentStatus == "Delivered")
                        MilestoneNode("Ready", if (currentStatus == "Ready") "Today" else "-", isCompleted = currentStatus == "Ready" || currentStatus == "Delivered")
                        MilestoneLine(isCompleted = currentStatus == "Delivered")
                        MilestoneNode("Delivered", "-", isCompleted = currentStatus == "Delivered")
                    }
                }
            }

            // Quick Access Action Rows (Order Items, Production Update, Documents, Chat)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    OrderActionRow(icon = "📦", title = "Order Items", subtitle = "View detailed specs & dimensions") {
                        Toast.makeText(context, "Items: ${order.itemsSummary}", Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    OrderActionRow(icon = "⚙️", title = "Production Update", subtitle = "Log stages, proof approval & dispatch info") {
                        Toast.makeText(context, "Production updated", Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    OrderActionRow(icon = "📄", title = "Documents (Artwork, PO, Invoice)", subtitle = "Print-ready high-res PDF & chapter PO") {
                        Toast.makeText(context, "Documents folder opened", Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    OrderActionRow(icon = "💬", title = "Chat with Organizer", subtitle = "Direct line to Chapter President & PST") {
                        Toast.makeText(context, "Opening direct chat...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneNode(label: String, date: String, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF0066CC) else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isCompleted) "✓" else "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCompleted) Color(0xFF0F172A) else Color(0xFF94A3B8))
        Text(date, fontSize = 8.sp, color = Color(0xFF64748B))
    }
}

@Composable
fun MilestoneLine(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(2.dp)
            .background(if (isCompleted) Color(0xFF0066CC) else Color(0xFFE2E8F0))
    )
}

@Composable
fun OrderActionRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon, fontSize = 18.sp)
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
        Text("›", fontSize = 20.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
    }
}

