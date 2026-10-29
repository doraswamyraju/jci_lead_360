package com.jci2027.lead360.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

/**
 * Helper to persist user photos to internal app storage so they never disappear on app restart
 */
object InternalPhotoStore {
    fun saveImageToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromInternalStorage(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }
}

/**
 * JCI Photo Frames Maker Screen
 */
@Composable
fun FramesScreen() {
    val context = LocalContext.current
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            Toast.makeText(context, "Photo loaded into frame!", Toast.LENGTH_SHORT).show()
        }
    }

    val framesList = listOf(
        "🎂 Birthday Greetings Frame 2027",
        "🏆 Outstanding Achievement Frame",
        "🏛️ Official Installation Ceremony Frame",
        "🌟 Leader of the Month Recognition",
        "🌍 World Earth Day Celebration Frame"
    )
    var selectedFrame by remember { mutableStateOf(framesList[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("📸 JCI Photo Frame Maker", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("Select official JCI 2027 photo frames for birthdays, greetings & achievements.", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF0097D8), RoundedCornerShape(12.dp))
                        .clickable { photoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Framed Photo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to pick portrait photo", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Selected Frame: $selectedFrame", color = Color(0xFFBAE6FD), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { photoPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(if (selectedPhotoUri != null) "Change Photo" else "Select Photo from Gallery", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Available Frames Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SELECT OFFICIAL 2027 FRAME", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(10.dp))

                framesList.forEach { frameTitle ->
                    val isSel = selectedFrame == frameTitle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF0097D8) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedFrame = frameTitle }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(frameTitle, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) Color(0xFF0369A1) else Color(0xFF334155))
                            if (isSel) Text("✓ Selected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
                        }
                    }
                }
            }
        }
    }
}

/**
 * President Profile & Chapter LO Settings Screen
 * Manages:
 * 1. Chapter Official Logo & details (Zone, Phone, Email, Address)
 * 2. President Personal details (Photo, Name, Phone, Email, Year)
 * 3. Logout action
 */
