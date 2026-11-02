package com.jci2027.lead360.vendor.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.CatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCatalogItemScreen(
    onBack: () -> Unit = {},
    onItemCreated: (CatalogItem) -> Unit = {}
) {
    val context = LocalContext.current
    val royalBlue = Color(0xFF0D47A1)

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mementoes") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var leadTime by remember { mutableStateOf("3-4 Days") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf("Mementoes", "Banners", "Brochures", "Merchandise", "Badges & Pins", "Event Tech")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add to Catalog",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = royalBlue)
            )
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Product Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product / Service Name *") },
                        placeholder = { Text("e.g. Official JCI Crystal Award Memento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Category Selector Pills
                    Text(
                        "Category",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) royalBlue else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { category = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Unit Price (₹) *") },
                            placeholder = { Text("1250") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("piece / unit / set") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = leadTime,
                        onValueChange = { leadTime = it },
                        label = { Text("Standard Production Turnaround") },
                        placeholder = { Text("e.g. 24 Hours / 3-4 Days") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Specifications & Material Description") },
                        placeholder = { Text("Enter size dimensions, crystal thickness, printing options, box packaging, etc.") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Photo / Design Upload Simulation Card
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable {
                                Toast.makeText(context, "Official JCI Product photo attached", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📸", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Attach Product Sample Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = royalBlue
                            )
                            Text(
                                "PNG, JPG up to 10MB (Official watermark will be applied)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Please enter Product Name and Price", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    val newItem = CatalogItem(
                        id = "cat_${System.currentTimeMillis()}",
                        name = name.trim(),
                        category = category,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = unit.ifBlank { "piece" },
                        leadTime = leadTime.ifBlank { "3-4 Days" },
                        description = description.ifBlank { "Official JCI customized supply item." },
                        imageUrl = "/assets/logo.png"
                    )
                    Toast.makeText(context, "Product published to catalog!", Toast.LENGTH_SHORT).show()
                    onItemCreated(newItem)
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = royalBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    if (isSubmitting) "Publishing..." else "Publish to Live Catalog",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

