package com.jci2027.lead360.vendor.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomQuoteScreen(
    initialTitle: String = "Installation Backdrop & Banners",
    onBack: () -> Unit = {},
    onQuoteSubmitted: () -> Unit = {}
) {
    val context = LocalContext.current
    var isCustomReq by remember { mutableStateOf(true) } // true: Custom Requirement, false: Bulk Order

    var title by remember { mutableStateOf(initialTitle) }
    var details by remember { mutableStateOf("Need 20 ft backdrop, 2 roll-up banners and 100 brochures for Installation.") }
    var eventDate by remember { mutableStateOf("15 Jan 2027") }
    var attachedDocName by remember { mutableStateOf("JCI_Theme_Reference.pdf") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Custom Quote", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Selector: Custom Requirement vs Bulk Order (Pill Tabs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { isCustomReq = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCustomReq) Color(0xFF0066CC) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustomReq) Color(0xFF0066CC) else Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📝", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Custom Requirement",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomReq) Color.White else Color(0xFF334155)
                        )
                    }
                }

                Surface(
                    onClick = { isCustomReq = false },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isCustomReq) Color(0xFF0066CC) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (!isCustomReq) Color(0xFF0066CC) else Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📦", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Bulk Order",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCustomReq) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            // Input Form Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Requirement Title
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("Requirement Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text(" *", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("e.g. Installation Backdrop & Banners") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Details
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text(" *", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = details,
                            onValueChange = { details = it },
                            placeholder = { Text("Describe dimensions, quantities, finishes...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Event Date
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Event Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            leadingIcon = { Text("📅", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Attach Reference (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Attach Reference (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

                        if (attachedDocName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("📎", fontSize = 14.sp)
                                        Text(attachedDocName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                    }
                                    IconButton(
                                        onClick = { attachedDocName = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("✕", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { attachedDocName = "Artwork_Sample.pdf" },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📎 Browse File / Artwork", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    Toast.makeText(context, "Quote request submitted successfully to organizer!", Toast.LENGTH_SHORT).show()
                    onQuoteSubmitted()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066CC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Submit Quote Request", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