@Composable
fun ProfileScreen(
    name: String,
    chapterName: String,
    role: String,
    onLogout: () -> Unit,
    onProfileUpdated: (updatedName: String, updatedChapter: String, photoPath: String?, logoPath: String?) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lead360_auth_prefs", Context.MODE_PRIVATE) }

    // Saved internal paths
    var savedPresidentPhotoPath by remember { mutableStateOf(prefs.getString("president_photo_path", null)) }
    var savedChapterLogoPath by remember { mutableStateOf(prefs.getString("chapter_logo_path", null)) }

    // President Profile State
    var presidentNameInput by remember { mutableStateOf(prefs.getString("auth_name", name) ?: name) }
    var presidentPhone by remember { mutableStateOf(prefs.getString("auth_phone", "+91 98765 43210") ?: "+91 98765 43210") }
    var presidentEmail by remember { mutableStateOf(prefs.getString("saved_email", "president@jcitirupati.com") ?: "president@jcitirupati.com") }

    // Chapter LO Profile State
    var chapterNameInput by remember { mutableStateOf(prefs.getString("auth_chapter", chapterName) ?: chapterName) }
    var chapterZone by remember { mutableStateOf(prefs.getString("chapter_zone", "Zone IV") ?: "Zone IV") }
    var chapterEmail by remember { mutableStateOf(prefs.getString("chapter_email", "info@jcitirupati.com") ?: "info@jcitirupati.com") }
    var chapterPhone by remember { mutableStateOf(prefs.getString("chapter_phone", "+91 98765 43210") ?: "+91 98765 43210") }

    var isSaved by remember { mutableStateOf(false) }

    // Photo Launchers that immediately write to persistent internal storage
    val presidentPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = InternalPhotoStore.saveImageToInternalStorage(context, uri, "president_profile_photo.png")
            if (savedPath != null) {
                savedPresidentPhotoPath = savedPath
                prefs.edit().putString("president_photo_path", savedPath).apply()
                onProfileUpdated(presidentNameInput, chapterNameInput, savedPath, savedChapterLogoPath)
                Toast.makeText(context, "✓ President portrait saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val chapterLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = InternalPhotoStore.saveImageToInternalStorage(context, uri, "chapter_official_logo.png")
            if (savedPath != null) {
                savedChapterLogoPath = savedPath
                prefs.edit().putString("chapter_logo_path", savedPath).apply()
                onProfileUpdated(presidentNameInput, chapterNameInput, savedPresidentPhotoPath, savedPath)
                Toast.makeText(context, "✓ Chapter logo saved! Now displayed in TopBar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val presidentBitmap = remember(savedPresidentPhotoPath) {
        InternalPhotoStore.loadBitmapFromInternalStorage(savedPresidentPhotoPath)
    }

    val chapterLogoBitmap = remember(savedChapterLogoPath) {
        InternalPhotoStore.loadBitmapFromInternalStorage(savedChapterLogoPath)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo with tap to change
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable { presidentPhotoLauncher.launch("image/*") }
                        .border(3.dp, Color(0xFF0097D8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (presidentBitmap != null) {
                        Image(
                            bitmap = presidentBitmap.asImageBitmap(),
                            contentDescription = "President Portrait",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = presidentNameInput.firstOrNull()?.toString() ?: "P",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(presidentNameInput, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(chapterNameInput, fontSize = 13.sp, color = Color(0xFFBAE6FD))

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0097D8)
                ) {
                    Text(
                        text = "CHAPTER PRESIDENT • 2027",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Tap portrait above to update profile photo",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 2. Local Organization (LO) Chapter Profile Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🏛️ Local Organization (LO) Chapter Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Configure your official chapter logo & parameters (shown in TopBar)",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Chapter Logo Upload Bar
                Text("OFFICIAL CHAPTER LOGO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        .clickable { chapterLogoLauncher.launch("image/*") }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        if (chapterLogoBitmap != null) {
                            Image(
                                bitmap = chapterLogoBitmap.asImageBitmap(),
                                contentDescription = "Chapter Logo",
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("🏛️", fontSize = 24.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (chapterLogoBitmap != null) "Chapter Logo Attached" else "Upload Chapter Logo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Replaces LEAD360 logo in TopBar and appears on flyers",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = { chapterLogoLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (chapterLogoBitmap != null) "Change" else "Browse", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = chapterNameInput,
                    onValueChange = { chapterNameInput = it },
                    label = { Text("Chapter Organization Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chapterZone,
                    onValueChange = { chapterZone = it },
                    label = { Text("JCI Zone / Region") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chapterEmail,
                    onValueChange = { chapterEmail = it },
                    label = { Text("Chapter Official Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chapterPhone,
                    onValueChange = { chapterPhone = it },
                    label = { Text("Chapter Official Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // 3. President Personal Profile Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "👤 President Personal Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Appears automatically on dynamic flyers and invitations",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                OutlinedTextField(
                    value = presidentNameInput,
                    onValueChange = { presidentNameInput = it },
                    label = { Text("President Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = presidentPhone,
                    onValueChange = { presidentPhone = it },
                    label = { Text("President Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = presidentEmail,
                    onValueChange = { presidentEmail = it },
                    label = { Text("President Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        prefs.edit().apply {
                            putString("auth_name", presidentNameInput.trim())
                            putString("auth_phone", presidentPhone.trim())
                            putString("auth_chapter", chapterNameInput.trim())
                            putString("chapter_zone", chapterZone.trim())
                            putString("chapter_email", chapterEmail.trim())
                            putString("chapter_phone", chapterPhone.trim())
                        }.apply()
                        onProfileUpdated(presidentNameInput.trim(), chapterNameInput.trim(), savedPresidentPhotoPath, savedChapterLogoPath)
                        isSaved = true
                        Toast.makeText(context, "✓ President & LO Details Saved Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isSaved) "✓ Profile & Chapter Details Saved" else "Save All Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Logout Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Session Management", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Clears active session credentials on this device", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp))

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Sign Out from Device", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
