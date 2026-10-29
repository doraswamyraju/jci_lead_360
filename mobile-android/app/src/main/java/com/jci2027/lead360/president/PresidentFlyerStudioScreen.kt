package com.jci2027.lead360.president

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.jci2027.lead360.engine.AndroidFlyerRenderer
import com.jci2027.lead360.model.TemplateModel
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresidentFlyerStudioScreen(
    token: String,
    presidentName: String,
    chapterName: String,
    chapterLogoBitmap: Bitmap? = null,
    presidentPhotoBitmap: Bitmap? = null,
    baseUrl: String = "https://lead.sriddha.com"
) {
    val context = LocalContext.current
    val flyerRenderer = remember { AndroidFlyerRenderer(context) }

    var templates by remember { mutableStateOf<List<TemplateModel>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<TemplateModel?>(null) }
    var isLoadingTemplates by remember { mutableStateOf(true) }
    var fetchErrorMessage by remember { mutableStateOf<String?>(null) }

    // Mode: 0: Template Explorer Grid, 1: Live Editor & Compositor
    var activeStudioTab by remember { mutableStateOf(0) }

    // Categories Filter
    val categories = listOf("All", "⭐ 100% Efficiency", "🏛️ GBM & Meetings", "🎓 Training", "🌍 Special Days", "🤝 Community")
    var selectedCategory by remember { mutableStateOf("All") }

    // Aspect Ratio Selector: 1:1 Square vs 4:5 Portrait Story
    var selectedAspectSquare by remember { mutableStateOf(true) }

    // Full-screen Zoom Modal State
    var showFullScreenZoom by remember { mutableStateOf(false) }

    val fieldValues = remember { mutableStateMapOf<String, String>() }
    var compositeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(false) }
    var cachedBaseBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Quick Venue Presets
    val venuePresets = listOf("Hotel Royal Grand, Main Hall", "Rotary Convention Center", "Chapter Secretariat", "Online Zoom Meeting")

    // Native Date & Time Dialog Helpers
    val calendarInstance = Calendar.getInstance()
    var selectedDateString by remember { mutableStateOf("15th Jan 2027") }
    var selectedTimeString by remember { mutableStateOf("6:30 PM") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val suffix = when (dayOfMonth) {
                1, 21, 31 -> "st"
                2, 22 -> "nd"
                3, 23 -> "rd"
                else -> "th"
            }
            selectedDateString = "$dayOfMonth$suffix ${months[month]} $year"
            val combined = "$selectedDateString • $selectedTimeString"
            fieldValues["event_date_time"] = combined
            selectedTemplate?.let { tpl ->
                triggerComposite(flyerRenderer, tpl, cachedBaseBitmap, chapterLogoBitmap, presidentPhotoBitmap, fieldValues) { bmp -> compositeBitmap = bmp }
            }
        },
        2027, 0, 15
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val displayMinute = String.format("%02d", minute)
            selectedTimeString = "$displayHour:$displayMinute $amPm"
            val combined = "$selectedDateString • $selectedTimeString"
            fieldValues["event_date_time"] = combined
            selectedTemplate?.let { tpl ->
                triggerComposite(flyerRenderer, tpl, cachedBaseBitmap, chapterLogoBitmap, presidentPhotoBitmap, fieldValues) { bmp -> compositeBitmap = bmp }
            }
        },
        18, 30, false
    )

    // Load templates from API
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                val client = OkHttpClient.Builder().addInterceptor(logging).build()
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiService::class.java)
                val response = api.getTemplates("Bearer $token")
                withContext(Dispatchers.Main) {
                    isLoadingTemplates = false
                    if (response.isSuccessful && response.body()?.success == true) {
                        val list = response.body()!!.templates
                        if (list.isNotEmpty()) {
                            templates = list
                            selectedTemplate = list.first()
                            initDefaultFieldValues(list.first(), presidentName, fieldValues)
                        } else {
                            fetchErrorMessage = "No flyer templates published yet by Super Admin."
                        }
                    } else {
                        fetchErrorMessage = "Server response: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingTemplates = false
                    fetchErrorMessage = "Connection error: ${e.localizedMessage ?: "Failed to reach server"}"
                }
            }
        }
    }

    // Load base image when selected template changes
    LaunchedEffect(selectedTemplate) {
        val tpl = selectedTemplate ?: return@LaunchedEffect
        val rawUrl = tpl.baseImageUrl
        val imageUrl = if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"

        isRendering = true
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(req) as? SuccessResult)?.drawable
                val bmp = (result as? BitmapDrawable)?.bitmap
                withContext(Dispatchers.Main) {
                    cachedBaseBitmap = bmp
                    if (bmp != null) {
                        triggerComposite(flyerRenderer, tpl, bmp, chapterLogoBitmap, presidentPhotoBitmap, fieldValues) { resultBmp ->
                            compositeBitmap = resultBmp
                            isRendering = false
                        }
                    } else {
                        isRendering = false
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isRendering = false
                }
            }
        }
    }

    if (isLoadingTemplates) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF0097D8))
                Spacer(modifier = Modifier.height(14.dp))
                Text("Loading official 2027 templates...", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }
        return
    }

    if (fetchErrorMessage != null) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(fetchErrorMessage!!, color = Color(0xFFDC2626), fontSize = 13.sp)
        }
        return
    }

    // Filter templates based on category chip
    val filteredTemplates = remember(templates, selectedCategory) {
        if (selectedCategory == "All") {
            templates
        } else {
            val key = selectedCategory.replace("⭐", "").replace("🏛️", "").replace("🎓", "").replace("🌍", "").replace("🤝", "").trim()
            templates.filter { it.category?.contains(key, ignoreCase = true) == true }
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

        // Top Studio Navigation Switcher (Catalogue vs Live Studio)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("✨ Live Studio & Editor", 0),
                    Pair("📂 Template Catalogue (${templates.size})", 1)
                ).forEach { (label, idx) ->
                    val isSel = activeStudioTab == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) Color(0xFF0097D8) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeStudioTab = idx }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(label, color = if (isSel) Color.White else Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // VIEW 1: Full Template Catalogue Grid
        if (activeStudioTab == 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SELECT ANY OFFICIAL TEMPLATE TO CUSTOMIZE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B)
                )

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isCatSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCatSelected) Color(0xFF0097D8) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCatSelected) Color(0xFF0097D8) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isCatSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isCatSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Grid of templates
                filteredTemplates.chunked(2).forEach { rowTemplates ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTemplates.forEach { tpl ->
                            val rawUrl = tpl.baseImageUrl
                            val imageUrl = if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"
                            val isSelected = tpl.id == selectedTemplate?.id

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTemplate = tpl
                                        initDefaultFieldValues(tpl, presidentName, fieldValues)
                                        activeStudioTab = 0
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isSelected) Color(0xFF0097D8) else Color(0xFFE2E8F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .background(Color(0xFF091322))
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = tpl.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Marked fields overlay on top of template creative
                                        tpl.layers?.let { layers ->
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                layers.forEach { layer ->
                                                    val zoneType = (layer.type ?: layer.key ?: "").lowercase()
                                                    val xPct = if (layer.xPercent > 0f) layer.xPercent else ((layer.x / 1080f) * 100f)
                                                    val yPct = if (layer.yPercent > 0f) layer.yPercent else ((layer.y / 1080f) * 100f)

                                                    val badgeText = when {
                                                        zoneType.contains("logo") -> "🏛️ Logo"
                                                        zoneType.contains("photo") -> "👤 Photo"
                                                        zoneType.contains("date") -> "📅 Date"
                                                        zoneType.contains("venue") -> "📍 Venue"
                                                        zoneType.contains("president") -> "👤 President"
                                                        else -> layer.label ?: "✏️ Text"
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xCC091322),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                                                        modifier = Modifier
                                                            .offset(
                                                                x = ((xPct / 100f) * 140).dp,
                                                                y = ((yPct / 100f) * 90).dp
                                                            )
                                                    ) {
                                                        Text(
                                                            text = badgeText,
                                                            fontSize = 7.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFBAE6FD),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(tpl.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, color = Color(0xFF0F172A))
                                        Text(tpl.category ?: "General", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Customize ⚡", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                        if (rowTemplates.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // VIEW 0: Live Studio & Compositor
        if (activeStudioTab == 0) {
            // 1. Live Composite Preview Viewport with Aspect Ratio Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aspect ratio chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedAspectSquare) Color(0xFF0097D8) else Color(0x22FFFFFF),
                                modifier = Modifier.clickable { selectedAspectSquare = true }
                            ) {
                                Text("1:1 Square", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!selectedAspectSquare) Color(0xFF0097D8) else Color(0x22FFFFFF),
                                modifier = Modifier.clickable { selectedAspectSquare = false }
                            ) {
                                Text("4:5 Story", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = selectedTemplate?.title ?: "Select Template",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1
                                )
                            }

                            // Full screen & Zoom trigger button
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0097D8),
                                modifier = Modifier.clickable { showFullScreenZoom = true }
                            ) {
                                Text(
                                    text = "🔍 Zoom / Fullscreen",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Composite Visual Canvas (Clickable to open Fullscreen Zoom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (selectedAspectSquare) 1f else 0.8f)
                            .background(Color(0xFF040810), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .clickable {
                                if (compositeBitmap != null) {
                                    showFullScreenZoom = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRendering) {
                            CircularProgressIndicator(color = Color(0xFF0097D8))
                        } else if (compositeBitmap != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = compositeBitmap!!.asImageBitmap(),
                                    contentDescription = "Live Customized Flyer",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                                Surface(
                                    shape = RoundedCornerShape(topStart = 8.dp),
                                    color = Color(0xCC091322),
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                ) {
                                    Text(
                                        "🔍 Tap to Zoom",
                                        color = Color(0xFFBAE6FD),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        } else {
                            Text("Rendering custom flyer...", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export / Share Actions (Crash-Proof Android Gallery & WhatsApp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                compositeBitmap?.let { bmp ->
                                    val file = flyerRenderer.saveBitmapToCache(bmp)
                                    val date = fieldValues["event_date_time"] ?: ""
                                    val venue = fieldValues["event_venue"] ?: ""
                                    val caption = "🏛️ *$chapterName*\n📢 *${selectedTemplate?.title ?: "JCI Official Event"}*\n\n📅 Date: $date\n📍 Venue: $venue\n👤 President: $presidentName\n\n_Generated via JCI LEAD360 Mobile_"
                                    AndroidShareManager.shareToWhatsApp(context, file, caption)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💬 WhatsApp Share", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                compositeBitmap?.let { bmp ->
                                    val filename = "${selectedTemplate?.title ?: "JCI_Flyer"}_${System.currentTimeMillis()}.png"
                                    AndroidShareManager.saveBitmapToGallery(context, bmp, filename, Bitmap.CompressFormat.PNG)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬇️ Save to Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Horizontal Switcher for other templates in category
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK TEMPLATE SELECTOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "View All ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.clickable { activeStudioTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTemplates) { tpl ->
                        val isSelected = (tpl.id == selectedTemplate?.id)
                        val rawUrl = tpl.baseImageUrl
                        val imageUrl = if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"

                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    selectedTemplate = tpl
                                    initDefaultFieldValues(tpl, presidentName, fieldValues)
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0097D8) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = tpl.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .background(Color(0xFF091322)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = tpl.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = if (isSelected) Color(0xFF0097D8) else Color(0xFF0F172A),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Smart Event Details Form (Date/Time Picker Dialogs + Venue Quick Select)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUSTOMIZE MEETING & EVENT DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Modify details below; composite updates automatically in real-time:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date & Time Picker Triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📅 $selectedDateString", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { timePickerDialog.show() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⏰ $selectedTimeString", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Venue Quick Presets
                    Text("QUICK VENUE PRESETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        items(venuePresets) { vp ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable {
                                    fieldValues["event_venue"] = vp
                                    selectedTemplate?.let { tpl ->
                                        triggerComposite(flyerRenderer, tpl, cachedBaseBitmap, chapterLogoBitmap, presidentPhotoBitmap, fieldValues) { bmp -> compositeBitmap = bmp }
                                    }
                                }
                            ) {
                                Text(vp, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    // Dynamic Remaining Fields (e.g. Chief Guest, Custom Theme)
                    selectedTemplate?.layers?.forEach { layer ->
                        val zoneType = (layer.type ?: layer.key ?: "custom_text").lowercase()
                        if (!zoneType.contains("logo") && !zoneType.contains("photo") && zoneType != "event_date_time") {
                            val currentVal = fieldValues[zoneType] ?: ""

                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { newVal ->
                                    fieldValues[zoneType] = newVal
                                    selectedTemplate?.let { tpl ->
                                        triggerComposite(flyerRenderer, tpl, cachedBaseBitmap, chapterLogoBitmap, presidentPhotoBitmap, fieldValues) { resultBmp ->
                                            compositeBitmap = resultBmp
                                        }
                                    }
                                },
                                label = { Text(layer.label ?: zoneType.replace("_", " ").uppercase()) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Interactive Zoom Dialog Modal
        if (showFullScreenZoom && compositeBitmap != null) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                offset += offsetChange
            }

            Dialog(
                onDismissRequest = { showFullScreenZoom = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Zoomable Image
                    Image(
                        bitmap = compositeBitmap!!.asImageBitmap(),
                        contentDescription = "Zoomed Flyer Creative",
                        modifier = Modifier
                            .fillMaxSize()
                            .transformable(state = state)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )

                    // Controls Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x99000000)
                        ) {
                            Text(
                                text = "Pinch to Zoom (${String.format("%.1f", scale)}x)",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Reset Zoom Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0097D8),
                                modifier = Modifier.clickable {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            ) {
                                Text(
                                    text = "Reset",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            // Close Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier.clickable { showFullScreenZoom = false }
                            ) {
                                Text(
                                    text = "✕ Close",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

private fun initDefaultFieldValues(
    template: TemplateModel,
    presidentName: String,
    fieldValues: MutableMap<String, String>
) {
    template.layers?.forEach { layer ->
        val zoneType = (layer.type ?: layer.key ?: "custom_text").lowercase()
        if (fieldValues[zoneType] == null) {
            fieldValues[zoneType] = when (zoneType) {
                "president_name" -> presidentName
                "event_date_time" -> "Friday, 15th Jan 2027 • 6:30 PM"
                "event_venue" -> "Hotel Royal Grand, Main Hall"
                "chief_guest" -> "JC Sen. Guest of Honour, Zone President"
                else -> layer.label ?: "Installation Ceremony"
            }
        }
    }
}

private fun triggerComposite(
    renderer: AndroidFlyerRenderer,
    template: TemplateModel,
    baseBitmap: Bitmap?,
    chapterLogoBitmap: Bitmap?,
    presidentPhotoBitmap: Bitmap?,
    fieldValues: Map<String, String>,
    onComplete: (Bitmap) -> Unit
) {
    baseBitmap ?: return
    val result = renderer.compositeDynamicFlyer(
        template = template,
        baseBitmap = baseBitmap,
        chapterLogoBitmap = chapterLogoBitmap,
        presidentPhotoBitmap = presidentPhotoBitmap,
        fieldValues = fieldValues
    )
    onComplete(result)
}
