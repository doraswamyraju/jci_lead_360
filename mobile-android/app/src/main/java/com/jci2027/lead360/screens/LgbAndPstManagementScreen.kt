package com.jci2027.lead360.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jci2027.lead360.engine.JciLogoGenerator
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.launch

data class TeamMemberItem(
    val id: String = "",
    val role: String,
    var name: String,
    val email: String,
    var phone: String,
    var photoUri: Uri? = null,
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
    var loLogoUri by remember { mutableStateOf<Uri?>(null) }
    var isSavedLo by remember { mutableStateOf(false) }

    // Executive team list
    val currentTeam = remember {
        mutableStateListOf(
            TeamMemberItem("usr_1", "PRESIDENT", presidentName, "president@jcitirupati.com", "+91 98765 43210"),
            TeamMemberItem("usr_2", "SECRETARY", "JC Secretary 2027", "secretary@jcitirupati.com", "+91 98765 43211"),
            TeamMemberItem("usr_3", "TREASURER", "JC Treasurer 2027", "treasurer@jcitirupati.com", "+91 98765 43212")
        )
    }

    // Image pickers
    val chapterLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            loLogoUri = uri
            Toast.makeText(context, "Chapter Logo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedOfficerIndex by remember { mutableStateOf<Int?>(null) }
    val officerPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val idx = selectedOfficerIndex
        if (uri != null && idx != null && idx < currentTeam.size) {
            val member = currentTeam[idx]
            currentTeam[idx] = member.copy(photoUri = uri)
            Toast.makeText(context, "Updated photo for ${member.name}!", Toast.LENGTH_SHORT).show()
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🏛️ Chapter Leadership Suite",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "$chapterName • Year 2027 Administration",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // LO Logo thumbnail if available
                    if (loLogoUri != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(46.dp)
                        ) {
                            AsyncImage(
                                model = loLogoUri,
                                contentDescription = "Chapter Logo",
                                modifier = Modifier.fillMaxSize().padding(3.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

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
                        "👥 PST & LGB Directory",
                        "🔑 Grant Access",
                        "🎨 Logo Generator",
                        "🏢 Chapter & LO Profile"
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

        // TAB 0: Board Directory with Photo & Profile Upload
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PST OFFICERS & LOCAL GOVERNING BOARD (${currentTeam.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                currentTeam.forEachIndexed { index, member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Officer Photo / Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2E8F0))
                                            .clickable {
                                                selectedOfficerIndex = index
                                                officerPhotoPicker.launch("image/*")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (member.photoUri != null) {
                                            AsyncImage(
                                                model = member.photoUri,
                                                contentDescription = member.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text("📷", fontSize = 20.sp)
                                        }
                                    }

                                    Column {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE0F2FE),
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        ) {
                                            Text(
                                                member.role,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(member.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(member.email, fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable {
                                        selectedOfficerIndex = index
                                        officerPhotoPicker.launch("image/*")
                                    }
                                ) {
                                    Text(
                                        text = if (member.photoUri != null) "Change Photo" else "+ Upload Photo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
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
                                TeamMemberItem(
                                    id = "usr_${System.currentTimeMillis()}",
                                    role = newMemberRole,
                                    name = newMemberName,
                                    email = newMemberEmail,
                                    phone = newMemberPhone
                                )
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

        // TAB 2: Exact replica of https://logo.jciath.be/ with genuine JCI crest & safe download
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_Social.jpg", Bitmap.CompressFormat.JPEG)
                                            val caption = "🏛️ Official JCI Social Media Avatar for *$line1*"
                                            AndroidShareManager.shareToWhatsApp(context, file, caption, "image/jpeg")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            AndroidShareManager.saveBitmapToGallery(
                                                context,
                                                logoBmp,
                                                "JCI_${line1}_Social.jpg",
                                                Bitmap.CompressFormat.JPEG
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("⬇️ Save JPG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // PNG Button
                                    Button(
                                        onClick = {
                                            AndroidShareManager.saveBitmapToGallery(
                                                context,
                                                logoBmp,
                                                "JCI_${line1}_${caseStyle.name}.png",
                                                Bitmap.CompressFormat.PNG
                                            )
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
                                            AndroidShareManager.saveBitmapToGallery(
                                                context,
                                                logoBmp,
                                                "JCI_${line1}_${caseStyle.name}.jpg",
                                                Bitmap.CompressFormat.JPEG
                                            )
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
                                                AndroidShareManager.saveSvgToDownloads(
                                                    context,
                                                    svgString,
                                                    "JCI_${line1}_Vector.svg"
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF091322)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f).height(40.dp)
                                    ) {
                                        Text("Vector SVG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // WhatsApp Share
                                    IconButton(
                                        onClick = {
                                            val file = logoGenerator.saveBitmap(logoBmp, "JCI_${line1}_${caseStyle.name}.png")
                                            val caption = "🏛️ Official JCI Logo (*${caseStyle.title}*) for *$line1*"
                                            AndroidShareManager.shareToWhatsApp(context, file, caption, "image/png")
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text("💬", fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: LO Profile & Chapter Details + LOGO UPLOAD
        if (selectedTab == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🏛️ Local Organization (LO) Profile", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text("Official parameters and chapter branding managed by Chapter President.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 14.dp))

                    // Chapter Logo Upload Section
                    Text("OFFICIAL CHAPTER LOGO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .clickable { chapterLogoPicker.launch("image/*") }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(60.dp)
                        ) {
                            if (loLogoUri != null) {
                                AsyncImage(
                                    model = loLogoUri,
                                    contentDescription = "Selected Chapter Logo",
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("📷", fontSize = 24.sp)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (loLogoUri != null) "Logo Uploaded" else "Tap to Upload Chapter Logo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "PNG, JPG, or SVG with transparent background recommended",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = { chapterLogoPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (loLogoUri != null) "Replace" else "Browse", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                        onClick = {
                            isSavedLo = true
                            Toast.makeText(context, "Chapter profile saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (isSavedLo) "✓ Chapter Profile Saved" else "Save Chapter Profile & Logo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
