package com.jci2027.lead360.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MemberModel(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val email: String,
    val businessName: String,
    val businessCategory: String,
    val status: String // Active, Pending
)

data class ScannedBusinessCard(
    val id: String,
    val contactName: String,
    val company: String,
    val designation: String,
    val phone: String,
    val email: String,
    val scannedAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersAndNetworkingScreen(
    chapterName: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Directory, 1: Pending Approvals, 2: Business Card Scanner
    var showQrDialog by remember { mutableStateOf(false) }
    var showScanSimulateDialog by remember { mutableStateOf(false) }

    val members = remember {
        mutableStateListOf(
            MemberModel("m1", "JC Doraswamy Raju", "President 2027", "+91 98765 43210", "president@jcitirupati.org", "TechInnovations Labs", "IT & Software", "Active"),
            MemberModel("m2", "JC Kiran Kumar", "Secretary", "+91 98480 12345", "secretary@jcitirupati.org", "Kiran Logistics & Cargo", "Supply Chain", "Active"),
            MemberModel("m3", "JC Venkat Raman", "Treasurer", "+91 99887 76655", "treasurer@jcitirupati.org", "VR Tax & Financials", "Accounting & Audit", "Active"),
            MemberModel("m4", "JC Anita Desai", "VP - Training", "+91 97000 11223", "anita@gmail.com", "Sparkle Skill Academy", "Education & Coaching", "Active"),
            MemberModel("m5", "JC Ramesh Babu", "VP - Community", "+91 94400 33445", "ramesh@gmail.com", "Babu Agro Seeds", "Agriculture & Farming", "Active"),
            MemberModel("m6", "JC Hemanth Varma", "VP - Business", "+91 91234 56789", "hemanth@varma.in", "Varma Realty Group", "Real Estate & Construction", "Active")
        )
    }

    val pendingQueue = remember {
        mutableStateListOf(
            MemberModel("p1", "Mr. Siddharth Rao", "Applicant", "+91 99112 23344", "siddharth@startup.io", "CloudScale SaaS", "Software & Cloud", "Pending"),
            MemberModel("p2", "Ms. Lavanya Reddy", "Applicant", "+91 98877 66554", "lavanya@designhub.in", "Reddy Architectural Studio", "Architecture", "Pending")
        )
    }

    val scannedCards = remember {
        mutableStateListOf(
            ScannedBusinessCard("sc1", "Mahesh Naidu", "AP Chamber of Commerce", "Director - SME Relations", "+91 94411 22334", "mahesh@apchamber.in", "Today, 11:30 AM"),
            ScannedBusinessCard("sc2", "Dr. Shalini Gupta", "Apollo Clinic Tirupati", "Chief Medical Consultant", "+91 98220 54321", "drshalini@apollo.com", "Yesterday")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Members & Networking", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text(chapterName, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                actions = {
                    TextButton(onClick = { showQrDialog = true }) {
                        Text("📲 Member Reg QR", fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
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
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0097D8)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("👥 Directory (${members.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⏳ Approvals (${pendingQueue.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📇 Scan Card", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // DIRECTORY
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(members) { member ->
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
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(member.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF0097D8), fontSize = 15.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                        Text(member.role, fontSize = 12.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(4.dp))
                                        Text("💼 ${member.businessName} • ${member.businessCategory}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("📞 ${member.phone}", fontSize = 11.sp, color = Color(0xFF475569))
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // PENDING REGISTRATIONS QUEUE
                    if (pendingQueue.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending member applications.", color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingQueue) { pending ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(pending.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                                                Text("NEW APPLICANT", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text("Enterprise: ${pending.businessName} (${pending.businessCategory})", fontSize = 12.sp, color = Color(0xFF475569))
                                        Text("Contact: ${pending.phone} | ${pending.email}", fontSize = 12.sp, color = Color(0xFF64748B))
                                        Spacer(Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    pendingQueue.remove(pending)
                                                    Toast.makeText(context, "Application rejected.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject", color = Color(0xFFEF4444))
                                            }
                                            Button(
                                                onClick = {
                                                    pendingQueue.remove(pending)
                                                    members.add(pending.copy(status = "Active", role = "Active Member"))
                                                    Toast.makeText(context, "${pending.name} approved as Active Member!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Text("Approve Member")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // BUSINESS CARD SCANNER (OCR LOGIC)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0097D8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📷 AI Business Card Scanner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Scan physical visiting cards during JCI zone conferences, B2B meets, and meetings. Extract details into phone and LEAD360 Rolodex.",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { showScanSimulateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("📸 Capture / Scan New Card", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("SAVED BUSINESS NETWORKING CARDS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF64748B))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(scannedCards) { card ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(card.contactName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                            Text(card.scannedAt, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                        Text("${card.designation} • ${card.company}", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(6.dp))
                                        Text("📞 ${card.phone}  |  ✉️ ${card.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Exported ${card.contactName} to Phone Contacts (vCard)!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("💾 Save to Phone Contacts (vCard)", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Member Registration QR Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Chapter Member Registration QR", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF0097D8), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏁\n[QR CODE]\nlead360.in/reg\n${chapterName.take(12)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Display this QR code at General Body Meetings & Induction ceremonies. Prospective members scan, fill self-service details, and appear in your Approval Queue.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Registration Link & QR Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                    showQrDialog = false
                }) { Text("Share QR Link") }
            },
            dismissButton = { TextButton(onClick = { showQrDialog = false }) { Text("Close") } }
        )
    }

    // Card Scanner Simulation Dialog
    if (showScanSimulateDialog) {
        var cName by remember { mutableStateOf("Rajesh Singhania") }
        var cCompany by remember { mutableStateOf("Apex Solar Energies") }
        var cRole by remember { mutableStateOf("Managing Director") }
        var cPhone by remember { mutableStateOf("+91 98490 99887") }
        var cEmail by remember { mutableStateOf("rajesh@apexsolar.in") }

        AlertDialog(
            onDismissRequest = { showScanSimulateDialog = false },
            title = { Text("Card Scanned Successfully (OCR)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parsed details from camera capture:", fontSize = 12.sp, color = Color(0xFF64748B))
                    OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cCompany, onValueChange = { cCompany = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cRole, onValueChange = { cRole = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cPhone, onValueChange = { cPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cEmail, onValueChange = { cEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    scannedCards.add(0, ScannedBusinessCard("sc_${System.currentTimeMillis()}", cName, cCompany, cRole, cPhone, cEmail, "Just now"))
                    Toast.makeText(context, "Card saved to LEAD360 Business Directory!", Toast.LENGTH_SHORT).show()
                    showScanSimulateDialog = false
                }) { Text("Save Card") }
            },
            dismissButton = { TextButton(onClick = { showScanSimulateDialog = false }) { Text("Cancel") } }
        )
    }
}

