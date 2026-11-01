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
import java.io.File
import java.util.concurrent.Executors

enum class ScannerStep {
    ENTRY,          // 1. Entry Screen
    SCAN_FRONT,     // 2. Scan Front Side (Live Camera Viewfinder)
    SCAN_BACK,      // 3. Scan Back Side (Live Camera Viewfinder)
    EXTRACT_REVIEW, // 4. Extract & Review Form
    SAVED_SUCCESS   // 5. Saved Successfully
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
    var confidence: String = "HIGH",
    var savedToLead360: Boolean = true,
    var savedToPhoneContacts: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardModuleScreen(
    chapterName: String = "JCI Tirupati Innovations",
    token: String? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(ScannerStep.ENTRY) }
    var cardModel by remember { mutableStateOf(ScannedCardModel()) }
    
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

    // Fetch live backend contacts (including contacts saved via reciprocal QR portal)
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            try {
                withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://lead.sriddha.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val service = retrofit.create(ApiService::class.java)
                    val response = service.getContacts("Bearer $token")
                    if (response.isSuccessful && response.body()?.success == true) {
                        val backendList = response.body()?.contacts.orEmpty()
                        withContext(Dispatchers.Main) {
                            backendList.forEach { c ->
                                val exists = contactList.any { 
                                    it.phone.filter { ch -> ch.isDigit() } == c.phone.filter { ch -> ch.isDigit() } ||
                                    it.name.equals(c.name, ignoreCase = true)
                                }
                                if (!exists) {
                                    contactList.add(
                                        0,
                                        ScannedCardModel(
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
                                            confidence = "HIGH"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep local contacts on network issue
            }
        }
    }
    var selectedEventFilter by remember { mutableStateOf("All Events") }
    var searchQuery by remember { mutableStateOf("") }
    var viewContactDetail by remember { mutableStateOf<ScannedCardModel?>(null) }

    // Check & request Camera Permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            currentStep = ScannerStep.SCAN_FRONT
        } else {
            Toast.makeText(context, "Camera permission is required to scan cards", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val parsed = BusinessCardOcrEngine.processCardBitmap(bitmap)
                    cardModel = cardModel.copy(
                        name = parsed.name,
                        designation = parsed.designation,
                        company = parsed.company,
                        phone = parsed.phone,
                        email = parsed.email,
                        website = parsed.website,
                        address = parsed.address,
                        socialProfiles = parsed.socialProfiles.toMutableList(),
                        frontBitmap = bitmap,
                        frontRawText = parsed.rawText,
                        confidence = parsed.confidence
                    )
                    currentStep = ScannerStep.EXTRACT_REVIEW
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentStep) {
                ScannerStep.ENTRY -> {
                    // SCREEN 1: DIRECTORY SCREEN with Event Filters, Search, and 1-tap "Scan Card" button
                    ScannerDirectoryScreen(
                        chapterName = chapterName,
                        contacts = contactList,
                        selectedEventFilter = selectedEventFilter,
                        onEventFilterSelected = { selectedEventFilter = it },
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { searchQuery = it },
                        onScanClick = {
                            cardModel = ScannedCardModel()
                            if (hasCameraPermission) {
                                currentStep = ScannerStep.SCAN_FRONT
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGalleryClick = {
                            cardModel = ScannedCardModel()
                            galleryPickerLauncher.launch("image/*")
                        },
                        onContactClick = { viewContactDetail = it },
                        onBack = onBack
                    )
                }
                ScannerStep.SCAN_FRONT -> {
                    // SCREEN 2: SCAN FRONT SIDE (Live CameraX Viewfinder with Card Boundary & Auto-Snap)
                    LiveCardScannerView(
                        stepTitle = "Front Side (1 of 2)",
                        guideHint = "Capture the front side of the business card.",
                        activeStepNumber = 1,
                        onImageCaptured = { bitmap, ocrText ->
                            cardModel.frontBitmap = bitmap
                            cardModel.frontRawText = ocrText
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
                        onBack = { currentStep = ScannerStep.ENTRY }
                    )
                }
                ScannerStep.SCAN_BACK -> {
                    // SCREEN 3: SCAN BACK SIDE (Optional 2nd side capture)
                    LiveCardScannerView(
                        stepTitle = "Back Side (2 of 2)",
                        guideHint = "Capture the back side of the business card.",
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
                    // SCREEN 4: EXTRACT & REVIEW SCREEN
                    ExtractAndReviewScreen(
                        cardModel = cardModel,
                        existingContacts = contactList.map { Triple(it.name, it.phone, it.email) },
                        onRescan = {
                            cardModel = ScannedCardModel()
                            currentStep = ScannerStep.SCAN_FRONT
                        },
                        onSave = { updatedCard ->
                            cardModel = updatedCard

                            // CardSnap Deduplication
                            val (isDuplicate, reason) = CardSnapDeduplicator.checkDuplicate(
                                updatedCard.name,
                                updatedCard.phone,
                                updatedCard.email,
                                updatedCard.company,
                                contactList.map { Triple(it.name, it.phone, it.email) }
                            )

                            if (isDuplicate) {
                                val idx = contactList.indexOfFirst {
                                    it.phone.filter { c -> c.isDigit() } == updatedCard.phone.filter { c -> c.isDigit() } ||
                                            it.name.equals(updatedCard.name, ignoreCase = true)
                                }
                                if (idx >= 0) {
                                    contactList[idx] = updatedCard
                                } else {
                                    contactList.add(0, updatedCard)
                                }
                            } else {
                                contactList.add(0, updatedCard)
                            }

                            // Save to Phone Contacts if enabled
                            if (updatedCard.savedToPhoneContacts) {
                                saveCardToPhoneContacts(context, updatedCard)
                            }

                            currentStep = ScannerStep.SAVED_SUCCESS
                        },
                        onBack = { currentStep = ScannerStep.SCAN_FRONT }
                    )
                }
                ScannerStep.SAVED_SUCCESS -> {
                    // SCREEN 5: SAVED SUCCESSFULLY
                    ContactSavedSuccessScreen(
                        cardModel = cardModel,
                        onViewContact = {
                            currentStep = ScannerStep.ENTRY
                        },
                        onScanAnother = {
                            cardModel = ScannedCardModel()
                            currentStep = ScannerStep.SCAN_FRONT
                        }
                    )
                }
            }
        }
    }

    // Modal Contact Detail
    viewContactDetail?.let { contact ->
        ContactDetailPopup(
            contact = contact,
            onDismiss = { viewContactDetail = null }
        )
    }
}

// =========================================================================
// SCREEN 1: DIRECTORY SCREEN (Search, Event Filters, Contact List, 1-Tap Scan)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerDirectoryScreen(
    chapterName: String,
    contacts: List<ScannedCardModel>,
    selectedEventFilter: String,
    onEventFilterSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onScanClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onContactClick: (ScannedCardModel) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Dynamic list of unique events for filtering
    val availableEvents = remember(contacts) {
        listOf("All Events") + contacts.map { it.eventTag }.distinct()
    }

    // Filtered contacts based on event filter & search text
    val filteredContacts = remember(contacts, selectedEventFilter, searchQuery) {
        contacts.filter { contact ->
            val matchesEvent = (selectedEventFilter == "All Events" || contact.eventTag == selectedEventFilter)
            val matchesQuery = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.company.contains(searchQuery, ignoreCase = true) ||
                    contact.eventTag.contains(searchQuery, ignoreCase = true) ||
                    contact.city.contains(searchQuery, ignoreCase = true)
            matchesEvent && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business Card Scanner", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text("CardSnap Engine • LO Networking Rolodex", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                actions = {
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("📷 Scan Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        onValueChange = onSearchQueryChanged,
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

                    // Event Filter Pills (e.g. Zonecon 2027, NATCON, GBM)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableEvents) { event ->
                            val isSelected = selectedEventFilter == event
                            val count = if (event == "All Events") contacts.size else contacts.count { it.eventTag == event }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF0097D8) else Color(0xFFF1F5F9),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable { onEventFilterSelected(event) }
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

            // Quick Gallery Import Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SAVED VISITING CARDS (${filteredContacts.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                TextButton(
                    onClick = onGalleryClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("🖼️ Import from Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
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
                        Text("No contacts found for '$selectedEventFilter'", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap 'Scan Card' to capture visiting cards with CameraX.", fontSize = 12.sp, color = Color(0xFF94A3B8))
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
                    items(filteredContacts) { contact ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactClick(contact) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header: Event Tag Pill & Confidence
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

                                    Surface(
                                        color = if (contact.confidence == "HIGH") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            contact.confidence,
                                            color = if (contact.confidence == "HIGH") Color(0xFF15803D) else Color(0xFFB45309),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

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

                                // Fast Action Bar (Call, WhatsApp, Save to Device Contacts)
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
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=Hi%20${contact.name},%20pleasure%20meeting%20you%20at%20${contact.eventTag}!"))
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
                                        Text("💾 Save", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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

/**
 * Detailed Contact Popup View
 */
@Composable
fun ContactDetailPopup(
    contact: ScannedCardModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                Text("${contact.designation} • ${contact.company}", fontSize = 12.sp, color = Color(0xFF0097D8))
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
                        Text("🏢 City: ${contact.city}", fontSize = 11.sp, color = Color(0xFF475569))
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

@Composable
fun FeatureValueRow(iconEmoji: String, iconBg: Color, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(iconEmoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B), lineHeight = 16.sp)
        }
    }
}

// =========================================================================
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
            putExtra(ContactsContract.Intents.Insert.NOTES, "JCI LEAD360 Card Scanner. Event: ${card.eventTag} (${card.city})")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Saved locally: ${card.name}", Toast.LENGTH_SHORT).show()
    }
}
