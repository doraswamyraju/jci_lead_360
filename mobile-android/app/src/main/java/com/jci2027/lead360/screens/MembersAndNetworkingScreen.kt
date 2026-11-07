package com.jci2027.lead360.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.jci2027.lead360.engine.QrCodeGenerator
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.MemberDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersAndNetworkingScreen(
    chapterName: String,
    token: String? = null,
    onBack: () -> Unit = {},
    onNavigateToScanCard: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Directory, 1: Pending Approvals, 2: Map View
    var showQrDialog by remember { mutableStateOf(false) }
    var viewingMember by remember { mutableStateOf<MemberDto?>(null) }
    var editingMember by remember { mutableStateOf<MemberDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<MemberDto?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }

    val membersList = remember { mutableStateListOf<MemberDto>() }
    val pendingQueue = remember { mutableStateListOf<MemberDto>() }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://lead.sriddha.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(ApiService::class.java) }

    suspend fun loadMembersFromBackend() {
        if (token.isNullOrBlank()) return
        try {
            withContext(Dispatchers.IO) {
                val res = apiService.getMembers("Bearer $token")
                if (res.isSuccessful && res.body()?.success == true) {
                    val all = res.body()?.members.orEmpty()
                    withContext(Dispatchers.Main) {
                        membersList.clear()
                        pendingQueue.clear()
                        all.forEach { m ->
                            if (m.status.equals("Pending", ignoreCase = true)) {
                                pendingQueue.add(m)
                            } else {
                                membersList.add(m)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(token) {
        loadMembersFromBackend()
    }

    fun deleteMemberAction(member: MemberDto) {
        membersList.removeAll { it.id == member.id }
        pendingQueue.removeAll { it.id == member.id }
        if (viewingMember?.id == member.id) viewingMember = null
        if (!token.isNullOrBlank()) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        apiService.deleteMember("Bearer $token", member.id)
                    }
                } catch (_: Exception) {}
            }
        }
        Toast.makeText(context, "${member.name} removed permanently", Toast.LENGTH_SHORT).show()
    }

    fun approveMemberAction(applicant: MemberDto) {
        pendingQueue.removeAll { it.id == applicant.id }
        val approved = applicant.copy(status = "Active", role = "Member")
        membersList.add(0, approved)
        if (viewingMember?.id == applicant.id) viewingMember = approved
        if (!token.isNullOrBlank()) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        apiService.approveMember("Bearer $token", applicant.id)
                    }
                } catch (_: Exception) {}
            }
        }
        Toast.makeText(context, "${applicant.name} approved & activated!", Toast.LENGTH_SHORT).show()
    }

    fun saveMemberAction(member: MemberDto, isEdit: Boolean) {
        if (isEdit) {
            val idx = membersList.indexOfFirst { it.id == member.id }
            if (idx >= 0) membersList[idx] = member
            if (viewingMember?.id == member.id) viewingMember = member
        } else {
            membersList.add(0, member)
        }
        if (!token.isNullOrBlank()) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        if (isEdit) {
                            apiService.updateMember("Bearer $token", member.id, member)
                        } else {
                            apiService.saveMember("Bearer $token", member)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        Toast.makeText(context, "Saved member details", Toast.LENGTH_SHORT).show()
    }

    // Dynamic categories for filtering
    val categories = remember(membersList.size) {
        listOf("All Categories") + membersList.mapNotNull { it.businessCategory }.filter { it.isNotBlank() }.distinct()
    }

    val filteredMembers = remember(membersList.toList(), searchQuery, selectedCategoryFilter) {
        membersList.filter { m ->
            val matchesCat = selectedCategoryFilter == "All Categories" || m.businessCategory?.equals(selectedCategoryFilter, ignoreCase = true) == true
            val matchesSearch = searchQuery.isBlank() ||
                    m.name.contains(searchQuery, ignoreCase = true) ||
                    m.businessName?.contains(searchQuery, ignoreCase = true) == true ||
                    m.city?.contains(searchQuery, ignoreCase = true) == true ||
                    m.role?.contains(searchQuery, ignoreCase = true) == true
            matchesCat && matchesSearch
        }
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.clickable { showQrDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📲", fontSize = 13.sp)
                            Text("Member Reg QR", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
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
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0097D8)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("👥 Directory (${membersList.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⏳ Approvals (${pendingQueue.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🗺️ Map View", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: Chapter Directory
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search & Add Bar
                        Surface(color = Color.White, shadowElevation = 1.dp) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search by name, company, trade, or city...", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("MEMBERS (${filteredMembers.size})", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))

                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add Member", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (filteredMembers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👥", fontSize = 42.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No members found in directory", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 14.sp)
                                    Text("Tap '+ Add Member' or share the Registration QR.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredMembers, key = { it.id }) { member ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(1.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewingMember = member }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (!member.photoUrl.isNullOrBlank()) {
                                                    val pUrl = if (member.photoUrl.startsWith("http")) member.photoUrl else "https://lead.sriddha.com${member.photoUrl}"
                                                    AsyncImage(
                                                        model = pUrl,
                                                        contentDescription = member.name,
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .clip(CircleShape)
                                                            .border(1.5.dp, Color(0xFF0097D8), CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE0F2FE)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(member.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF0097D8), fontSize = 16.sp)
                                                    }
                                                }

                                                Spacer(Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                                        if (!member.bloodGroup.isNullOrBlank()) {
                                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEE2E2)) {
                                                                Text("🩸 ${member.bloodGroup}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                            }
                                                        }
                                                    }

                                                    Text(member.role ?: "Member", fontSize = 12.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.SemiBold)
                                                    Spacer(Modifier.height(3.dp))
                                                    Text("💼 ${member.businessName} • ${member.businessCategory}", fontSize = 11.sp, color = Color(0xFF475569))
                                                    Text("📞 ${member.phone}  |  📍 ${member.city}", fontSize = 11.sp, color = Color(0xFF64748B))
                                                }
                                            }

                                            Spacer(Modifier.height(10.dp))
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { viewingMember = member }) {
                                                    Text("👁️ Full Profile Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(onClick = { editingMember = member }, modifier = Modifier.size(32.dp)) {
                                                        Text("✏️", fontSize = 14.sp)
                                                    }
                                                    IconButton(onClick = { memberToDelete = member }, modifier = Modifier.size(32.dp)) {
                                                        Text("🗑️", fontSize = 14.sp)
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
                1 -> {
                    // TAB 1: Pending Approvals Queue
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "APPLICANTS PENDING PRESIDENT APPROVAL (${pendingQueue.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64748B)
                        )

                        if (pendingQueue.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✅", fontSize = 42.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No pending applications", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 14.sp)
                                    Text("All member registrations are active and cleared.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(pendingQueue, key = { it.id }) { applicant ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
                                        elevation = CardDefaults.cardElevation(2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(applicant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                                    Text("Applied for: ${applicant.chapterName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                                }
                                                Surface(
                                                    color = Color(0xFFFFF7ED),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                                                ) {
                                                    Text("⏳ Pending Review", color = Color(0xFFC2410C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }

                                            Spacer(Modifier.height(8.dp))
                                            Text("💼 ${applicant.businessName} • ${applicant.designation} (${applicant.businessCategory})", fontSize = 12.sp, color = Color(0xFF334155))
                                            Text("📞 ${applicant.phone}  |  ✉️ ${applicant.email}", fontSize = 12.sp, color = Color(0xFF64748B))
                                            if (!applicant.bloodGroup.isNullOrBlank()) {
                                                Text("🩸 Blood Group: ${applicant.bloodGroup}", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                                            }

                                            Spacer(Modifier.height(14.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedButton(
                                                    onClick = { deleteMemberAction(applicant) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { approveMemberAction(applicant) },
                                                    modifier = Modifier.weight(1.5f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("✓ Approve & Activate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: Interactive Map View of Members
                    InteractiveMembersMapView(
                        members = membersList,
                        onMemberClick = { viewingMember = it }
                    )
                }
            }
        }
    }

    // Professional Full Profile Preview Dialog
    viewingMember?.let { member ->
        ProfessionalMemberPreviewDialog(
            member = member,
            onDismiss = { viewingMember = null },
            onEdit = {
                editingMember = member
                viewingMember = null
            },
            onDelete = {
                memberToDelete = member
                viewingMember = null
            }
        )
    }

    // Edit Member Dialog
    editingMember?.let { member ->
        MemberFormDialog(
            title = "Edit Member Details",
            initial = member,
            onDismiss = { editingMember = null },
            onConfirm = { updated ->
                saveMemberAction(updated, isEdit = true)
                editingMember = null
            }
        )
    }

    // Add Member Dialog
    if (showAddDialog) {
        MemberFormDialog(
            title = "Add Chapter Member",
            initial = MemberDto(
                id = "mem_${System.currentTimeMillis()}",
                name = "",
                phone = "",
                chapterName = chapterName
            ),
            onDismiss = { showAddDialog = false },
            onConfirm = { newMem ->
                saveMemberAction(newMem, isEdit = false)
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Delete Member Record", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently remove '${member.name}' from chapter records? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteMemberAction(member)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Member Registration QR Code Dialog
    if (showQrDialog) {
        val joinUrl = "https://lead.sriddha.com/join/chap_tirupati_01"
        val qrBitmap = remember { QrCodeGenerator.generateQrBitmap(joinUrl, 500) }

        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📲 Official Member Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(4.dp))
                    Text("Scan with any phone to open the application portal", fontSize = 11.sp, color = Color(0xFF64748B), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(16.dp))

                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Registration QR",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(joinUrl, fontSize = 11.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Join $chapterName: $joinUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Registration Link"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Share Link via WhatsApp")
                    }

                    TextButton(onClick = { showQrDialog = false }) {
                        Text("Close", color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

/**
 * Interactive Map-Based View of Chapter Members
 */
@Composable
fun InteractiveMembersMapView(
    members: List<MemberDto>,
    onMemberClick: (MemberDto) -> Unit
) {
    val context = LocalContext.current
    var selectedMemberByPin by remember { mutableStateOf<MemberDto?>(members.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map Canvas Box (Interactive stylised geography canvas)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1D32)),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background map grid texture
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color(0x330097D8),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0097D8))
                        ) {
                            Text("📍 JCI Chapter Regional Network Map", color = Color(0xFFBAE6FD), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Text("Total: ${members.size} Hubs", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }

                    // Interactive location pins row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        members.take(5).forEachIndexed { idx, m ->
                            val isSel = selectedMemberByPin?.id == m.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedMemberByPin = m }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isSel) 44.dp else 34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) Color(0xFF0097D8) else Color(0xFF1E293B))
                                        .border(2.dp, if (isSel) Color.White else Color(0xFF38BDF8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(m.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isSel) 14.sp else 11.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(m.city?.take(8) ?: "Hub", color = Color(0xFFCBD5E1), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Text("Tap any hub to inspect business pin", color = Color(0xFF64748B), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Active Pin Details Card
        selectedMemberByPin?.let { pinMember ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pinMember.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Text("${pinMember.designation} • ${pinMember.businessName}", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE0F2FE)) {
                            Text("📍 ${pinMember.city}", color = Color(0xFF0369A1), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("🏢 Trade: ${pinMember.businessCategory}", fontSize = 12.sp, color = Color(0xFF475569))
                    if (!pinMember.businessAddress.isNullOrBlank()) {
                        Text("📍 Address: ${pinMember.businessAddress}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val mapUri = Uri.parse("geo:0,0?q=${Uri.encode("${pinMember.businessName}, ${pinMember.city}")}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🗺️ Open in Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onMemberClick(pinMember) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Full Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-End Executive Full Member Profile Preview
 */
@Composable
fun ProfessionalMemberPreviewDialog(
    member: MemberDto,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFF091322))
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 20.dp, y = 35.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color(0xFF0097D8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!member.photoUrl.isNullOrBlank()) {
                            val pUrl = if (member.photoUrl.startsWith("http")) member.photoUrl else "https://lead.sriddha.com${member.photoUrl}"
                            AsyncImage(
                                model = pUrl,
                                contentDescription = member.name,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(member.name.take(2).uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF0097D8))
                        }
                    }
                }

                Spacer(Modifier.height(42.dp))

                // Name & Titles
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
                            Text("${member.role} • ${member.chapterName}", fontSize = 12.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.Bold)
                        }
                        if (!member.bloodGroup.isNullOrBlank()) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                                Text("🩸 ${member.bloodGroup}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 1-Tap Quick Action Row (WhatsApp, Call, Email, Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cleanPhone = member.phone.replace("+", "").replace(" ", "").replace("-", "")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=Hi%20${member.name},%20connecting%20via%20JCI%20LEAD360!"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("💬 WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📞 Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = ContactsContract.Contacts.CONTENT_TYPE
                                    putExtra(ContactsContract.Intents.Insert.NAME, member.name)
                                    putExtra(ContactsContract.Intents.Insert.COMPANY, member.businessName)
                                    putExtra(ContactsContract.Intents.Insert.JOB_TITLE, member.designation)
                                    putExtra(ContactsContract.Intents.Insert.PHONE, member.phone)
                                    putExtra(ContactsContract.Intents.Insert.EMAIL, member.email)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("💾 Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(Modifier.height(12.dp))

                    // JCI Track Record
                    Text("JCI MEMBERSHIP TRACK RECORD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                    Spacer(Modifier.height(6.dp))
                    Text("• Membership Category: ${member.membershipType ?: "Active Member"}", fontSize = 12.sp, color = Color(0xFF334155))
                    Text("• Induction / Joining Year: ${member.joinYear ?: "2027"}", fontSize = 12.sp, color = Color(0xFF334155))
                    Text("• Zone Jurisdiction: ${member.zone ?: "Zone XXVI"}", fontSize = 12.sp, color = Color(0xFF334155))

                    Spacer(Modifier.height(14.dp))

                    // Business Directory
                    Text("BUSINESS & PROFESSIONAL SHOWCASE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                    Spacer(Modifier.height(6.dp))
                    Text("🏢 Firm: ${member.businessName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("💼 Role: ${member.designation} (${member.businessCategory})", fontSize = 12.sp, color = Color(0xFF0284C7))
                    if (!member.city.isNullOrBlank()) Text("📍 City: ${member.city}", fontSize = 12.sp, color = Color(0xFF475569))
                    if (!member.website.isNullOrBlank()) Text("🌐 Web: ${member.website}", fontSize = 12.sp, color = Color(0xFF0284C7))
                    if (!member.services.isNullOrBlank()) Text("⚡ Services: ${member.services}", fontSize = 12.sp, color = Color(0xFF334155))

                    Spacer(Modifier.height(20.dp))

                    // Edit & Delete Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDelete) {
                            Text("🗑️ Delete Record", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✏️ Edit Details")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Add / Edit Member Modal Dialog
 */
@Composable
fun MemberFormDialog(
    title: String,
    initial: MemberDto,
    onDismiss: () -> Unit,
    onConfirm: (MemberDto) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var phone by remember { mutableStateOf(initial.phone) }
    var email by remember { mutableStateOf(initial.email ?: "") }
    var role by remember { mutableStateOf(initial.role ?: "Member") }
    var bloodGroup by remember { mutableStateOf(initial.bloodGroup ?: "O+") }
    var businessName by remember { mutableStateOf(initial.businessName ?: "") }
    var designation by remember { mutableStateOf(initial.designation ?: "") }
    var businessCategory by remember { mutableStateOf(initial.businessCategory ?: "General") }
    var city by remember { mutableStateOf(initial.city ?: "Tirupati") }
    var website by remember { mutableStateOf(initial.website ?: "") }
    var services by remember { mutableStateOf(initial.services ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("JCI Role / Title (e.g. VP Training, Member)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group (e.g. O+, A+, B+)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Company / Firm Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation in Firm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = businessCategory,
                        onValueChange = { businessCategory = it },
                        label = { Text("Trade Category (e.g. Solar, IT, Construction)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = services,
                        onValueChange = { services = it },
                        label = { Text("Services Offered") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Website") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) return@Button
                    val updated = initial.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        role = role.trim().ifBlank { "Member" },
                        bloodGroup = bloodGroup.trim(),
                        businessName = businessName.trim(),
                        designation = designation.trim(),
                        businessCategory = businessCategory.trim(),
                        city = city.trim(),
                        services = services.trim(),
                        website = website.trim()
                    )
                    onConfirm(updated)
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
            ) {
                Text("Save Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
