package com.jci2027.lead360.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.engine.JciLogoGenerator
import com.jci2027.lead360.share.AndroidShareManager

data class TeamMemberItem(
    val role: String,
    val name: String,
    val email: String,
    val phone: String,
    val status: String = "Active"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LgbAndPstManagementScreen(
    token: String,
    chapterName: String,
    presidentName: String,
    baseUrl: String = "https://lead.sriddha.com"
) {
    val context = LocalContext.current
    val logoGenerator = remember { JciLogoGenerator(context) }

    // 0: Directory, 1: Grant Access, 2: Official Logo Generator, 3: LO Profile
    var selectedTab by remember { mutableStateOf(0) }

    // Form inputs for creating credentials
    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberPassword by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf("SECRETARY") }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Logo Generator State
    var customLogoChapterName by remember { mutableStateOf(chapterName) }
    var selectedLogoTheme by remember { mutableStateOf(JciLogoGenerator.LogoTheme.JCI_BLUE) }
    var generatedLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Re-generate logo when parameters change
    LaunchedEffect(customLogoChapterName, selectedLogoTheme) {
        val bmp = logoGenerator.generateOfficialChapterLogo(
            chapterName = customLogoChapterName,
            theme = selectedLogoTheme
        )
        generatedLogoBitmap = bmp
    }

    // LO Profile details state
    var loZone by remember { mutableStateOf("Zone IV") }
    var loEmail by remember { mutableStateOf("info@jcitirupati.com") }
    var loPhone by remember { mutableStateOf("+91 98765 43210") }
    var isSavedLo by remember { mutableStateOf(false) }

    val currentTeam = remember {
        mutableStateListOf(
            TeamMemberItem("PRESIDENT", presidentName, "president@jcitirupati.com", "+91 98765 43210"),
            TeamMemberItem("SECRETARY", "JC Secretary 2027", "secretary@jcitirupati.com", "+91 98765 43211"),
            TeamMemberItem("TREASURER", "JC Treasurer 2027", "treasurer@jcitirupati.com", "+91 98765 43212")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("👥 Leadership, Team & Brand Asset Hub", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(chapterName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("LGB board, PST credentials, official vector logo generator & profile", color = Color(0xFF38BDF8), fontSize = 12.sp)
            }
        }

        // 4-Tab Navigation Selector (Directory | Access | Logo Gen | Profile)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("LGB Directory", "Grant Access", "🎨 Logo Gen", "LO Profile").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color.White else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    onClick = { selectedTab = index }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) Color(0xFF0097D8) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // TAB 0: LGB Directory
        if (selectedTab == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentTeam.forEach { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE0F2FE),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(member.role, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0369A1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(member.email, fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(member.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text("Active", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: Grant Access / Create Login Credentials for PST
        if (selectedTab == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🔑 Create Login Credentials for PST", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text("Generate credentials for your Chapter Secretary or Treasurer to access templates and post on WhatsApp.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 14.dp))

                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Member Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("Login Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberPassword,
                        onValueChange = { newMemberPassword = it },
                        label = { Text("Assign Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newMemberPhone,
                        onValueChange = { newMemberPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("SELECT ROLE DESIGNATION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("SECRETARY", "TREASURER").forEach { r ->
                            val isSel = newMemberRole == r
                            Button(
                                onClick = { newMemberRole = r },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) Color(0xFF0097D8) else Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(r, color = if (isSel) Color.White else Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = statusMessage!!,
                            color = if (isError) Color(0xFFDC2626) else Color(0xFF16A34A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (newMemberName.isBlank() || newMemberEmail.isBlank() || newMemberPassword.isBlank()) {
                                statusMessage = "Please complete all required fields"
                                isError = true
                                return@Button
                            }
                            currentTeam.add(
                                TeamMemberItem(newMemberRole, newMemberName, newMemberEmail, newMemberPhone)
                            )
                            isError = false
                            statusMessage = "✓ Successfully generated $newMemberRole account credentials!"
                            newMemberName = ""
                            newMemberEmail = ""
                            newMemberPassword = ""
                            newMemberPhone = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isSubmitting
                    ) {
                        Text("Create & Dispatch Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TAB 2: JCI Official Logo Lockup Generator (Equivalent to logo.jciath.be)
        if (selectedTab == 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎨 Official JCI Chapter Logo Generator",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Generates high-resolution corporate-identity compliant chapter lockup logos (inspired by logo.jciath.be).",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Logo Canvas Preview Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                if (selectedLogoTheme.isDarkBg) Color(0xFF091322) else Color(0xFFF1F5F9),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                Color(0xFFCBD5E1),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (generatedLogoBitmap != null) {
                            Image(
                                bitmap = generatedLogoBitmap!!.asImageBitmap(),
                                contentDescription = "Generated JCI Logo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            )
                        } else {
                            CircularProgressIndicator(color = Color(0xFF0097D8))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chapter Name Input
                    OutlinedTextField(
                        value = customLogoChapterName,
                        onValueChange = { customLogoChapterName = it },
                        label = { Text("Chapter Organization Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme Selector Chips
                    Text("SELECT BRAND COLOR VARIANT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("Official Blue", JciLogoGenerator.LogoTheme.JCI_BLUE),
                            Pair("JCI Navy", JciLogoGenerator.LogoTheme.JCI_NAVY),
                            Pair("Black", JciLogoGenerator.LogoTheme.MONOCHROME_BLACK),
                            Pair("White", JciLogoGenerator.LogoTheme.MONOCHROME_WHITE)
                        ).forEach { (label, theme) ->
                            val isSel = selectedLogoTheme == theme
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFF0097D8) else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF0097D8) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLogoTheme = theme }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color(0xFF334155))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1-Tap Action: Export & Share Transparent PNG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                generatedLogoBitmap?.let { bmp ->
                                    val file = logoGenerator.saveLogoToCache(bmp, "${customLogoChapterName}_Official_Logo.png")
                                    val caption = "🏛️ Official Compliant Logo for *$customLogoChapterName*\nGenerated via JCI LEAD360 Platform"
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
                                generatedLogoBitmap?.let { bmp ->
                                    logoGenerator.saveLogoToCache(bmp, "${customLogoChapterName}_Official_Logo.png")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬇️ Save PNG", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // TAB 3: LO Profile & Chapter Details
        if (selectedTab == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🏛️ Local Organization (LO) Details", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text("Official chapter parameters managed by Chapter President.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 14.dp))

                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = {},
                        label = { Text("Chapter Organization Name") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loZone,
                        onValueChange = { loZone = it },
                        label = { Text("Assigned JCI Zone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loEmail,
                        onValueChange = { loEmail = it },
                        label = { Text("Official Chapter Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loPhone,
                        onValueChange = { loPhone = it },
                        label = { Text("Helpline Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isSavedLo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✓ Chapter details saved successfully", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { isSavedLo = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("💾 Save Chapter Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

    }
}
