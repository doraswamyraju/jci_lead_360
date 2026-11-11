package com.jci2027.lead360.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.jci2027.lead360.engine.BusinessCardOcrEngine
import com.jci2027.lead360.engine.CardSnapDeduplicator
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.SaveContactRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

enum class ScannerStep {
    DIRECTORY,
    SCAN_FRONT,
    SCAN_BACK,
    EXTRACT_REVIEW,
    SAVED_SUCCESS
}

data class ScannedCardModel(
    val id: String = "c_${System.currentTimeMillis()}",
    var name: String = "",
    var designation: String = "",
    var company: String = "",
    var phone: String = "",
    var email: String = "",
    var website: String = "",
    var address: String = "",
    var socialProfiles: MutableList<String> = mutableListOf(),
    var frontBitmap: Bitmap? = null,
    var backBitmap: Bitmap? = null,
    var frontRawText: String = "",
    var backRawText: String = "",
    var eventTag: String = "Zonecon 2027",
    var city: String = "Tirupati",
    var notes: String = "",
    var confidence: String = "HIGH",
    var savedToLead360: Boolean = true,
    var savedToPhoneContacts: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardModuleScreen(
    chapterName: String = "JCI Tirupati Innovations",
    token: String? = null,
    initialTargetContactId: String? = null,
    startWithScanner: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://lead.sriddha.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(ApiService::class.java) }

    var currentStep by remember { mutableStateOf(if (startWithScanner) ScannerStep.SCAN_FRONT else ScannerStep.DIRECTORY) }
    var cardModel by remember { mutableStateOf(ScannedCardModel()) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            currentStep = ScannerStep.SCAN_FRONT
        } else {
            Toast.makeText(context, "Camera permission needed to scan business cards", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    }
                    val parsed = BusinessCardOcrEngine.processCardBitmap(bitmap)
                    cardModel = ScannedCardModel(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        confidence = parsed.confidence,
                        frontBitmap = bitmap,
                        frontRawText = parsed.rawText
                    )
                    currentStep = ScannerStep.EXTRACT_REVIEW
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load card image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Existing saved contacts for directory
    val contactList = remember {
        mutableStateListOf(
            ScannedCardModel(
                id = "c1",
                name = "JC Sen. Rajesh Singhania",
                designation = "National Vice President",
                company = "Apex Solar & Energies Ltd",
                phone = "+91 98490 11223",
                email = "rajesh@apexsolar.in",
                website = "www.apexsolar.in",
                address = "Jubilee Hills, Hyderabad",
                eventTag = "Zonecon 2027",
                city = "Hyderabad",
                confidence = "HIGH"
            ),
            ScannedCardModel(
                id = "c2",
                name = "JC HGF Priya Nambiar",
                designation = "Chapter President",
                company = "Nambiar Logistics & Cargo",
                phone = "+91 97420 55667",
                email = "priya@nambiarlogistics.com",
                website = "www.nambiarlogistics.com",
                address = "Whitefield, Bangalore",
                eventTag = "Zonecon 2027",
                city = "Hyderabad",
                confidence = "HIGH"
            ),
            ScannedCardModel(
                id = "c3",
                name = "Mahesh Babu Naidu",
                designation = "Director - SME Relations",
                company = "AP Chamber of Commerce",
                phone = "+91 94411 22334",
                email = "mahesh@apchamber.in",
                website = "www.apchamber.in",
                address = "Tirupati Chamber Hub",
                eventTag = "B2B Expo & Conclave",
                city = "Tirupati",
                confidence = "HIGH"
            ),
            ScannedCardModel(
                id = "c4",
                name = "Dr. Shalini Gupta",
                designation = "Chief Medical Consultant",
                company = "Apollo Specialty Clinics",
                phone = "+91 98220 54321",
                email = "drshalini@apollo.com",
                website = "www.apolloclinics.com",
                address = "Renigunta Road, Tirupati",
                eventTag = "GBM January 2027",
                city = "Tirupati",
                confidence = "HIGH"
            )
        )
    }

    var selectedEventFilter by remember { mutableStateOf("All Events") }
    var searchQuery by remember { mutableStateOf("") }
    var viewContactDetail by remember { mutableStateOf<ScannedCardModel?>(null) }
    var contactToEdit by remember { mutableStateOf<ScannedCardModel?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<ScannedCardModel?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }

    suspend fun refreshContactsFromBackend() {
        if (token.isNullOrBlank()) return
        try {
            withContext(Dispatchers.IO) {
                val response = apiService.getContacts("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val backendList = response.body()?.contacts.orEmpty()
                    withContext(Dispatchers.Main) {
                        backendList.forEach { c ->
                            val existingIndex = contactList.indexOfFirst { it.id == c.id }
                            val model = ScannedCardModel(
                                id = c.id,
                                name = c.name,
                                designation = c.designation,
                                company = c.company,
                                phone = c.phone,
                                email = c.email,
                                website = c.website,
                                address = c.address,
                                eventTag = c.eventTag.ifBlank { "QR Contact Exchange" },
                                city = c.city,
                                notes = c.notes,
                                confidence = "HIGH"
                            )
                            if (existingIndex >= 0) {
                                contactList[existingIndex] = model
                            } else {
                                val dupPhone = contactList.any {
                                    it.phone.filter { ch -> ch.isDigit() } == c.phone.filter { ch -> ch.isDigit() }
                                }
                                if (!dupPhone) {
                                    contactList.add(0, model)
                                }
                            }
                        }

                        // If an initial contact ID was passed (e.g. from notification click), open it directly
                        if (!initialTargetContactId.isNullOrBlank()) {
                            contactList.find { it.id == initialTargetContactId }?.let { target ->
                                viewContactDetail = target
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Auto-poll contacts periodically (every 5 seconds) so newly exchanged contacts appear live without restarting screen
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            while (true) {
                refreshContactsFromBackend()
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    // Direct check if targetContactId is already present locally
    LaunchedEffect(initialTargetContactId) {
        if (!initialTargetContactId.isNullOrBlank()) {
            contactList.find { it.id == initialTargetContactId }?.let { target ->
                viewContactDetail = target
            }
        }
    }

    // Delete Contact Function
    fun performDeleteContact(contact: ScannedCardModel) {
        contactList.removeAll { it.id == contact.id }
        if (viewContactDetail?.id == contact.id) {
            viewContactDetail = null
        }
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.deleteContact(contact.id)
                }
            } catch (e: Exception) {
                // silent fallback
            }
        }
        Toast.makeText(context, "Contact '${contact.name}' deleted", Toast.LENGTH_SHORT).show()
    }

    // Save Contact Function (Create or Update)
    fun performSaveContact(contact: ScannedCardModel, isEdit: Boolean) {
        if (isEdit) {
            val idx = contactList.indexOfFirst { it.id == contact.id }
            if (idx >= 0) {
                contactList[idx] = contact
            }
            if (viewContactDetail?.id == contact.id) {
                viewContactDetail = contact
            }
            Toast.makeText(context, "Contact '${contact.name}' updated", Toast.LENGTH_SHORT).show()
        } else {
            contactList.add(0, contact)
            Toast.makeText(context, "Contact '${contact.name}' added", Toast.LENGTH_SHORT).show()
        }

        // Sync with backend API
        if (!token.isNullOrBlank()) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val req = SaveContactRequest(
                            id = contact.id,
                            name = contact.name,
                            company = contact.company,
                            designation = contact.designation,
                            phone = contact.phone,
                            email = contact.email,
                            website = contact.website,
                            address = contact.address,
                            eventTag = contact.eventTag,
                            locationName = contact.city,
                            city = contact.city,
                            notes = contact.notes,
                            followUpDate = ""
                        )
                        if (isEdit) {
                            apiService.updateContact("Bearer $token", contact.id, req)
                        } else {
                            apiService.saveContact("Bearer $token", req)
                        }
                    }
                } catch (e: Exception) {
                    // silent fallback
                }
            }
        }
    }

    // Dynamic list of unique events for filtering
    val availableEvents = remember(contactList.size, selectedEventFilter) {
        listOf("All Events") + contactList.map { it.eventTag }.distinct()
    }

    // Filtered contacts based on event filter & search text
    val filteredContacts = remember(contactList.toList(), selectedEventFilter, searchQuery) {
        contactList.filter { contact ->
            val matchesEvent = (selectedEventFilter == "All Events" || contact.eventTag == selectedEventFilter)
            val matchesQuery = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.company.contains(searchQuery, ignoreCase = true) ||
                    contact.eventTag.contains(searchQuery, ignoreCase = true) ||
                    contact.city.contains(searchQuery, ignoreCase = true)
            matchesEvent && matchesQuery
        }
    }

    when (currentStep) {
        ScannerStep.DIRECTORY -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Exchange & Cards", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                                Text("LO Rolodex • Manage All Contacts", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        refreshContactsFromBackend()
                                        isRefreshing = false
                                        Toast.makeText(context, "Directory synced", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("🔄", fontSize = 16.sp)
                            }
                            // 📸 CardSnap Business Card Camera Scanner
                            Button(
                                onClick = {
                                    cardModel = ScannedCardModel()
                                    if (hasCameraPermission) {
                                        currentStep = ScannerStep.SCAN_FRONT
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("📸", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text("Scan Card", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(Modifier.width(4.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // Search Input Box & Event Filter Bar
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, company, event, or city...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0097D8),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Event Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableEvents) { event ->
                            val isSelected = selectedEventFilter == event
                            val count = if (event == "All Events") contactList.size else contactList.count { it.eventTag == event }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF0097D8) else Color(0xFFF1F5F9),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable { selectedEventFilter = event }
                            ) {
                                Text(
                                    text = "$event ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL CONTACTS (${filteredContacts.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                TextButton(
                    onClick = { showCreateDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+ New Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
                }
            }

            // Contacts List
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📇", fontSize = 42.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No contacts found", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap '+ Add Contact' to create a new card in the Rolodex.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredContacts, key = { it.id }) { contact ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewContactDetail = contact }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header: Event Tag Pill & Quick Edit/Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("📍 ${contact.eventTag}", color = Color(0xFF0284C7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            if (contact.city.isNotBlank()) {
                                                Text(" • ${contact.city}", color = Color(0xFF0369A1), fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { contactToEdit = contact },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0097D8), modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { contactToDelete = contact },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Contact Info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0097D8).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            contact.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0097D8),
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                        Text("${contact.designation} • ${contact.company}", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(2.dp))
                                        Text("📞 ${contact.phone}  |  ✉️ ${contact.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(Modifier.height(10.dp))

                                // Fast Action Bar (Call, WhatsApp, Save to Device)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("📞 Call", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            val cleanPhone = contact.phone.replace("+", "").replace(" ", "").replace("-", "")
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=Hi%20${contact.name},%20pleasure%20connecting%20with%20you%20via%20JCI%20LEAD360!"))
                                            context.startActivity(waIntent)
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("💬 WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            saveCardToPhoneContacts(context, contact)
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("💾 Phone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Contact Detail Popup (Read)
    viewContactDetail?.let { contact ->
        ContactDetailPopup(
            contact = contact,
            onDismiss = { viewContactDetail = null },
            onEdit = {
                contactToEdit = contact
                viewContactDetail = null
            },
            onDelete = {
                contactToDelete = contact
                viewContactDetail = null
            }
        )
    }

    // Create New Contact Modal (Create)
    if (showCreateDialog) {
        ContactFormDialog(
            title = "Add New Contact",
            confirmButtonText = "Create Contact",
            initialContact = ScannedCardModel(),
            onDismiss = { showCreateDialog = false },
            onConfirm = { newContact ->
                performSaveContact(newContact, isEdit = false)
                showCreateDialog = false
            }
        )
    }

    // Edit Contact Modal (Update)
    contactToEdit?.let { existingContact ->
        ContactFormDialog(
            title = "Edit Contact Details",
            confirmButtonText = "Save Changes",
            initialContact = existingContact,
            onDismiss = { contactToEdit = null },
            onConfirm = { updatedContact ->
                performSaveContact(updatedContact, isEdit = true)
                contactToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog (Delete)
    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = { Text("Delete Contact", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${contact.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        performDeleteContact(contact)
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
        } // End of ScannerStep.DIRECTORY
        ScannerStep.SCAN_FRONT -> {
            LiveCardScannerView(
                stepTitle = "Front Side (1 of 2)",
                guideHint = "Position the front side of the business card within the frame.",
                activeStepNumber = 1,
                onImageCaptured = { bitmap, ocrText ->
                    cardModel.frontBitmap = bitmap
                    cardModel.frontRawText = ocrText
                    val parsed = BusinessCardOcrEngine.parseExtractedText(ocrText)
                    cardModel = cardModel.copy(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        confidence = parsed.confidence
                    )
                    currentStep = ScannerStep.SCAN_BACK
                },
                onSkipBackSide = { bitmap, ocrText ->
                    cardModel.frontBitmap = bitmap
                    cardModel.frontRawText = ocrText
                    val parsed = BusinessCardOcrEngine.parseExtractedText(ocrText)
                    cardModel = cardModel.copy(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        confidence = parsed.confidence
                    )
                    currentStep = ScannerStep.EXTRACT_REVIEW
                },
                onPickGallery = { galleryPickerLauncher.launch("image/*") },
                onBack = { currentStep = ScannerStep.DIRECTORY }
            )
        }
        ScannerStep.SCAN_BACK -> {
            LiveCardScannerView(
                stepTitle = "Back Side (2 of 2)",
                guideHint = "Capture the back side of the business card (optional).",
                activeStepNumber = 2,
                onImageCaptured = { bitmap, ocrText ->
                    cardModel.backBitmap = bitmap
                    cardModel.backRawText = ocrText
                    val parsed = BusinessCardOcrEngine.mergeAndParse(cardModel.frontRawText, ocrText)
                    cardModel = cardModel.copy(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        confidence = parsed.confidence
                    )
                    currentStep = ScannerStep.EXTRACT_REVIEW
                },
                onSkipBackSide = { _, _ ->
                    val parsed = BusinessCardOcrEngine.parseExtractedText(cardModel.frontRawText)
                    cardModel = cardModel.copy(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        confidence = parsed.confidence
                    )
                    currentStep = ScannerStep.EXTRACT_REVIEW
                },
                onPickGallery = { galleryPickerLauncher.launch("image/*") },
                onBack = { currentStep = ScannerStep.SCAN_FRONT }
            )
        }
        ScannerStep.EXTRACT_REVIEW -> {
            ExtractAndReviewScreen(
                cardModel = cardModel,
                existingContacts = contactList.map { Triple(it.name, it.phone, it.email) },
                onRescan = {
                    cardModel = ScannedCardModel()
                    currentStep = ScannerStep.SCAN_FRONT
                },
                onSave = { updatedCard ->
                    cardModel = updatedCard
                    performSaveContact(updatedCard, isEdit = false)
                    if (updatedCard.savedToPhoneContacts) {
                        saveCardToPhoneContacts(context, updatedCard)
                    }
                    currentStep = ScannerStep.SAVED_SUCCESS
                },
                onBack = { currentStep = ScannerStep.SCAN_FRONT }
            )
        }
        ScannerStep.SAVED_SUCCESS -> {
            ContactSavedSuccessScreen(
                cardModel = cardModel,
                onViewContact = {
                    viewContactDetail = cardModel
                    currentStep = ScannerStep.DIRECTORY
                },
                onScanAnother = {
                    cardModel = ScannedCardModel()
                    currentStep = ScannerStep.SCAN_FRONT
                }
            )
        }
    }
}

/**
 * Detailed Contact Popup View (Read) with Edit and Delete actions
 */
@Composable
fun ContactDetailPopup(
    contact: ScannedCardModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                    Text("${contact.designation} • ${contact.company}", fontSize = 12.sp, color = Color(0xFF0097D8))
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0097D8))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("EVENT ORIGIN & LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        Spacer(Modifier.height(4.dp))
                        Text("📍 Event: ${contact.eventTag}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        if (contact.city.isNotBlank()) {
                            Text("🏢 City: ${contact.city}", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }
                }

                Text("📞 Phone: ${contact.phone}", fontSize = 13.sp, color = Color(0xFF334155))
                Text("✉️ Email: ${contact.email}", fontSize = 13.sp, color = Color(0xFF334155))
                if (contact.address.isNotBlank()) {
                    Text("📍 Address: ${contact.address}", fontSize = 13.sp, color = Color(0xFF334155))
                }
                if (contact.website.isNotBlank()) {
                    Text("🌐 Web: ${contact.website}", fontSize = 13.sp, color = Color(0xFF334155))
                }
                if (contact.notes.isNotBlank()) {
                    Text("📝 Notes: ${contact.notes}", fontSize = 13.sp, color = Color(0xFF334155))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    saveCardToPhoneContacts(context, contact)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
            ) {
                Text("Save to Device Contacts")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Dialog for creating or editing contacts (Form Dialog)
 */
@Composable
fun ContactFormDialog(
    title: String,
    confirmButtonText: String,
    initialContact: ScannedCardModel,
    onDismiss: () -> Unit,
    onConfirm: (ScannedCardModel) -> Unit
) {
    var name by remember { mutableStateOf(initialContact.name) }
    var designation by remember { mutableStateOf(initialContact.designation) }
    var company by remember { mutableStateOf(initialContact.company) }
    var phone by remember { mutableStateOf(initialContact.phone) }
    var email by remember { mutableStateOf(initialContact.email) }
    var city by remember { mutableStateOf(initialContact.city) }
    var address by remember { mutableStateOf(initialContact.address) }
    var website by remember { mutableStateOf(initialContact.website) }
    var eventTag by remember { mutableStateOf(initialContact.eventTag) }
    var notes by remember { mutableStateOf(initialContact.notes) }

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
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company / Chapter") },
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
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = eventTag,
                        onValueChange = { eventTag = it },
                        label = { Text("Event / Tag (e.g. Zonecon 2027)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
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
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        return@Button
                    }
                    val updated = initialContact.copy(
                        name = name.trim(),
                        designation = designation.trim(),
                        company = company.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        city = city.trim(),
                        address = address.trim(),
                        website = website.trim(),
                        eventTag = eventTag.trim().ifBlank { "Networking" },
                        notes = notes.trim()
                    )
                    onConfirm(updated)
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Android Native Contacts Contractor
 */
fun saveCardToPhoneContacts(context: Context, card: ScannedCardModel) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, card.name)
            putExtra(ContactsContract.Intents.Insert.COMPANY, card.company)
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, card.designation)
            if (card.email.isNotBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, card.email)
            if (card.phone.isNotBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, card.phone)
            if (card.address.isNotBlank()) putExtra(ContactsContract.Intents.Insert.POSTAL, card.address)
            putExtra(ContactsContract.Intents.Insert.NOTES, "JCI LEAD360. Event: ${card.eventTag} (${card.city}). Notes: ${card.notes}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Saved locally: ${card.name}", Toast.LENGTH_SHORT).show()
    }
}
// SCREEN 2 & 3: REAL-TIME CAMERAX VIEWFINDER WITH AUTO-DETECT (Reference Design 2 & 3)
// =========================================================================
@Composable
fun LiveCardScannerView(
    stepTitle: String,
    guideHint: String,
    activeStepNumber: Int,
    onImageCaptured: (Bitmap, String) -> Unit,
    onSkipBackSide: (Bitmap, String) -> Unit,
    onPickGallery: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Stepper header + live camera box
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Live Camera Preview Feed
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    // Image Analysis for real-time text/card detection
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        BusinessCardOcrEngine.processImageProxy(imageProxy) { detectedText ->
                            // Continuous detection can trigger auto-snap if rich text found
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Header & 3-Step Stepper Indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Scan Business Card", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                IconButton(onClick = {
                    isTorchOn = !isTorchOn
                    cameraControl?.enableTorch(isTorchOn)
                }) {
                    Text(if (isTorchOn) "⚡" else "💡", fontSize = 20.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Step Indicator (1) Front Side ➔ (2) Back Side ➔ (3) Review
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCircleIndicator(number = 1, title = "Front Side", isActive = activeStepNumber == 1, isCompleted = activeStepNumber > 1)
                StepDivider(isCompleted = activeStepNumber > 1)
                StepCircleIndicator(number = 2, title = "Back Side", isActive = activeStepNumber == 2, isCompleted = activeStepNumber > 2)
                StepDivider(isCompleted = false)
                StepCircleIndicator(number = 3, title = "Review", isActive = false, isCompleted = false)
            }
        }

        // 3. Center Business Card Viewfinder Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.65f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.5.dp, Color.White, RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
            ) {
                // Corner targeting accents
                Box(Modifier.size(24.dp).align(Alignment.TopStart).border(3.dp, Color(0xFF0097D8), RoundedCornerShape(topStart = 16.dp)))
                Box(Modifier.size(24.dp).align(Alignment.TopEnd).border(3.dp, Color(0xFF0097D8), RoundedCornerShape(topEnd = 16.dp)))
                Box(Modifier.size(24.dp).align(Alignment.BottomStart).border(3.dp, Color(0xFF0097D8), RoundedCornerShape(bottomStart = 16.dp)))
                Box(Modifier.size(24.dp).align(Alignment.BottomEnd).border(3.dp, Color(0xFF0097D8), RoundedCornerShape(bottomEnd = 16.dp)))
            }
        }

        // 4. Bottom Controls, Hints, and Shutter
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Pill (e.g. Front Side 1 of 2)
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    stepTitle,
                    color = Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Guidance toast pill
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    guideHint,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Shutter Row (Gallery, Big White Shutter Button, Switch/Skip)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gallery button
                IconButton(
                    onClick = onPickGallery,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Text("🖼️", fontSize = 20.sp)
                }

                // Shutter Capture Button (Triggers ML Kit OCR)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(if (isProcessing) Color(0xFF0097D8) else Color.White)
                        .border(3.dp, Color.Black, CircleShape)
                        .clickable(enabled = !isProcessing) {
                            val capture = imageCapture ?: return@clickable
                            isProcessing = true
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = image.toBitmap()
                                        image.close()

                                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                            val parsed = BusinessCardOcrEngine.processCardBitmap(bitmap)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                onImageCaptured(bitmap, parsed.rawText)
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isProcessing = false
                                        Toast.makeText(context, "Capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    } else {
                        Box(Modifier.size(60.dp).clip(CircleShape).background(Color.White).border(2.dp, Color(0xFF0097D8), CircleShape))
                    }
                }

                // Skip / Direct review button (if back not needed)
                TextButton(
                    onClick = {
                        val emptyBitmap = Bitmap.createBitmap(100, 60, Bitmap.Config.ARGB_8888)
                        onSkipBackSide(emptyBitmap, "")
                    }
                ) {
                    Text("Skip >", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun StepCircleIndicator(number: Int, title: String, isActive: Boolean, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF10B981)
                        isActive -> Color(0xFF0097D8)
                        else -> Color.White.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text("$number", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            color = if (isActive || isCompleted) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(2.dp)
            .background(if (isCompleted) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f))
    )
}

// =========================================================================
// SCREEN 4: EXTRACT & REVIEW SCREEN (Matching Reference Design 4)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractAndReviewScreen(
    cardModel: ScannedCardModel,
    existingContacts: List<Triple<String, String, String>>,
    onRescan: () -> Unit,
    onSave: (ScannedCardModel) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(cardModel.name) }
    var designation by remember { mutableStateOf(cardModel.designation) }
    var company by remember { mutableStateOf(cardModel.company) }
    var phone by remember { mutableStateOf(cardModel.phone) }
    var email by remember { mutableStateOf(cardModel.email) }
    var website by remember { mutableStateOf(cardModel.website) }
    var address by remember { mutableStateOf(cardModel.address) }
    var socials = remember { mutableStateListOf<String>().apply { addAll(cardModel.socialProfiles) } }

    var saveToLead360 by remember { mutableStateOf(cardModel.savedToLead360) }
    var saveToPhoneContacts by remember { mutableStateOf(cardModel.savedToPhoneContacts) }

    // Run CardSnap Deduplication
    val (isDup, dupReason) = remember(name, phone, email) {
        CardSnapDeduplicator.checkDuplicate(name, phone, email, company, existingContacts)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Contact", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRescan,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0097D8))
                        Spacer(Modifier.width(6.dp))
                        Text("Rescan", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val updated = cardModel.copy(
                                name = name.ifBlank { "JC Executive" },
                                designation = designation,
                                company = company,
                                phone = phone,
                                email = email,
                                website = website,
                                address = address,
                                socialProfiles = socials.toMutableList(),
                                savedToLead360 = saveToLead360,
                                savedToPhoneContacts = saveToPhoneContacts
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Save Contact", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
            // Dual Card Thumbnails: [Front Side ✓] and [Back Side ✓]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardThumbnailBox(
                    bitmap = cardModel.frontBitmap,
                    label = "Front Side ✓",
                    modifier = Modifier.weight(1f)
                )
                CardThumbnailBox(
                    bitmap = cardModel.backBitmap,
                    label = if (cardModel.backBitmap != null) "Back Side ✓" else "Back Side (Skipped)",
                    modifier = Modifier.weight(1f)
                )
            }

            // Deduplication Alert Banner
            if (isDup && dupReason != null) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Deduplication Match: $dupReason. Saving will update the existing entry.", fontSize = 12.sp, color = Color(0xFFB45309), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Extracted Details Form Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨ Extracted Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Auto-filled (editable)", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0097D8)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Designation
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation") },
                        leadingIcon = { Text("💼", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Company / Organization
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company / Organization") },
                        leadingIcon = { Text("🏢", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Phone Number with + Add
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0097D8)) },
                        trailingIcon = {
                            TextButton(onClick = { Toast.makeText(context, "Secondary phone added", Toast.LENGTH_SHORT).show() }) {
                                Text("+ Add", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Email with + Add
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0097D8)) },
                        trailingIcon = {
                            TextButton(onClick = { Toast.makeText(context, "Secondary email added", Toast.LENGTH_SHORT).show() }) {
                                Text("+ Add", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Website
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Website") },
                        leadingIcon = { Text("🌐", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0097D8)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Social Profiles Chip Pills
                    Text("Social Profiles", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(socials) { soc ->
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(soc, fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp).clickable { socials.remove(soc) }
                                    )
                                }
                            }
                        }
                    }

                    TextButton(onClick = {
                        socials.add("linkedin.com/in/${name.lowercase().replace(" ", "")}")
                    }) {
                        Text("+ Add More Social Profile", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Save Destination Toggles
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Save Destinations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { saveToLead360 = !saveToLead360 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = saveToLead360, onCheckedChange = { saveToLead360 = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)))
                        Spacer(Modifier.width(8.dp))
                        Text("Save to LEAD360 Chapter Directory", fontSize = 13.sp, color = Color(0xFF334155))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { saveToPhoneContacts = !saveToPhoneContacts },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = saveToPhoneContacts, onCheckedChange = { saveToPhoneContacts = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)))
                        Spacer(Modifier.width(8.dp))
                        Text("Save to Native Phone Contacts", fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun CardThumbnailBox(bitmap: Bitmap?, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📇", fontSize = 24.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(label, color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

// =========================================================================
// SCREEN 5: SAVED SUCCESSFULLY SCREEN (Matching Reference Design 5)
// =========================================================================
@Composable
fun ContactSavedSuccessScreen(
    cardModel: ScannedCardModel,
    onViewContact: () -> Unit,
    onScanAnother: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(24.dp))

            // Animated Big Green Checkmark
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Contact Saved!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Spacer(Modifier.height(4.dp))
            Text(
                "${cardModel.name} has been added to your LEAD360 contacts.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Contact Summary Card with Initials Avatar & 5 Circular Actions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cardModel.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0097D8),
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(cardModel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Text("${cardModel.designation} • ${cardModel.company}", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(Modifier.height(16.dp))

                    // 5 Circular Instant Quick Actions: Call, Email, Web, Map, LinkedIn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionCircle(icon = Icons.Default.Phone, label = "Call") {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cardModel.phone}"))
                            context.startActivity(intent)
                        }
                        QuickActionCircle(icon = Icons.Default.Email, label = "Email") {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${cardModel.email}"))
                            context.startActivity(intent)
                        }
                        QuickActionCircle(iconEmoji = "🌐", label = "Web") {
                            val cleanWeb = if (cardModel.website.startsWith("http")) cardModel.website else "https://${cardModel.website}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanWeb))
                            context.startActivity(intent)
                        }
                        QuickActionCircle(icon = Icons.Default.LocationOn, label = "Map") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(cardModel.address)}"))
                            context.startActivity(intent)
                        }
                        QuickActionCircle(icon = Icons.Default.Share, label = "Share") {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Contact: ${cardModel.name} (${cardModel.company}) - Phone: ${cardModel.phone}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Dual Confirmation Badges
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗄️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Saved to LEAD360", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Saved to Phone Contacts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Bottom CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onViewContact,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("View Contact Directory", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onScanAnother,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0097D8))
            ) {
                Text("📷", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("Scan Another Card", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
            }
        }
    }
}

@Composable
fun QuickActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconEmoji: String? = null,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = label, tint = Color(0xFF0097D8), modifier = Modifier.size(20.dp))
            } else if (iconEmoji != null) {
                Text(iconEmoji, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

