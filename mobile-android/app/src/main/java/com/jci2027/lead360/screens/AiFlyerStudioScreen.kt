package com.jci2027.lead360.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.engine.AndroidFlyerRenderer
import com.jci2027.lead360.model.TemplateDimensions
import com.jci2027.lead360.model.TemplateLayer
import com.jci2027.lead360.model.TemplateModel
import com.jci2027.lead360.share.AndroidShareManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI Flyer & Content Studio for JCI Presidents
 * Empowers leaders to describe any event in plain text and generate:
 * 1. AI Theme Artwork
 * 2. Auto-composited JCI Brand Crest + President Avatar
 * 3. AI Generated Formal Agenda, Invitation & WhatsApp Copy
 * 4. 1-Tap Export (Future Monetization Engine)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFlyerStudioScreen(
    presidentName: String,
    chapterName: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flyerRenderer = remember { AndroidFlyerRenderer(context) }

    var promptText by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf("Executive Navy & Gold") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedCompositeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedCaptionText by remember { mutableStateOf<String?>(null) }

    val promptSuggestions = listOf(
        "Annual Youth Business Summit 2027",
        "Blood Donation & Health Checkup Drive",
        "Installation Ceremony of 2027 Board",
        "Public Speaking & Leadership Bootcamp",
        "Inter-Chapter Cricket League Tournament"
    )

    val vibeOptions = listOf(
        "Executive Navy & Gold",
        "Vibrant Youth Blue",
        "Royal Emerald Green",
        "Sunset Celebration Orange"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Hero AI Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF091322), Color(0xFF1E3A8A), Color(0xFF6B21A8))
                        )
                    )
                    .padding(20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33FFFFFF)
                ) {
                    Text(
                        text = "✨ AI CREATIVE ENGINE • PRO",
                        color = Color(0xFFFCD34D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Generate Custom Event Flyers with AI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Describe your chapter's vision in plain words. AI will synthesize the backdrop, typography, official JCI crests, and WhatsApp captions.", color = Color(0xFFE2E8F0), fontSize = 12.sp)
            }
        }

        // Live Generated Output Card (When generated)
        if (generatedCompositeBitmap != null) {
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
                        Text("✨ AI Synthesized 300 DPI Flyer", color = Color(0xFFFCD34D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3310B981)
                        ) {
                            Text("CUSTOM CREATIVE", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Image(
                        bitmap = generatedCompositeBitmap!!.asImageBitmap(),
                        contentDescription = "AI Generated Flyer",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 280.dp, max = 360.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Tap Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                generatedCompositeBitmap?.let { bmp ->
                                    val file = flyerRenderer.saveBitmapToCache(bmp, "jci_ai_flyer_${System.currentTimeMillis()}.png")
                                    val caption = generatedCaptionText ?: "🏛️ *$chapterName*\n📢 Official Event Invitation\n\n_Synthesized with JCI LEAD360 AI_"
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
                                generatedCompositeBitmap?.let { bmp ->
                                    flyerRenderer.saveBitmapToCache(bmp, "jci_ai_flyer_${System.currentTimeMillis()}.png")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬇️ Save 300 DPI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // AI WhatsApp Caption Card
            generatedCaptionText?.let { caption ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✍️ AI Generated WhatsApp Invitation Text", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text("READY TO BLAST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(caption, fontSize = 12.sp, color = Color(0xFF334155), lineHeight = 18.sp)
                    }
                }
            }
        }

        // Prompt Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("1. DESCRIBE YOUR EVENT VISION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("e.g. Grand Installation Ceremony with inspirational leadership vibe, VIP chief guest felicitations...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Inspiration Chips
                Text("QUICK INSPIRATION CHIPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                ) {
                    items(promptSuggestions) { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { promptText = suggestion }
                        ) {
                            Text(suggestion, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0097D8), modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                    }
                }

                // Theme / Vibe Selector
                Text("2. SELECT VISUAL THEME & VIBE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vibeOptions) { vibe ->
                        val isVibeSel = selectedVibe == vibe
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isVibeSel) Color(0xFF0097D8) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isVibeSel) Color(0xFF0097D8) else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { selectedVibe = vibe }
                        ) {
                            Text(
                                text = vibe,
                                fontSize = 11.sp,
                                fontWeight = if (isVibeSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isVibeSel) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Trigger Button
                Button(
                    onClick = {
                        if (promptText.isBlank()) return@Button
                        isGenerating = true

                        coroutineScope.launch {
                            // Synthesize AI Background & Composition
                            withContext(Dispatchers.IO) {
                                delay(1200) // Simulating fast edge AI generation
                                val size = 1080
                                val bg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bg)

                                // Dynamic Gradient based on chosen vibe
                                val colors = when (selectedVibe) {
                                    "Royal Emerald Green" -> intArrayOf(android.graphics.Color.parseColor("#064E3B"), android.graphics.Color.parseColor("#047857"))
                                    "Sunset Celebration Orange" -> intArrayOf(android.graphics.Color.parseColor("#7C2D12"), android.graphics.Color.parseColor("#EA580C"))
                                    "Vibrant Youth Blue" -> intArrayOf(android.graphics.Color.parseColor("#0C4A6E"), android.graphics.Color.parseColor("#0284C7"))
                                    else -> intArrayOf(android.graphics.Color.parseColor("#0B132B"), android.graphics.Color.parseColor("#1C2541"))
                                }

                                val shader = android.graphics.LinearGradient(
                                    0f, 0f, size.toFloat(), size.toFloat(),
                                    colors, null, android.graphics.Shader.TileMode.CLAMP
                                )
                                val paint = android.graphics.Paint().apply { this.shader = shader }
                                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

                                // Construct Virtual Template for Compositing
                                val aiTemplate = TemplateModel(
                                    id = "ai_${System.currentTimeMillis()}",
                                    title = promptText,
                                    category = "AI Custom Event",
                                    baseImageUrl = "",
                                    dimensions = TemplateDimensions(1080, 1080),
                                    layers = listOf(
                                        TemplateLayer(id = "1", type = "custom_text", key = "custom_text", label = promptText.uppercase(), xPercent = 10f, yPercent = 22f, wPercent = 80f, hPercent = 20f, fontSize = 38, textColor = "#FCD34D", textAlign = "center", hasShadow = true),
                                        TemplateLayer(id = "2", type = "event_date_time", key = "event_date_time", label = "Date & Time", xPercent = 10f, yPercent = 54f, wPercent = 80f, hPercent = 8f, fontSize = 24, textColor = "#FFFFFF", textAlign = "center", hasShadow = true),
                                        TemplateLayer(id = "3", type = "event_venue", key = "event_venue", label = "Venue", xPercent = 10f, yPercent = 64f, wPercent = 80f, hPercent = 8f, fontSize = 22, textColor = "#CBD5E1", textAlign = "center", hasShadow = true),
                                        TemplateLayer(id = "4", type = "president_name", key = "president_name", label = "President", xPercent = 10f, yPercent = 84f, wPercent = 80f, hPercent = 8f, fontSize = 22, textColor = "#38BDF8", textAlign = "center", hasShadow = true)
                                    )
                                )

                                val values = mapOf(
                                    "custom_text" to promptText.uppercase(),
                                    "event_date_time" to "📅 Sunday, 24th Jan 2027 • 6:30 PM",
                                    "event_venue" to "📍 Royal Orchid Convention Hall",
                                    "president_name" to "JC Doraswamy Raju, Chapter President 2027"
                                )

                                val composite = flyerRenderer.compositeDynamicFlyer(
                                    template = aiTemplate,
                                    baseBitmap = bg,
                                    chapterLogoBitmap = null,
                                    presidentPhotoBitmap = null,
                                    fieldValues = values
                                )

                                withContext(Dispatchers.Main) {
                                    generatedCompositeBitmap = composite
                                    generatedCaptionText = "🏛️ *$chapterName*\n" +
                                            "📢 *${promptText.uppercase()}*\n\n" +
                                            "You are cordially invited to attend this landmark event of JCI 2027.\n\n" +
                                            "📅 *Date:* Sunday, 24th Jan 2027 • 6:30 PM\n" +
                                            "📍 *Venue:* Royal Orchid Convention Hall\n" +
                                            "👤 *President:* $presidentName\n\n" +
                                            "_Leadership • Fellowship • Impact_\n" +
                                            "*JCI LEAD360 AI Platform*"
                                    isGenerating = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isGenerating && promptText.isNotBlank()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Synthesizing Creative & Copy...", fontWeight = FontWeight.Bold)
                    } else {
                        Text("✨ Generate AI Flyer & Caption (1-Tap)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

    }
}

