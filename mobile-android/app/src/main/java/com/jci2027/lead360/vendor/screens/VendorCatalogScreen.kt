package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
    catalogList: List<CatalogItem>,
    onBack: () -> Unit = {},
    onProductClick: (CatalogItem) -> Unit = {},
    onAddNewProduct: () -> Unit = {}
) {
    val royalBlue = Color(0xFF0D47A1)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Mementoes", "Banners", "Brochures", "Merchandise")

    val filteredItems = catalogList.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Catalog",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
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
                actions = {
                    IconButton(onClick = onAddNewProduct) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Product",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = royalBlue)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewProduct,
                containerColor = royalBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Item", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input with Magnifier Icon (Reference Column 3 Top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products (e.g., Memento, Banner)", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = royalBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Pills matching Reference exactly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) royalBlue else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2-Column Grid of Catalog Products matching Reference Column 3
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(item) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Official Watermarked Product Showcase Tile
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        when (item.category.lowercase()) {
                                            "mementoes" -> "🏆"
                                            "banners" -> "🎪"
                                            "brochures" -> "📑"
                                            "merchandise" -> "👕"
                                            else -> "📦"
                                        },
                                        fontSize = 38.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "JCI APPROVED",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = royalBlue,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${item.productCountInCat.coerceAtLeast(10)} Products Available",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "₹${String.format("%,.0f", item.price)} / ${item.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = royalBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
