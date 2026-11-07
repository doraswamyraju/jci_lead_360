package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterBottomSheet(
    token: String? = null,
    onDismiss: () -> Unit,
    onNavigateToContact: ((contactId: String?) -> Unit)? = null
) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch live backend notifications + fallback to local contacts exchange check if online list is empty
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            try {
                withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://lead.sriddha.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val service = retrofit.create(ApiService::class.java)
                    val res = service.getNotifications("Bearer $token")
                    var loadedNotifs = if (res.isSuccessful && res.body()?.success == true) {
                        res.body()?.notifications.orEmpty()
                    } else emptyList()

                    // Also fetch contacts to ensure any recent QR reciprocal exchanges generate an instant notification card
                    val contactsRes = service.getContacts("Bearer $token")
                    if (contactsRes.isSuccessful && contactsRes.body()?.success == true) {
                        val backendContacts = contactsRes.body()?.contacts.orEmpty()
                        val exchangeContacts = backendContacts.filter { 
                            it.eventTag.contains("Exchange", ignoreCase = true) || it.id.startsWith("c_recip_") 
                        }
                        
                        val syntheticNotifs = exchangeContacts.map { c ->
                            NotificationItem(
                                id = "notif_contact_${c.id}",
                                title = "📇 Exchanged: ${c.name}",
                                message = "${c.name} (${c.designation.ifBlank { "Visitor" }} at ${c.company.ifBlank { c.city.ifBlank { "JCI Event" } }}) scanned your QR code and exchanged contacts.",
                                type = "contact_exchange",
                                contactId = c.id,
                                createdAt = c.followUpDate.ifBlank { "Just now" }
                            )
                        }

                        // Combine without duplicates
                        val existingIds = loadedNotifs.map { it.id }.toSet()
                        val combined = loadedNotifs.toMutableList()
                        syntheticNotifs.forEach { syn ->
                            if (!existingIds.contains(syn.id) && !combined.any { it.contactId == syn.contactId }) {
                                combined.add(0, syn)
                            }
                        }
                        loadedNotifs = combined
                    }

                    withContext(Dispatchers.Main) {
                        notifications = loadedNotifs
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        } else {
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔔", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Notification Center",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Real-time alerts, orders & contact exchanges",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    val count = notifications.count { !it.read }
                    Text(
                        text = if (count > 0) "$count NEW" else "UPDATED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0097D8))
                }
            } else if (notifications.isEmpty()) {
                // Static Default Fallbacks
                val defaultAlerts = listOf(
                    Triple("⭐ 100% Efficiency Notice", "1st LOM General Body Meeting flyer template has been published by Super Admin. Generate and share with your chapter members.", "2h ago"),
                    Triple("📅 Compliance Deadline", "Action Plan 2027 and annual chapter budget must be submitted before 15th January 2027 to claim maximum points.", "1d ago"),
                    Triple("🎉 Platform Live Alert", "Hostinger isolated production port 5087 active. 300 DPI canvas compositor online.", "2d ago")
                )

                defaultAlerts.forEach { (title, desc, time) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0097D8)
                                )
                                Text(
                                    text = time,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                notifications.forEach { notif ->
                    val isExchange = notif.type == "contact_exchange" || notif.title.contains("Contact", ignoreCase = true)
                    val targetContactId = notif.contactId ?: notif.data?.get("contactId")?.toString()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isExchange && onNavigateToContact != null) {
                                    onDismiss()
                                    onNavigateToContact(targetContactId)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExchange) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                        ),
                        border = if (isExchange) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = notif.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExchange) Color(0xFF15803D) else Color(0xFF0097D8)
                                    )
                                }
                                Text(
                                    text = if (!notif.read) "● NEW" else "",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                            if (isExchange) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "➔ Tap to view contact in Rolodex",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .padding(top = 8.dp)
            ) {
                Text("Mark All as Read", fontWeight = FontWeight.Bold)
            }
        }
    }
}

