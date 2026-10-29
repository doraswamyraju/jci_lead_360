package com.jci2027.lead360.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.engine.JciLogoGenerator
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.launch

data class TeamMemberItem(
    val role: String,
    val name: String,
    val email: String,
    val phone: String,
    val status: String = "Active"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LgbAndPstManagementScreen(
    token: String,
    chapterName: String,
    presidentName: String,
    baseUrl: String = "https://lead.sriddha.com"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val logoGenerator = remember { JciLogoGenerator(context) }

    // 0: Directory, 1: Grant Access, 2: Official Logo Generator (logo.jciath.be replica), 3: LO Profile
    var selectedTab by remember { mutableStateOf(0) }

    // Form inputs for creating credentials
    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberPassword by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf("SECRETARY") }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Logo Generator State - Replica of logo.jciath.be
    val initialCleanName = remember(chapterName) {
        chapterName.replace("JCI", "", ignoreCase = true).trim()
    }
    var line1 by remember { mutableStateOf(if (initialCleanName.isBlank()) "Exemple" else initialCleanName) }
    var line2 by remember { mutableStateOf("") }

    // 7 visual cases from logo.jciath.be
    val logoCases = remember { JciLogoGenerator.CaseStyle.values() }

    // LO Profile details state
    var loZone by remember { mutableStateOf("Zone IV") }
    var loEmail by remember { mutableStateOf("info@jcitirupati.com") }
    var loPhone by remember { mutableStateOf("+91 98765 43210") }
    var isSavedLo by remember { mutableStateOf(false) }

    val currentTeam = remember {
        mutableStateListOf(
            TeamMemberItem("PRESIDENT", presidentName, "president@jcitirupati.com", "+91 98765 43210"),
            TeamMemberItem("SECRETARY", "JC Secretary 2027", "secretary@jcitirupati.com", "+91 98765 43211"),
            TeamMemberItem("TREASURER", "JC Treasurer 2027", "treasurer@jcitirupati.com", "+91 98765 43212")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🏛️ Local Chapter Leadership Suite",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "$chapterName • Year 2027 Administration",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Pills
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF0097D8),
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {}
                ) {
                    listOf(
                        "👥 Directory",
                        "🔑 Grant Access",
                        "🎨 Logo Generator",
                        "🏢 Chapter Details"
                    ).forEachIndexed { idx, label ->
                        val isSelected = selectedTab == idx
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = idx },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF0097D8) else Color(0xFF1E293B),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 0: Board Directory
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACTIVE EXECUTIVE BOARD (${currentTeam.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                currentTeam.forEach { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE0F2FE),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(member.role, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0369A1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(member.email, fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(member.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text("Active", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: Grant Access / Create Login Credentials for PST
        if (selectedTab == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🔑 Create Login Credentials for PST", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text("Generate credentials for your Chapter Secretary or Treasurer to access templates and post on WhatsApp.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 14.dp))

                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Member Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("Login Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberPassword,
                        onValueChange = { newMemberPassword = it },
                        label = { Text("Assign Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberPhone,
                        onValueChange = { newMemberPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("SELECT ROLE DESIGNATION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("SECRETARY", "TREASURER").forEach { r ->
                            val isSel = newMemberRole == r
                            Button(
                                onClick = { newMemberRole = r },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) Color(0xFF0097D8) else Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(r, color = if (isSel) Color.White else Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = if (isError) Color(0xFFDC2626) else Color(0xFF166534),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (newMemberName.isBlank() || newMemberEmail.isBlank() || newMemberPassword.isBlank()) {
                                statusMessage = "Please complete all required fields"
                                isError = true
                                return@Button
                            }
                            currentTeam.add(
                                TeamMemberItem(newMemberRole, newMemberName, newMemberEmail, newMemberPhone)
                            )
                            isError = false
                            statusMessage = "✓ Successfully generated $newMemberRole account credentials!"
                            newMemberName = ""
                            newMemberEmail = ""
                            newMemberPassword = ""
                            newMemberPhone = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isSubmitting
                    ) {
                        Text("Create & Dispatch Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TAB 2: Exact replica of https://logo.jciath.be/
        if (selectedTab == 2) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header & Inputs (Centered Line 1 and optional Line 2)
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
                            text = "JCI - Logo generator",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF130F2D)
                        )
                        Text(
                            text = "Standard corporate-identity chapter brand lockups (replica of logo.jciath.be)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        // Line 1 input (Auto-scaled, centered)
                        OutlinedTextField(
                            value = line1,
                            onValueChange = { line1 = it },
                            label = { Text("Line 1 (e.g. Ath or Tirupati)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Line 2 input (Shown when line 1 exists, allows 2-line lockup)
                        OutlinedTextField(
                            value = line2,
                            onValueChange = { line2 = it },
                            label = { Text("Line 2 (Optional, e.g. Pays Vert)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Cases Grid (Exact replicas of all 7 cases on logo.jciath.be)
                Text(
                    text = "AVAILABLE CASES & FORMATS",
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = caseStyle.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Logo preview box with correct background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (caseStyle.isSocial) 180.dp else 120.dp)
                                    .background(
                                        Color(caseStyle.bgType.hexColor),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = logoBmp.asImageBitmap(),
                                    contentDescription = caseStyle.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: PNG, JPG, Vector SVG (or Social Media)
                            if (caseStyle.isSocial) {
                                Button(
                                    onClick = {
                                        val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_Social.jpg", Bitmap.CompressFormat.JPEG)
                                        val caption = "🏛️ Official JCI Social Media Avatar for *$line1*"
                                        AndroidShareManager.shareToWhatsApp(context, file, caption)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D7)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Text("Social Media (JPG)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // PNG Button
                                    Button(
                                        onClick = {
                                            val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_${caseStyle.name}.png", Bitmap.CompressFormat.PNG)
                                            val caption = "🏛️ Official JCI Logo (*${caseStyle.title}*) for *$line1*"
                                            AndroidShareManager.shareToWhatsApp(context, file, caption)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("PNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // JPG Button
                                    Button(
                                        onClick = {
                                            val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_${caseStyle.name}.jpg", Bitmap.CompressFormat.JPEG)
                                            val caption = "🏛️ Official JCI Logo JPG for *$line1*"
                                            AndroidShareManager.shareToWhatsApp(context, file, caption)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("JPG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Vector File (SVG) Button
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val svgString = logoGenerator.fetchVectorSvg(line1, line2)
                                                val file = logoGenerator.saveSvgFile(svgString, "JCI_${line1}_Vector.svg")
                                                Toast.makeText(context, "Saved Vector SVG to cache!", Toast.LENGTH_SHORT).show()
                                                val caption = "📐 Official JCI Vector File (SVG) for *$line1*"
                                                AndroidShareManager.shareToWhatsApp(context, file, caption)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF091322)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f).height(40.dp)
                                    ) {
                                        Text("Vector File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: LO Profile & Chapter Details
        if (selectedTab == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🏛️ Local Organization (LO) Details", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text("Official chapter parameters managed by Chapter President.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 14.dp))

                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = {},
                        label = { Text("Chapter Organization Name") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loZone,
                        onValueChange = { loZone = it },
                        label = { Text("JCI Zone / Region") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loEmail,
                        onValueChange = { loEmail = it },
                        label = { Text("Chapter Official Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loPhone,
                        onValueChange = { loPhone = it },
                        label = { Text("Chapter Official Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { isSavedLo = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (isSavedLo) "✓ Details Saved" else "Save Chapter Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
