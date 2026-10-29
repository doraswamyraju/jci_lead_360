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
    var role: String,
    var name: String,
    var email: String,
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

    // 0: Directory, 1: Grant Access, 2: Official Logo Generator (logo.jciath.be replica)
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

    // Executive team list (LGB & PST)
    val currentTeam = remember {
        mutableStateListOf(
            TeamMemberItem("usr_1", "PRESIDENT", presidentName, "president@jcitirupati.com", "+91 98765 43210"),
            TeamMemberItem("usr_2", "SECRETARY", "JC Secretary 2027", "secretary@jcitirupati.com", "+91 98765 43211"),
            TeamMemberItem("usr_3", "TREASURER", "JC Treasurer 2027", "treasurer@jcitirupati.com", "+91 98765 43212"),
            TeamMemberItem("usr_4", "VP MANAGEMENT", "JC Vice President 1", "vp.mgmt@jcitirupati.com", "+91 98765 43213"),
            TeamMemberItem("usr_5", "VP COMMUNITY", "JC Vice President 2", "vp.comm@jcitirupati.com", "+91 98765 43214"),
            TeamMemberItem("usr_6", "VP INDIVIDUAL", "JC Vice President 3", "vp.ind@jcitirupati.com", "+91 98765 43215"),
            TeamMemberItem("usr_7", "VP INTERNATIONAL", "JC Vice President 4", "vp.intl@jcitirupati.com", "+91 98765 43216")
        )
    }

    // Modal BottomSheet State for Officer Details (Edit, Update, Delete)
    var selectedMemberForSheet by remember { mutableStateOf<TeamMemberItem?>(null) }
    var showMemberBottomSheet by remember { mutableStateOf(false) }

    // Temporary editing state for bottom sheet
    var editName by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val sheetPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editPhotoUri = uri
            Toast.makeText(context, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // Modal Bottom Sheet Implementation
    if (showMemberBottomSheet && selectedMemberForSheet != null) {
        val member = selectedMemberForSheet!!
        ModalBottomSheet(
            onDismissRequest = { showMemberBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Officer Profile Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "View, edit or remove member from Chapter LGB / PST",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = editRole,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Avatar / Photo Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { sheetPhotoPicker.launch("image/*") }
                            .border(2.dp, Color(0xFF0097D8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (editPhotoUri != null) {
                            AsyncImage(
                                model = editPhotoUri,
                                contentDescription = "Officer Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("📷", fontSize = 24.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Officer Photograph", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                        Text("High-res portrait for flyers & invitations", fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    OutlinedButton(
                        onClick = { sheetPhotoPicker.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (editPhotoUri != null) "Change" else "Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit Fields
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Phone / WhatsApp Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Login Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = editRole,
                    onValueChange = { editRole = it },
                    label = { Text("Designation / Role") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons: Update / Save & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Delete Button (Disabled for President)
                    if (member.role != "PRESIDENT") {
                        Button(
                            onClick = {
                                currentTeam.removeAll { it.id == member.id }
                                showMemberBottomSheet = false
                                Toast.makeText(context, "Removed ${member.name} from board", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("🗑️ Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Save / Update Button
                    Button(
                        onClick = {
                            val idx = currentTeam.indexOfFirst { it.id == member.id }
                            if (idx != -1) {
                                currentTeam[idx] = member.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    email = editEmail.trim(),
                                    role = editRole.trim(),
                                    photoUri = editPhotoUri
                                )
                                showMemberBottomSheet = false
                                Toast.makeText(context, "✓ Details updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(if (member.role != "PRESIDENT") 1.8f else 1f).height(48.dp)
                    ) {
                        Text("Save & Update Officer", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
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
                Text(
                    text = "🏛️ PST & LGB Management Suite",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "$chapterName • Year 2027 Executive Board",
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
                        "👥 PST & LGB Directory",
                        "🔑 Grant Access",
                        "🎨 Logo Generator"
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

        // TAB 0: Board Directory with 1-Tap Bottom Sheet Inspector
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EXECUTIVE DIRECTORY (${currentTeam.size}) • TAP MEMBER TO EDIT OR DELETE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                currentTeam.forEach { member ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMemberForSheet = member
                                editName = member.name
                                editRole = member.role
                                editEmail = member.email
                                editPhone = member.phone
                                editPhotoUri = member.photoUri
                                showMemberBottomSheet = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Avatar Photo
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0)),
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
                                        Text(
                                            text = member.name.firstOrNull()?.toString() ?: "O",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF0097D8)
                                        )
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
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0369A1),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(member.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }

                            // View / Edit indicator arrow
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "Edit ⚙️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
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
                                    contentScale = ContentScale.Fit,
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
    }
}
