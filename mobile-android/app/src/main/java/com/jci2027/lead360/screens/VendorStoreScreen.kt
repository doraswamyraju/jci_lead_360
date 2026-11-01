package com.jci2027.lead360.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class StoreItem(
    val id: String,
    val title: String,
    val vendorName: String,
    val category: String,
    val priceEstimate: String,
    val leadTime: String,
    val rating: String,
    val iconEmoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStoreScreen(
    chapterName: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var showQuoteDialog by remember { mutableStateOf<StoreItem?>(null) }

    val categories = listOf("All", "Momentos & Trophies", "Banners & Printing", "JCI Badges & Pins", "Event Tech & AV")

    val storeItems = remember {
        listOf(
            StoreItem("s1", "Official JCI 2027 President Gold Collar Pin", "National Insignia Makers", "JCI Badges & Pins", "₹450 / pc", "3 Days", "⭐ 4.9 (42 LOs)", "🎖️"),
            StoreItem("s2", "Crystal Presidential Memento with Gold Etching", "Prestige Awards & Trophies", "Momentos & Trophies", "₹1,200 / pc", "4 Days", "⭐ 4.8 (85 LOs)", "🏆"),
            StoreItem("s3", "Heavy Duty Roll-up Standee (6x3 ft)", "ColorCraft Digital Press", "Banners & Printing", "₹1,450 / unit", "24 Hours", "⭐ 4.7 (120 LOs)", "🎪"),
            StoreItem("s4", "Satin LO President & Chief Guest Sash with Crest", "Regal Embroidery Works", "JCI Badges & Pins", "₹650 / pc", "2 Days", "⭐ 4.9 (64 LOs)", "🎗️"),
            StoreItem("s5", "Acrylic Certificate Frames with Laser Crest", "CraftPoint Displays", "Momentos & Trophies", "₹380 / pc", "3 Days", "⭐ 4.6 (50 LOs)", "📜"),
            StoreItem("s6", "JCI Stage LED Screen & Audio Setup Rental", "SoundWave Events", "Event Tech & AV", "₹18,000 / day", "Same Day", "⭐ 4.9 (31 LOs)", "🎤")
        )
    }

    val filteredItems = if (selectedCategory == "All") storeItems else storeItems.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("JCI Vendor Store & Marketplace", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text("Verified Official Suppliers for $chapterName", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
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
        ) {
            // Category Filter Pills
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color.White,
                edgePadding = 16.dp
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(
                                cat,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == cat) Color(0xFF0097D8) else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.iconEmoji, fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("By ${item.vendorName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.priceEstimate, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF059669))
                                    Spacer(Modifier.width(10.dp))
                                    Text("⚡ ${item.leadTime}", fontSize = 11.sp, color = Color(0xFF0284C7))
                                    Spacer(Modifier.width(10.dp))
                                    Text(item.rating, fontSize = 11.sp, color = Color(0xFFD97706))
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { showQuoteDialog = item },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Get Quote", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Quote Dialog
    showQuoteDialog?.let { item ->
        var qty by remember { mutableStateOf("25") }
        var notes by remember { mutableStateOf("Need with JCI Tirupati Innovations official 2027 logo") }
        AlertDialog(
            onDismissRequest = { showQuoteDialog = null },
            title = { Text("Request Quotation: ${item.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vendor: ${item.vendorName}", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Required Quantity") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Customization Brief / Event Date") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Quote request sent to ${item.vendorName}! They will connect via WhatsApp.", Toast.LENGTH_LONG).show()
                    showQuoteDialog = null
                }) { Text("Send Quote Request") }
            },
            dismissButton = { TextButton(onClick = { showQuoteDialog = null }) { Text("Cancel") } }
        )
    }
}

