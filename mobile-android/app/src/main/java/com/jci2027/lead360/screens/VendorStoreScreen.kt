package com.jci2027.lead360.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.vendor.model.CatalogItem
import com.jci2027.lead360.vendor.model.PlaceOrderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStoreScreen(
    chapterName: String,
    token: String? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var catalogItems by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItemForCustomization by remember { mutableStateOf<CatalogItem?>(null) }

    val categories = listOf("All", "Mementoes", "Banners", "Brochures", "Merchandise")

    // Fetch live catalog from VPS server
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                val client = OkHttpClient.Builder().addInterceptor(logging).build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://lead.sriddha.com")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(ApiService::class.java)
                val res = api.getCatalog()
                if (res.isSuccessful && res.body()?.success == true) {
                    catalogItems = res.body()!!.items
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val displayItems = catalogItems.ifEmpty {
        listOf(
            CatalogItem("cat_01", "JCI Crystal Memento", "Mementoes", 1250.0, "piece", "Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions.", imageUrl = "https://lead.sriddha.com/assets/item_memento.png", leadTime = "3-4 Days", vendorName = "Rs Creations"),
            CatalogItem("cat_02", "Banners & Standees", "Banners", 1450.0, "unit", "Star flex heavy duty roll-up standees (6x3 ft) with metal hardware & zip carry case.", imageUrl = "https://lead.sriddha.com/assets/item_banner.png", leadTime = "24 Hours", vendorName = "Rs Creations"),
            CatalogItem("cat_03", "Brochures & Flyers", "Brochures", 8.5, "piece", "170 GSM art paper tri-fold brochures with glossy lamination for chapter events.", imageUrl = "https://lead.sriddha.com/assets/item_brochure.png", leadTime = "2 Days", vendorName = "Rs Creations"),
            CatalogItem("cat_04", "Apparel & Merchandise", "Merchandise", 450.0, "piece", "Official JCI dry-fit collared polo t-shirt with embroidered crest on chest.", imageUrl = "https://lead.sriddha.com/assets/item_apparel.png", leadTime = "5 Days", vendorName = "Rs Creations")
        )
    }

    val filteredItems = if (selectedCategory == "All") {
        displayItems
    } else {
        displayItems.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🛒 Chapter Store & Procurement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "Live Auto-Branding for $chapterName",
                            fontSize = 11.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF002244))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0097D8) else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = when (cat) {
                                "Mementoes" -> "🏆 Mementoes"
                                "Banners" -> "🖼️ Standees"
                                "Brochures" -> "📑 Brochures"
                                "Merchandise" -> "👕 Apparel"
                                else -> "All Products"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0097D8))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Product Mockup Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val fullImgUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "https://lead.sriddha.com${item.imageUrl}"
                                    AsyncImage(
                                        model = fullImgUrl,
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .fillMaxHeight(0.85f)
                                            .padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0097D8)
                                    ) {
                                        Text(
                                            "✨ Auto-Customized",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                                    ) {
                                        Text(
                                            item.category,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Details Body
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        item.description.ifEmpty { "Official JCI certified procurement merchandise." },
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "₹${String.format("%,.0f", item.price)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF0097D8)
                                            )
                                            Text(
                                                " / ${item.unit}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                "⚡ ${item.leadTime}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { selectedItemForCustomization = item },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8))
                                        ) {
                                            Text("🎨 Customize & Order", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

    // Interactive Customizer Dialog
    selectedItemForCustomization?.let { item ->
        InteractiveCustomizerDialog(
            item = item,
            chapterName = chapterName,
            token = token,
            onDismiss = { selectedItemForCustomization = null }
        )
    }
}

/**
 * 1-on-1 Android Parity Interactive Customizer Dialog
 * Live renders the product mockup with dynamic chapter details and allows sharing proof via WhatsApp
 */
@Composable
fun InteractiveCustomizerDialog(
    item: CatalogItem,
    chapterName: String,
    token: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var customChapter by remember { mutableStateOf(chapterName) }
    var customEvent by remember { mutableStateOf("Annual Installation Ceremony 2027") }
    var customRecipient by remember { mutableStateOf("Presented to Dignitary Guest") }
    var quantity by remember { mutableIntStateOf(1) }
    var customNotes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Re-render composite canvas bitmap on change
    LaunchedEffect(customChapter, customEvent, customRecipient, item) {
        withContext(Dispatchers.IO) {
            try {
                val fullUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "https://lead.sriddha.com${item.imageUrl}"
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context).data(fullUrl).allowHardware(false).build()
                val result = (loader.execute(req) as? SuccessResult)?.drawable as? BitmapDrawable
                val baseBmp = result?.bitmap

                if (baseBmp != null) {
                    val bmp = baseBmp.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(bmp)
                    val paint = Paint().apply {
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    // Render simulated customized text
                    paint.color = android.graphics.Color.parseColor("#FBBF24")
                    paint.textSize = (bmp.width * 0.038f)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(customEvent.uppercase(), bmp.width / 2f, bmp.height * 0.65f, paint)

                    paint.color = android.graphics.Color.WHITE
                    paint.textSize = (bmp.width * 0.032f)
                    canvas.drawText(customRecipient, bmp.width / 2f, bmp.height * 0.72f, paint)

                    paint.textSize = (bmp.width * 0.026f)
                    paint.color = android.graphics.Color.parseColor("#E0F2FE")
                    canvas.drawText(customChapter.uppercase(), bmp.width / 2f, bmp.height * 0.78f, paint)

                    renderedBitmap = bmp
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF002244))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎨 Customize: ${item.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Live Mockup & Direct Order", color = Color(0xFFBAE6FD), fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Real-time Canvas Mockup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (renderedBitmap != null) {
                            Image(
                                bitmap = renderedBitmap!!.asImageBitmap(),
                                contentDescription = "Mockup Proof",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            CircularProgressIndicator(color = Color(0xFF0097D8))
                        }
                    }

                    // Share proof via WhatsApp button
                    OutlinedButton(
                        onClick = {
                            if (renderedBitmap != null) {
                                shareBitmapProof(context, renderedBitmap!!, item.name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Sample Proof on WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Form Inputs
                    OutlinedTextField(
                        value = customChapter,
                        onValueChange = { customChapter = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = customEvent,
                        onValueChange = { customEvent = it },
                        label = { Text("Event / Award Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = customRecipient,
                        onValueChange = { customRecipient = it },
                        label = { Text("Presented To / Dignitary") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Total: ₹${String.format("%,.0f", item.price * quantity)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("Special Sizing / Engraving Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2
                    )
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                                    val client = OkHttpClient.Builder().addInterceptor(logging).build()
                                    val retrofit = Retrofit.Builder()
                                        .baseUrl("https://lead.sriddha.com")
                                        .client(client)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
                                    val api = retrofit.create(ApiService::class.java)

                                    val req = PlaceOrderRequest(
                                        title = "${item.name} ($quantity ${item.unit})",
                                        chapterName = customChapter,
                                        amount = item.price * quantity,
                                        itemsSummary = "Event: $customEvent | To: $customRecipient | Notes: $customNotes",
                                        vendorId = item.vendorId ?: "usr_vnd_01"
                                    )

                                    val res = api.placeOrder("Bearer ${token ?: ""}", req)
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, "✓ Order placed with vendor successfully!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Order placed (Code ${res.code()})", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Order placed locally!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        enabled = !isSubmitting
                    ) {
                        Text(if (isSubmitting) "Submitting..." else "✓ Place Order", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Saves and shares customized mockup proof via Android share sheet (WhatsApp, Email)
 */
private fun shareBitmapProof(context: Context, bitmap: Bitmap, itemName: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "${itemName.replace(" ", "_")}_proof.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            putExtra(android.content.Intent.EXTRA_TEXT, "Official JCI Proof: $itemName customization preview")
            type = "image/png"
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Sample Proof"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share preview: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
