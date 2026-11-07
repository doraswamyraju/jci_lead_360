package com.jci2027.lead360

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.auth.LoginScreen
import com.jci2027.lead360.navigation.Screen
import com.jci2027.lead360.president.PresidentFlyerStudioScreen
import com.jci2027.lead360.screens.ChapterWebsiteBuilderScreen
import com.jci2027.lead360.screens.DashboardOverviewScreen
import com.jci2027.lead360.screens.FramesScreen
import com.jci2027.lead360.screens.InteractiveCalendarScreen
import com.jci2027.lead360.screens.InternalPhotoStore
import com.jci2027.lead360.screens.JciResourcesScreen
import com.jci2027.lead360.screens.LgbAndPstManagementScreen
import com.jci2027.lead360.screens.MembersAndNetworkingScreen
import com.jci2027.lead360.screens.NotificationCenterBottomSheet
import com.jci2027.lead360.screens.ProfileScreen
import com.jci2027.lead360.screens.ProjectsAndTasksScreen
import com.jci2027.lead360.screens.ScanCardModuleScreen
import com.jci2027.lead360.screens.VendorStoreScreen
import com.jci2027.lead360.engine.QrCodeGenerator
import com.jci2027.lead360.theme.JciColors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { context.getSharedPreferences("lead360_auth_prefs", Context.MODE_PRIVATE) }
                
                var token by remember { mutableStateOf<String?>(prefs.getString("auth_token", null)) }
                var userRole by remember { mutableStateOf(prefs.getString("auth_role", "PRESIDENT") ?: "PRESIDENT") }
                var userName by remember { mutableStateOf(prefs.getString("auth_name", "JC Doraswamy Raju") ?: "JC Doraswamy Raju") }
                var userChapter by remember { mutableStateOf(prefs.getString("auth_chapter", "JCI Tirupati Innovations") ?: "JCI Tirupati Innovations") }

                var userPhotoPath by remember { mutableStateOf(prefs.getString("president_photo_path", null)) }
                var serverPhotoUrl by remember { mutableStateOf(prefs.getString("president_photo_server_url", null)) }
                var chapterLogoPath by remember { mutableStateOf(prefs.getString("chapter_logo_path", null)) }

                val userProfileBitmap = remember(userPhotoPath) {
                    InternalPhotoStore.loadBitmapFromInternalStorage(userPhotoPath)
                }

                val chapterLogoBitmap = remember(chapterLogoPath) {
                    InternalPhotoStore.loadBitmapFromInternalStorage(chapterLogoPath)
                }

                if (token == null) {
                    LoginScreen(
                        onLoginSuccess = { t, role, name, chapter ->
                            token = t
                            userRole = role
                            userName = name
                            userChapter = chapter
                            userPhotoPath = prefs.getString("president_photo_path", null)
                            serverPhotoUrl = prefs.getString("president_photo_server_url", null)
                            chapterLogoPath = prefs.getString("chapter_logo_path", null)
                        }
                    )
                } else if (userRole.equals("SUPER_ADMIN", ignoreCase = true) || userRole.equals("ADMIN", ignoreCase = true)) {
                    // Dedicated Super Admin Experience with Parity to Web Admin Module
                    com.jci2027.lead360.admin.AdminAppContainer(
                        token = token!!,
                        onSignOut = {
                            prefs.edit().clear().apply()
                            token = null
                        }
                    )
                } else if (userRole.equals("VENDOR", ignoreCase = true)) {
                    // Dedicated standalone Vendor App experience for official vendors
                    com.jci2027.lead360.vendor.VendorAppContainer(
                        onExitVendorMode = {
                            prefs.edit().clear().apply()
                            token = null
                        }
                    )
                } else {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                    var showNotificationBottomSheet by remember { mutableStateOf(false) }
                    var showExchangeQrModal by remember { mutableStateOf(false) }

                    // Navigation items for BottomBar (Report Primary Navigation)
                    val bottomNavItems = listOf(
                        Screen.Dashboard,
                        Screen.Projects,
                        Screen.Members,
                        Screen.Flyers,
                        Screen.Store
                    )

                    // Modal BottomSheet Notification Center
                    if (showNotificationBottomSheet) {
                        NotificationCenterBottomSheet(
                            onDismiss = { showNotificationBottomSheet = false }
                        )
                    }

                    // Luxury Instant Exchange QR Code Modal Dialog
                    if (showExchangeQrModal) {
                        val exchangeUrl = QrCodeGenerator.buildExchangeUrl(leaderId = "usr_president", baseUrl = "https://lead.sriddha.com")
                        val qrBitmap = remember {
                            QrCodeGenerator.generateQrBitmap(exchangeUrl, width = 480, height = 480)
                        }

                        AlertDialog(
                            onDismissRequest = { showExchangeQrModal = false },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showExchangeQrModal = false
                                        currentScreen = Screen.ScanCard
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Open Full Scanner", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { showExchangeQrModal = false },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Close")
                                }
                            },
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(Color(0xFF0097D8), Color(0xFF002F6C))
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_qr_code),
                                            contentDescription = "QR Code",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text("Exchange Contacts", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                        Text("President Digital Card", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }
                            },
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE2E8F0)),
                                        shadowElevation = 6.dp,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        if (qrBitmap != null) {
                                            Image(
                                                bitmap = qrBitmap.asImageBitmap(),
                                                contentDescription = "President Contact Exchange QR Code",
                                                modifier = Modifier
                                                    .size(220.dp)
                                                    .padding(14.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(220.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = Color(0xFF0097D8))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Scan with any smartphone camera to instantly exchange contacts with $userName",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 16.sp
                                    )
                                }
                            },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    // Navigation Drawer Sidebar (Full Screen Drawer)
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = JciColors.DeepNavy,
                                drawerContentColor = Color.White,
                                modifier = Modifier.width(300.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Header: Chapter Logo or Default LEAD360 logo
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        if (chapterLogoBitmap != null) {
                                            Image(
                                                bitmap = chapterLogoBitmap.asImageBitmap(),
                                                contentDescription = "Chapter Logo",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .padding(6.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.lead360_logo),
                                                contentDescription = "JCI LEAD360 Official Logo",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0097D8)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (userProfileBitmap != null) {
                                                Image(
                                                    bitmap = userProfileBitmap.asImageBitmap(),
                                                    contentDescription = userName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else if (!serverPhotoUrl.isNullOrBlank()) {
                                                val photoFullUrl = if (serverPhotoUrl!!.startsWith("http")) serverPhotoUrl!! else "https://lead.sriddha.com$serverPhotoUrl"
                                                AsyncImage(
                                                    model = photoFullUrl,
                                                    contentDescription = userName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            }
                                        }

                                        Column {
                                            Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(userChapter, color = Color(0xFFBAE6FD), fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }

                                Divider(color = Color(0xFF1E293B))

                                // Drawer Navigation Items
                                val drawerItems = listOf(
                                    Screen.Dashboard,
                                    Screen.Projects,
                                    Screen.Members,
                                    Screen.Resources,
                                    Screen.ScanCard,
                                    Screen.Store,
                                    Screen.Flyers,
                                    Screen.WebsitePortal,
                                    Screen.Calendar,
                                    Screen.Lgb,
                                    Screen.Frames,
                                    Screen.Vendor,
                                    Screen.Profile
                                )

                                drawerItems.forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationDrawerItem(
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(screen.icon, fontSize = 16.sp)
                                                Text(
                                                    screen.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color(0xFFBAE6FD) else Color(0xFFCBD5E1)
                                                )
                                            }
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            currentScreen = screen
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = Color(0xFF0097D8).copy(alpha = 0.2f),
                                            unselectedContainerColor = Color.Transparent
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Divider(color = Color(0xFF1E293B))

                                // Sign-out link at drawer bottom
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            prefs.edit().clear().apply()
                                            token = null
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logout),
                                        contentDescription = "Sign Out",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Sign Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { currentScreen = Screen.Dashboard }
                                        ) {
                                            // Dynamic Logo: Shows Chapter Logo if uploaded, else official Lead360 Logo
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White,
                                                shadowElevation = 2.dp
                                            ) {
                                                if (chapterLogoBitmap != null) {
                                                    Image(
                                                        bitmap = chapterLogoBitmap.asImageBitmap(),
                                                        contentDescription = userChapter,
                                                        modifier = Modifier
                                                            .height(36.dp)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.lead360_logo),
                                                        contentDescription = "LEAD360 Home",
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            coroutineScope.launch { drawerState.open() }
                                        }) {
                                            Text("☰", color = Color.White, fontSize = 22.sp)
                                        }
                                    },
                                    actions = {
                                        // Quick Access Contact Exchange QR Code Action
                                        IconButton(onClick = { showExchangeQrModal = true }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                                            listOf(Color(0xFF0097D8), Color(0xFF002F6C))
                                                        )
                                                    )
                                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_qr_code),
                                                    contentDescription = "Contact Exchange QR",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Notification Bell -> Opens BottomSheet
                                        IconButton(onClick = { showNotificationBottomSheet = true }) {
                                            Text("🔔", fontSize = 18.sp)
                                        }

                                        // Profile Avatar Shortcut (Displays actual User Photo or Server Photo if available)
                                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0097D8))
                                                    .border(1.5.dp, Color(0xFFBAE6FD), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (userProfileBitmap != null) {
                                                    Image(
                                                        bitmap = userProfileBitmap.asImageBitmap(),
                                                        contentDescription = userName,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else if (!serverPhotoUrl.isNullOrBlank()) {
                                                    val photoFullUrl = if (serverPhotoUrl!!.startsWith("http")) serverPhotoUrl!! else "https://lead.sriddha.com$serverPhotoUrl"
                                                    AsyncImage(
                                                        model = photoFullUrl,
                                                        contentDescription = userName,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                            }
                                        }

                                        // Sleek Header Logout Button (Standard exit-to-app logout vector icon)
                                        IconButton(
                                            onClick = {
                                                prefs.edit().clear().apply()
                                                token = null
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_logout),
                                                contentDescription = "Log Out",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = JciColors.DeepNavyDark
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 8.dp
                                ) {
                                    bottomNavItems.forEach { screen ->
                                        val isSelected = currentScreen == screen
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentScreen = screen },
                                            icon = {
                                                Text(
                                                    text = screen.icon,
                                                    fontSize = 18.sp
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) JciColors.RoyalBlue else Color(0xFF64748B)
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = JciColors.RoyalBlue,
                                                indicatorColor = Color(0xFFE0F2FE)
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentScreen) {
                                    Screen.Dashboard -> DashboardOverviewScreen(
                                        presidentName = userName,
                                        chapterName = userChapter,
                                        onNavigateTab = { tabId ->
                                            currentScreen = when (tabId) {
                                                "projects" -> Screen.Projects
                                                "members" -> Screen.Members
                                                "scan_card" -> Screen.ScanCard
                                                "store" -> Screen.Store
                                                "flyers" -> Screen.Flyers
                                                "website_builder" -> Screen.WebsitePortal
                                                "resources" -> Screen.Resources
                                                "calendar" -> Screen.Calendar
                                                "lgb" -> Screen.Lgb
                                                "frames" -> Screen.Frames
                                                "profile" -> Screen.Profile
                                                else -> Screen.Dashboard
                                            }
                                        }
                                    )
                                    Screen.Projects -> ProjectsAndTasksScreen(
                                        chapterName = userChapter,
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Members -> MembersAndNetworkingScreen(
                                        chapterName = userChapter,
                                        token = token,
                                        onBack = { currentScreen = Screen.Dashboard },
                                        onNavigateToScanCard = { currentScreen = Screen.ScanCard }
                                    )
                                    Screen.ScanCard -> ScanCardModuleScreen(
                                        chapterName = userChapter,
                                        token = token,
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Store -> VendorStoreScreen(
                                        chapterName = userChapter,
                                        token = token,
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Flyers -> PresidentFlyerStudioScreen(
                                        token = token!!,
                                        presidentName = userName,
                                        chapterName = userChapter,
                                        chapterLogoBitmap = chapterLogoBitmap,
                                        presidentPhotoBitmap = userProfileBitmap
                                    )
                                    Screen.WebsitePortal -> ChapterWebsiteBuilderScreen(
                                        chapterName = userChapter,
                                        presidentName = userName,
                                        onBackToHome = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Resources -> JciResourcesScreen(
                                        chapterName = userChapter,
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Calendar -> InteractiveCalendarScreen(
                                        onNavigateToFlyer = { title, date ->
                                            currentScreen = Screen.Flyers
                                        }
                                    )
                                    Screen.Lgb -> LgbAndPstManagementScreen(
                                        token = token!!,
                                        chapterName = userChapter,
                                        presidentName = userName
                                    )
                                    Screen.Frames -> FramesScreen()
                                    Screen.Vendor -> com.jci2027.lead360.vendor.VendorAppContainer(
                                        onExitVendorMode = { currentScreen = Screen.Dashboard }
                                    )
                                    Screen.Profile -> ProfileScreen(
                                        name = userName,
                                        chapterName = userChapter,
                                        role = userRole,
                                        token = token,
                                        onLogout = {
                                            prefs.edit().clear().apply()
                                            token = null
                                        },
                                        onProfileUpdated = { updatedName, updatedChapter, photoPath, logoPath ->
                                            userName = updatedName
                                            userChapter = updatedChapter
                                            userPhotoPath = photoPath
                                            chapterLogoPath = logoPath
                                            serverPhotoUrl = prefs.getString("president_photo_server_url", null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
