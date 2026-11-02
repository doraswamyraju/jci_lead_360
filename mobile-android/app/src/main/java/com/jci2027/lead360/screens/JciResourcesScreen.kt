package com.jci2027.lead360.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.engine.JciLogoGenerator
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JciResourcesScreen(
    chapterName: String = "JCI Tirupati",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(0) } // 0: Logo Generator, 1: Letterhead, 2: Visiting Card, 3: Certificate

    val categories = listOf(
        ResourceCategory("logo", "🎨 Official Logo", "Active Tool"),
        ResourceCategory("letterhead", "📄 Letterhead", "Coming Soon"),
        ResourceCategory("visiting_card", "🪪 Visiting Card", "Coming Soon"),
        ResourceCategory("certificate", "🏆 Certificate", "Coming Soon")
    )

    // Logo Generator Engine & State
    val logoGenerator = remember { JciLogoGenerator(context) }
    var line1 by remember { mutableStateOf(chapterName.removePrefix("JCI ").trim().ifBlank { "Tirupati" }) }
    var line2 by remember { mutableStateOf("") }
    val logoCases = remember { JciLogoGenerator.CaseStyle.values() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("JCI Resources & Identity Hub", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text("Corporate Brand Assets, Generators & Kits", fontSize = 11.sp, color = Color(0xFF64748B))
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Categories Scroll / Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                containerColor = Color.White,
                contentColor = Color(0xFF0097D8),
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    cat.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Medium
                                )
                                if (cat.badge == "Coming Soon") {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            "Soon",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            when (selectedCategory) {
                0 -> {
                    // 1. OFFICIAL JCI LOGO GENERATOR (FULL FEATURED ENGINE)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "JCI - Official Chapter Logo Generator",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF130F2D)
                                )
                                Text(
                                    text = "Generates standard corporate-identity chapter brand lockups conforming to JCI Corporate Identity Guidelines (all 7 official styles)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                                )

                                OutlinedTextField(
                                    value = line1,
                                    onValueChange = { line1 = it },
                                    label = { Text("Line 1 (e.g. Tirupati or Ath)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = line2,
                                    onValueChange = { line2 = it },
                                    label = { Text("Line 2 (Optional, e.g. Innovations or Region)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Text(
                            text = "OFFICIAL BRAND CASES & FORMATS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64748B)
                        )

                        logoCases.forEach { caseStyle ->
                            val logoBmp = remember(line1, line2, caseStyle) {
                                logoGenerator.renderLogoBitmap(line1, line2, caseStyle)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(caseStyle.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Surface(
                                            color = Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Vector Ready",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Logo preview box with matching background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .background(
                                                color = Color(caseStyle.bgType.hexColor),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = logoBmp.asImageBitmap(),
                                            contentDescription = caseStyle.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .padding(8.dp),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons for saving and sharing
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_${caseStyle.name}.png")
                                                if (file != null) {
                                                    Toast.makeText(context, "Saved PNG to Pictures/JCI_Logos!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
                                        ) {
                                            Text("💾 Save PNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_${caseStyle.name}.png")
                                                if (file != null) {
                                                    val caption = "Official JCI Logo (${caseStyle.title}) for $line1"
                                                    AndroidShareManager.shareToWhatsApp(context, file, caption, "image/png")
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("📤 Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // LETTERHEAD GENERATOR COMING SOON
                    ComingSoonResourceCard(
                        emoji = "📄",
                        title = "Official Chapter Letterhead Generator",
                        description = "Automated high-resolution PDF & DOCX executive chapter letterhead with official JCI crest, President signature line, secretary contact footer, and tax ID fields.",
                        expectedDate = "Phase 2 Rollout • December 2026",
                        features = listOf(
                            "Formal Presidential Communication Template",
                            "Official Chapter Crest & Zone Lockup",
                            "Digital Signature & Stamp Placement",
                            "One-Click Export to PDF & Word Format"
                        )
                    )
                }
                2 -> {
                    // JCI VISITING CARD GENERATOR COMING SOON
                    ComingSoonResourceCard(
                        emoji = "🪪",
                        title = "JCI Visiting Card & Business Badge Generator",
                        description = "Standard 3.5\" x 2\" print-ready corporate identity business card generator with dynamic reciprocal QR code, LO officer titles, and dual-sided layout.",
                        expectedDate = "Phase 2 Rollout • December 2026",
                        features = listOf(
                            "Front & Back Print-Ready PDF (300 DPI with Bleed)",
                            "Embedded Dynamic Reciprocal Exchange QR Code",
                            "Official JCI India & World Corporate Branding",
                            "Officer Role-specific color codes & crests"
                        )
                    )
                }
                3 -> {
                    // CERTIFICATE GENERATOR COMING SOON
                    ComingSoonResourceCard(
                        emoji = "🏆",
                        title = "JCI Official Certificate & Award Generator",
                        description = "Generate batch certificates for Outstanding Project Recognition, Guest Speaker Felicitations, Member of the Month, and Conference Participation with 1 click.",
                        expectedDate = "Phase 2 Rollout • January 2027",
                        features = listOf(
                            "Award & Recognition Official Guilloche Borders",
                            "Automated Name & Citation Merge from Directory",
                            "President & Secretary Dual Signature Blocks",
                            "Print-Ready Landscape Vector PDF Export"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ComingSoonResourceCard(
    emoji: String,
    title: String,
    description: String,
    expectedDate: String,
    features: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        "🚧 COMING SOON",
                        color = Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    description,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "CORE CAPABILITIES BEING CONFIGURED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                features.forEach { feat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("✨", fontSize = 13.sp)
                        Text(feat, fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🗓️", fontSize = 14.sp)
                        Text("Release Timeline: $expectedDate", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

data class ResourceCategory(
    val id: String,
    val title: String,
    val badge: String
)

