package com.jci2027.lead360.president

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.jci2027.lead360.engine.AndroidFlyerRenderer
import com.jci2027.lead360.model.TemplateModel
import com.jci2027.lead360.model.TemplatesResponse
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Native Android Dynamic Flyer Studio for Chapter Presidents
 * 1. Automatically fetches official live templates from the Hostinger VPS backend
 * 2. Displays auto-branding for the logged-in President
 * 3. Builds instant clean input fields for event variables (Date, Time, Venue, Chief Guest, Theme)
 * 4. Renders live high-res canvas composite in real-time as the President types
 * 5. 1-Tap 300 DPI Export & Direct WhatsApp Intent Sharing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresidentFlyerStudioScreen(
    token: String,
    presidentName: String,
    chapterName: String,
    baseUrl: String = "https://lead.sriddha.com"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flyerRenderer = remember { AndroidFlyerRenderer(context) }

    var templates by remember { mutableStateOf<List<TemplateModel>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<TemplateModel?>(null) }
    var isLoadingTemplates by remember { mutableStateOf(true) }

    // Dynamic field values
    val fieldValues = remember { mutableStateMapOf<String, String>() }

    // Rendered Composite Bitmap
    var compositeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCompositing by remember { mutableStateOf(false) }

    // Cached Base & Logo Bitmaps
    var cachedBaseBitmap by remember { mutableStateOf<Bitmap?>(null) }

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
                if (response.isSuccessful && response.body()?.templates?.isNotEmpty() == true) {
                    val list = response.body()!!.templates
                    withContext(Dispatchers.Main) {
                        templates = list
                        selectedTemplate = list.first()
                        initDefaultFieldValues(list.first(), presidentName, fieldValues)
                        isLoadingTemplates = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingTemplates = false
                }
            }
        }
    }

    // Load base image when selected template changes
    LaunchedEffect(selectedTemplate) {
        val tpl = selectedTemplate ?: return@LaunchedEffect
        val imageUrl = if (tpl.baseImageUrl.startsWith("http")) tpl.baseImageUrl else "$baseUrl${tpl.baseImageUrl}"

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
                    triggerComposite(flyerRenderer, tpl, bmp, fieldValues) { resultBmp ->
                        compositeBitmap = resultBmp
                    }
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "⚡ JCI 1-Tap Flyer Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "$chapterName • Year 2027",
                            fontSize = 11.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF091428)
                )
            )
        }
    ) { paddingValues ->
        if (isLoadingTemplates) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0097D8))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Live Composite Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
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
                        Text(
                            text = "Live 300 DPI Flyer Composite",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x330097D8)
                        ) {
                            Text(
                                text = "INSTANT PREVIEW",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (compositeBitmap != null) {
                        Image(
                            bitmap = compositeBitmap!!.asImageBitmap(),
                            contentDescription = "Flyer Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 260.dp, max = 340.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0097D8))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Tap Action Buttons
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
                                    val caption = "🏛️ *$chapterName*\n📢 *${selectedTemplate?.title ?: "JCI Event"}*\n\n📅 Date: $date\n📍 Venue: $venue\n👤 President: $presidentName\n\n_Generated via JCI LEAD360 Mobile_"
                                    AndroidShareManager.shareToWhatsApp(context, file, caption)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💬 WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                compositeBitmap?.let { bmp ->
                                    flyerRenderer.saveBitmapToCache(bmp)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬇️ Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Select Template Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. CHOOSE OFFICIAL TEMPLATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    templates.forEach { tpl ->
                        val isSelected = (tpl.id == selectedTemplate?.id)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedTemplate = tpl
                                    initDefaultFieldValues(tpl, presidentName, fieldValues)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF0097D8) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tpl.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF0369A1) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = tpl.category ?: "General",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Text("✓ Selected", color = Color(0xFF0097D8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Dynamic Variables Form (Customized without styling burdens)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. EVENT DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "The design, layout, and fonts are pre-configured. Simply customize your meeting details below:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    selectedTemplate?.layers?.forEach { layer ->
                        val zoneType = (layer.type ?: layer.key ?: "custom_text").lowercase()
                        if (!zoneType.contains("logo") && !zoneType.contains("photo")) {
                            val currentVal = fieldValues[zoneType] ?: ""

                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { newVal ->
                                    fieldValues[zoneType] = newVal
                                    selectedTemplate?.let { tpl ->
                                        triggerComposite(flyerRenderer, tpl, cachedBaseBitmap, fieldValues) { resultBmp ->
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
                "event_date_time" -> "Sunday, 15th Jan 2027 • 6:30 PM"
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
    fieldValues: Map<String, String>,
    onComplete: (Bitmap) -> Unit
) {
    baseBitmap ?: return
    val result = renderer.compositeDynamicFlyer(
        template = template,
        baseBitmap = baseBitmap,
        chapterLogoBitmap = null,
        presidentPhotoBitmap = null,
        fieldValues = fieldValues
    )
    onComplete(result)
}
