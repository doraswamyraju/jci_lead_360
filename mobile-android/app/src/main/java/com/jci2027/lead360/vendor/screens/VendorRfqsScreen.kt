package com.jci2027.lead360.vendor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.RfqItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorRfqsScreen(
    onBack: () -> Unit = {},
    onRfqSelected: (RfqItem) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Open") } // Open, Quoted, Closed

    val rfqList = remember {
        listOf(
            RfqItem(
                id = "rfq_1",
                title = "Mementoes for Awards",
                chapterName = "JCI Tirupati Innovations",
                eventDate = "20 Jan 2027",
                status = "Open",
                isNew = true,
                emoji = "🏆",
                description = "25 crystal mementoes with golden crest engraving for annual excellence awards."
            ),
            RfqItem(
                id = "rfq_2",
                title = "Installation Backdrop Set",
                chapterName = "JCI Chittoor",
                eventDate = "15 Jan 2027",
                status = "Open",
                isNew = true,
                emoji = "🖼️",
                description = "20x10 star flex backdrop with wooden frame mounting and 2 presidential podium covers."
            ),
            RfqItem(
                id = "rfq_3",
                title = "Brochures – 500 Nos",
                chapterName = "JCI Nellore",
                eventDate = "10 Jan 2027",
                status = "Open",
                isNew = false,
                emoji = "📑",
                description = "Tri-fold color brochures on 250 GSM art paper for community project promotion."
            ),
            RfqItem(
                id = "rfq_4",
                title = "T-shirts with JCI Logo",
                chapterName = "JCI Kadapa",
                eventDate = "25 Jan 2027",
                status = "Open",
                isNew = false,
                emoji = "👕",
                description = "100 dry-fit collared polo t-shirts in royal blue with chapter embroidery."
            ),
            RfqItem(
                id = "rfq_5",
                title = "Stage Podium & Badges",
                chapterName = "JCI Tirupati",
                eventDate = "05 Jan 2027",
                status = "Quoted",
                isNew = false,
                emoji = "🏷️",
                description = "Magnetic metal name badges for all 15 LGB members + presidential collar chain.",
                quotedAmount = 14500.0
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("RFQs (Requests for Quote)", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
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
            // Filter Tabs (Open, Quoted, Closed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Open (12)", "Quoted (4)", "Closed (18)").forEach { tabText ->
                    val tabKey = tabText.substringBefore(" ")
                    val isSelected = selectedFilter == tabKey
                    Surface(
                        onClick = { selectedFilter = tabKey },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0066CC) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0066CC) else Color(0xFFCBD5E1)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            tabText,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // RFQ Cards List
            val displayedList = rfqList.filter {
                if (selectedFilter == "Open") it.status == "Open"
                else if (selectedFilter == "Quoted") it.status == "Quoted"
                else it.status == "Closed"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedList) { rfq ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRfqSelected(rfq) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rfq.emoji, fontSize = 20.sp)
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        rfq.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (rfq.isNew) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "New",
                                                color = Color(0xFFD97706),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "🏛️ ${rfq.chapterName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📅 Event: ${rfq.eventDate}", fontSize = 11.sp, color = Color(0xFF0066CC), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Text("›", fontSize = 20.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

