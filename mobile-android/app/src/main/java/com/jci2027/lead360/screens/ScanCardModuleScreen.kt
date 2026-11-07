package com.jci2027.lead360.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.SaveContactRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ScannedCardModel(
    val id: String = "c_${System.currentTimeMillis()}",
    var name: String = "",
    var designation: String = "",
    var company: String = "",
    var phone: String = "",
    var email: String = "",
    var website: String = "",
    var address: String = "",
    var eventTag: String = "Zonecon 2027",
    var city: String = "Tirupati",
    var notes: String = "",
    var confidence: String = "HIGH",
    var savedToPhoneContacts: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardModuleScreen(
    chapterName: String = "JCI Tirupati Innovations",
    token: String? = null,
    initialTargetContactId: String? = null,
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

    // Fetch live backend contacts
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
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
            } catch (e: Exception) {
                // Keep local contacts on error
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
