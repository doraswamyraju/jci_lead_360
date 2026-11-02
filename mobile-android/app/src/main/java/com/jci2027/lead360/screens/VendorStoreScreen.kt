package com.jci2027.lead360.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.jci2027.lead360.vendor.model.CatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStoreScreen(
    chapterName: String,
    onBack: () -> Unit = {},
    liveCatalog: List<CatalogItem> = emptyList(),
    onRequestQuote: (CatalogItem) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Mementoes", "Banners", "Brochures", "Merchandise")

    val displayItems = liveCatalog.ifEmpty {
        listOf(
            CatalogItem("cat_01", "JCI Crystal Memento", "Mementoes", 1250.0, "piece", "Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions.", leadTime = "3-4 Days", vendorName = "Rs Creations"),
            CatalogItem("cat_02", "Banners & Standees", "Banners", 1450.0, "unit", "Star flex heavy duty roll-up standees (6x3 ft) with metal hardware & zip carry case.", leadTime = "24 Hours", vendorName = "Rs Creations"),
            CatalogItem("cat_03", "Brochures & Flyers", "Brochures", 8.5, "piece", "170 GSM art paper tri-fold brochures with glossy lamination for chapter events.", leadTime = "2 Days", vendorName = "Rs Creations"),
            CatalogItem("cat_04", "Apparel & Merchandise", "Merchandise", 450.0, "piece", "Official JCI dry-fit collared polo t-shirt with embroidered crest on chest.", leadTime = "5 Days", vendorName = "Rs Creations")
        )
    }

    val filteredItems = if (selectedCategory == "All") {
        displayItems
    } else {
        displayItems.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "JCI Vendor Store & Marketplace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "Official Live Suppliers for $chapterName",
                            fontSize = 11.sp,
                            color = Color(0xFFBBDEFB)
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0D47A1) else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
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
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (item.category.lowercase()) {
                                        "mementoes" -> "🏆"
                                        "banners" -> "🎪"
                                        "brochures" -> "📑"
                                        "merchandise" -> "👕"
                                        else -> "📦"
                                    },
                                    fontSize = 32.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Supplied by ${item.vendorName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0D47A1),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Turnaround: ${item.leadTime}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "₹${String.format("%,.0f", item.price)} / ${item.unit}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Button(
                                onClick = {
                                    onRequestQuote(item)
                                    Toast.makeText(context, "Quote request created for ${item.name}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Quote", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
