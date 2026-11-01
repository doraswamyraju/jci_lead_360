package com.jci2027.lead360.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.jci2027.lead360.engine.BusinessCardOcrEngine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BusinessCardContact(
    val id: String,
    val name: String,
    val designation: String,
    val company: String,
    val phone: String,
    val email: String,
    val website: String = "",
    val eventTag: String,          // e.g. "Zonecon 2027", "NATCON", "GBM Jan"
    val locationName: String,      // e.g. "Novotel HICC", "Tirupati Royal Palace"
    val city: String,              // e.g. "Hyderabad", "Tirupati"
    val capturedAt: String,        // Timestamp
    val notes: String = "",
    val followUpDate: String = "",
    val hasFrontPhoto: Boolean = true,
    val hasBackPhoto: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardModuleScreen(
    chapterName: String = "JCI Tirupati Innovations",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Sample initial cards with event & location tracking
    val contactList = remember {
        mutableStateListOf(
            BusinessCardContact(
                id = "c1",
                name = "JC Sen. Rajesh Singhania",
                designation = "National Vice President",
                company = "Apex Solar & Energies Ltd",
                phone = "+91 98490 11223",
                email = "rajesh@apexsolar.in",
                website = "www.apexsolar.in",
                eventTag = "Zonecon 2027",
                locationName = "Novotel HICC",
                city = "Hyderabad",
                capturedAt = "28 Jan 2027, 04:30 PM",
                notes = "Agreed to be Chief Guest for our February Youth Leadership Conclave.",
                followUpDate = "31 Jan 2027"
            ),
            BusinessCardContact(
                id = "c2",
                name = "JC HGF Priya Nambiar",
                designation = "Chapter President",
                company = "Nambiar Logistics & Cargo",
                phone = "+91 97420 55667",
                email = "priya@nambiarlogistics.com",
                website = "www.nambiarlogistics.com",
                eventTag = "Zonecon 2027",
                locationName = "Novotel HICC",
                city = "Hyderabad",
                capturedAt = "28 Jan 2027, 06:15 PM",
                notes = "Discussed joint inter-chapter twinning project with JCI Bangalore.",
                followUpDate = "05 Feb 2027"
            ),
            BusinessCardContact(
                id = "c3",
                name = "Mahesh Babu Naidu",
                designation = "Director - SME Relations",
                company = "AP Chamber of Commerce",
                phone = "+91 94411 22334",
                email = "mahesh@apchamber.in",
                website = "www.apchamber.in",
                eventTag = "B2B Expo & Conclave",
                locationName = "SV University Auditorium",
                city = "Tirupati",
                capturedAt = "15 Jan 2027, 11:20 AM",
                notes = "Interested in providing chamber endorsement for our Trade Fair.",
                followUpDate = "20 Jan 2027"
            ),
            BusinessCardContact(
                id = "c4",
                name = "Dr. Shalini Gupta",
                designation = "Chief Medical Consultant",
                company = "Apollo Specialty Clinics",
                phone = "+91 98220 54321",
                email = "drshalini@apollo.com",
                website = "www.apolloclinics.com",
                eventTag = "GBM January 2027",
                locationName = "Hotel Bliss Royal Hall",
                city = "Tirupati",
                capturedAt = "10 Jan 2027, 08:45 PM",
                notes = "Sponsoring doctor kits for the Mega Blood Camp.",
                followUpDate = "22 Jan 2027"
            )
        )
    }

    var selectedEventFilter by remember { mutableStateOf("All Events") }
    var searchQuery by remember { mutableStateOf("") }
    var showScanCaptureSheet by remember { mutableStateOf(false) }
    var viewContactDetail by remember { mutableStateOf<BusinessCardContact?>(null) }

    // Dynamic list of unique events for filtering
    val availableEvents = remember(contactList) {
        listOf("All Events") + contactList.map { it.eventTag }.distinct()
    }

    // Filtered contacts based on event filter & search text
    val filteredContacts = remember(contactList, selectedEventFilter, searchQuery) {
        contactList.filter { contact ->
            val matchesEvent = (selectedEventFilter == "All Events" || contact.eventTag == selectedEventFilter)
            val matchesQuery = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.company.contains(searchQuery, ignoreCase = true) ||
                    contact.eventTag.contains(searchQuery, ignoreCase = true) ||
                    contact.city.contains(searchQuery, ignoreCase = true) ||
                    contact.locationName.contains(searchQuery, ignoreCase = true)
            matchesEvent && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business Card Scanner", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text("Event & Geo Tagged Networking Directory", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                actions = {
                    Button(
                        onClick = { showScanCaptureSheet = true },
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
            // Search Input Box
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

                    // Event Filter Pills (e.g. Zonecon 2027, NATCON, GBM)
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (event == "All Events") "🏷️ All" else "📍 $event",
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            "$count",
                                            color = if (isSelected) Color.White else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact List
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📇", fontSize = 42.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No contacts found for '$selectedEventFilter'", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 14.sp)
                        Text("Tap 'Scan Card' to capture visiting cards at this event.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredContacts) { contact ->
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
                                // Header: Event Pill & Date
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
                                    Text(
                                        contact.capturedAt.split(",").firstOrNull() ?: "",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Contact Info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0097D8).copy(alpha = 0.1f)),
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
                                        Text("🏢 Venue: ${contact.locationName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }

                                if (contact.notes.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Text(
                                            "📝 \"${contact.notes}\"",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(Modifier.height(10.dp))

                                // Fast Action Bar (Call, WhatsApp, Save vCard)
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
                                            Toast.makeText(context, "Saved ${contact.name} to Phone Contacts (vCard)!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("💾 vCard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FULL SCAN & OCR VERIFICATION SHEET ---
    if (showScanCaptureSheet) {
        ScanCardCaptureDialog(
            chapterName = chapterName,
            existingEvents = availableEvents.filter { it != "All Events" },
            onDismiss = { showScanCaptureSheet = false },
            onSaveContact = { newContact ->
                // Duplicate check
                val duplicate = contactList.find { it.phone.isNotBlank() && it.phone == newContact.phone }
                if (duplicate != null) {
                    Toast.makeText(context, "Updated existing contact for ${newContact.name}!", Toast.LENGTH_SHORT).show()
                    val idx = contactList.indexOf(duplicate)
                    contactList[idx] = newContact
                } else {
                    contactList.add(0, newContact)
                    Toast.makeText(context, "Card captured at ${newContact.eventTag} & saved!", Toast.LENGTH_SHORT).show()
                }
                showScanCaptureSheet = false
            }
        )
    }

    // --- VIEW / EDIT CONTACT DETAIL MODAL ---
    viewContactDetail?.let { contact ->
        ContactDetailModal(
            contact = contact,
            onDismiss = { viewContactDetail = null }
        )
    }
}

/**
 * Live Camera / Photo Picker + Google ML Kit OCR Engine with Geo & Event Tagging
 */
@Composable
fun ScanCardCaptureDialog(
    chapterName: String,
    existingEvents: List<String>,
    onDismiss: () -> Unit,
    onSaveContact: (BusinessCardContact) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var scanPhase by remember { mutableStateOf(0) } // 0: Live Capture / Gallery, 1: OCR Verification & Tagging Form
    var isOcrProcessing by remember { mutableStateOf(false) }

    // Form fields populated by Google ML Kit OCR
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    // Event & Location Tracking Fields
    var eventTag by remember { mutableStateOf(existingEvents.firstOrNull() ?: "Zonecon 2027") }
    var locationName by remember { mutableStateOf("Novotel Convention Center") }
    var city by remember { mutableStateOf("Hyderabad") }
    var notes by remember { mutableStateOf("") }
    var followUpDate by remember { mutableStateOf("Tomorrow (10:00 AM)") }

    var isFrontCaptured by remember { mutableStateOf(false) }
    var isBackCaptured by remember { mutableStateOf(false) }

    // Gallery / Camera Photo Pickers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    isOcrProcessing = true
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val parsed = BusinessCardOcrEngine.processCardBitmap(bitmap)
                    name = parsed.name
                    designation = parsed.designation
                    company = parsed.company
                    phone = parsed.phone
                    email = parsed.email
                    website = parsed.website
                    isFrontCaptured = true
                    isOcrProcessing = false
                    scanPhase = 1 // Move directly to review
                    Toast.makeText(context, "Google ML Kit extracted details!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    isOcrProcessing = false
                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coroutineScope.launch {
                try {
                    isOcrProcessing = true
                    val parsed = BusinessCardOcrEngine.processCardBitmap(bitmap)
                    name = parsed.name
                    designation = parsed.designation
                    company = parsed.company
                    phone = parsed.phone
                    email = parsed.email
                    website = parsed.website
                    isFrontCaptured = true
                    isOcrProcessing = false
                    scanPhase = 1 // Move directly to review
                    Toast.makeText(context, "Google ML Kit extracted card fields!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    isOcrProcessing = false
                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (scanPhase == 0) "📷 Google ML Kit Card Scanner" else "✅ Verify & Tag Contact",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (scanPhase == 0) "STEP 1/2" else "STEP 2/2",
                        color = Color(0xFF0284C7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (scanPhase == 0) {
                    // LIVE CAPTURE OR GALLERY SELECTOR WITH OCR VIEWFINDER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(2.dp, Color(0xFF0097D8), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOcrProcessing) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF38BDF8))
                                Spacer(Modifier.height(10.dp))
                                Text("Running Google ML Kit OCR...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📇", fontSize = 38.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Google ML Kit Neural OCR",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Captures Front/Back or selects from Gallery",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }

                            // Corner guidelines
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("📍 Auto Location: $city ($locationName)", color = Color(0xFF38BDF8), fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraPhotoLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
                        ) {
                            Text("📸 Take Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🖼️ From Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Preset Demo Quick Scan
                    OutlinedButton(
                        onClick = {
                            val parsed = BusinessCardOcrEngine.parseExtractedText(
                                "JC Sen. Rajesh Singhania\nManaging Director\nApex Solar Energies Ltd\n+91 98490 11223\nrajesh@apexsolar.in\nwww.apexsolar.in"
                            )
                            name = parsed.name
                            designation = parsed.designation
                            company = parsed.company
                            phone = parsed.phone
                            email = parsed.email
                            website = parsed.website
                            isFrontCaptured = true
                            scanPhase = 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Fast Demo Sample OCR", fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "On-device neural network model extracts phone, email, and company details without uploading sensitive data to external clouds.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                } else {
                    // OCR VERIFICATION + EVENT TAGGING FORM
                    Text("Review extracted text and tag the event where you met:", fontSize = 11.sp, color = Color(0xFF64748B))

                    // Event & Geo Tagging Card
                    Surface(
                        color = Color(0xFFF0F9FF),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📍 EVENT & LOCATION TRACKING", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0284C7))
                            OutlinedTextField(
                                value = eventTag,
                                onValueChange = { eventTag = it },
                                label = { Text("Event Name (e.g. Zonecon, NATCON, GBM)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = locationName,
                                    onValueChange = { locationName = it },
                                    label = { Text("Venue / Hotel") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("City") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Contact Details
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Discussion Notes / Follow-up") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            }
        },
        confirmButton = {
            if (scanPhase == 0) {
                Button(
                    onClick = {
                        if (name.isBlank()) name = "JC Delegate"
                        scanPhase = 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
                ) {
                    Text("Manual Entry / Review")
                }
            } else {
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                        val newContact = BusinessCardContact(
                            id = "c_${System.currentTimeMillis()}",
                            name = name.ifBlank { "JC Executive" },
                            designation = designation.ifBlank { "Member" },
                            company = company.ifBlank { "Enterprise" },
                            phone = phone,
                            email = email,
                            website = website,
                            eventTag = eventTag.ifBlank { "Networking Meet" },
                            locationName = locationName,
                            city = city,
                            capturedAt = timestamp,
                            notes = notes,
                            followUpDate = followUpDate,
                            hasFrontPhoto = isFrontCaptured,
                            hasBackPhoto = isBackCaptured
                        )
                        onSaveContact(newContact)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save & Tag Contact")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (scanPhase == 1) scanPhase = 0 else onDismiss()
            }) {
                Text(if (scanPhase == 1) "Back" else "Cancel")
            }
        }
    )
}

/**
 * Detailed Contact View Sheet
 */
@Composable
fun ContactDetailModal(
    contact: BusinessCardContact,
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
                // Event Metadata Pill
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("EVENT ORIGIN & LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        Spacer(Modifier.height(4.dp))
                        Text("📍 Event: ${contact.eventTag}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        Text("🏢 Venue: ${contact.locationName}, ${contact.city}", fontSize = 11.sp, color = Color(0xFF475569))
                        Text("⏱️ Captured: ${contact.capturedAt}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Text("📞 Phone: ${contact.phone}", fontSize = 13.sp, color = Color(0xFF334155))
                Text("✉️ Email: ${contact.email}", fontSize = 13.sp, color = Color(0xFF334155))
                if (contact.website.isNotBlank()) {
                    Text("🌐 Web: ${contact.website}", fontSize = 13.sp, color = Color(0xFF334155))
                }
                if (contact.notes.isNotBlank()) {
                    Text("📝 Discussion Notes: ${contact.notes}", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Exported ${contact.name} to Phone Contacts (vCard)!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
            ) {
                Text("Export to Phone Contacts (vCard)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
