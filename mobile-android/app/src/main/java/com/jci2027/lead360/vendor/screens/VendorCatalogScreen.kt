package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.CatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorCatalogScreen(
    onBack: () -> Unit = {},
    onProductClick: (CatalogItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Mementoes", "Banners", "Brochures", "Merchandise")

    val catalogItems = remember {
        listOf(
            CatalogItem(
                id = "cat_1",
                name = "JCI Mementoes",
                category = "Mementos",
                price = 1250.0,
                unit = "piece",
                description = "Premium crystal memento with official JCI logo engraving. Ideal for installations, awards and recognitions.",
                productCountInCat = 28
            ),
            CatalogItem(
                id = "cat_2",
                name = "Banners & Standees",
                category = "Banners",
                price = 850.0,
                unit = "piece",
                description = "High resolution star flex roll-up standees and stage backdrops conforming to JCI color guidelines.",
                productCountInCat = 16
            ),
            CatalogItem(
                id = "cat_3",
                name = "Brochures & Flyers",
                category = "Brochures",
                price = 12.0,
                unit = "copy",
                description = "300 GSM art card tri-fold brochures and flyers with spot UV lamination for chapter projects.",
                productCountInCat = 12
            ),
            CatalogItem(
                id = "cat_4",
                name = "Apparel & Merchandise",
                category = "Merchandise",
                price = 450.0,
                unit = "piece",
                description = "Official royal blue polo t-shirts, lapel pins, ties and collar badges with embroidered JCI crest.",
                productCountInCat = 10
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Catalog", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products (e.g., Memento, Banner)", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                leadingIcon = { Text("🔍", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0066CC) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0066CC) else Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Products 2-Column Grid
            val filteredList = catalogItems.filter {
                (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true) || it.name.contains(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                val itemEmoji = when {
                                    item.name.contains("Memento", ignoreCase = true) -> "🏆"
                                    item.name.contains("Banner", ignoreCase = true) -> "🖼️"
                                    item.name.contains("Brochure", ignoreCase = true) -> "📑"
                                    else -> "👕"
                                }
                                Text(itemEmoji, fontSize = 44.sp)
                            }

                            Text(
                                item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Text(
                                "${item.productCountInCat} Products",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

