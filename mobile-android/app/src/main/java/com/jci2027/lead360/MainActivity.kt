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
import com.jci2027.lead360.screens.AiFlyerStudioScreen
import com.jci2027.lead360.screens.DashboardOverviewScreen
import com.jci2027.lead360.screens.FramesScreen
import com.jci2027.lead360.screens.InteractiveCalendarScreen
import com.jci2027.lead360.screens.InternalPhotoStore
import com.jci2027.lead360.screens.LgbAndPstManagementScreen
import com.jci2027.lead360.screens.NotificationCenterBottomSheet
import com.jci2027.lead360.screens.ProfileScreen
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
                            chapterLogoPath = prefs.getString("chapter_logo_path", null)
                        }
                    )
                } else {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                    var showNotificationBottomSheet by remember { mutableStateOf(false) }

                    // Navigation items for BottomBar
                    val bottomNavItems = listOf(
                        Screen.Dashboard,
                        Screen.Flyers,
                        Screen.AiFlyer,
                        Screen.Calendar,
                        Screen.Lgb
                    )

                    // Modal BottomSheet Notification Center
                    if (showNotificationBottomSheet) {
                        NotificationCenterBottomSheet(
                            onDismiss = { showNotificationBottomSheet = false }
                        )
                    }

                    // Navigation Drawer Sidebar (Full Screen Drawer)
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = Color(0xFF091322),
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
                                    Screen.Flyers,
                                    Screen.AiFlyer,
                                    Screen.Calendar,
                                    Screen.Lgb,
                                    Screen.Frames,
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
                                        // Notification Bell -> Opens BottomSheet
                                        IconButton(onClick = { showNotificationBottomSheet = true }) {
                                            Text("🔔", fontSize = 18.sp)
                                        }

                                        // Profile Avatar Shortcut (Displays actual User Photo if available)
                                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
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
                                        containerColor = Color(0xFF091322)
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
                                                    color = if (isSelected) Color(0xFF0097D8) else Color(0xFF64748B)
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFF0097D8),
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
                                        userName = userName,
                                        chapterName = userChapter,
                                        onNavigateTab = { tabId ->
                                            currentScreen = when (tabId) {
                                                "flyers" -> Screen.Flyers
                                                "ai_flyer" -> Screen.AiFlyer
                                                "calendar" -> Screen.Calendar
                                                "lgb" -> Screen.Lgb
                                                "frames" -> Screen.Frames
                                                "profile" -> Screen.Profile
                                                else -> Screen.Dashboard
                                            }
                                        }
                                    )
                                    Screen.Flyers -> PresidentFlyerStudioScreen(
                                        token = token!!,
                                        presidentName = userName,
                                        chapterName = userChapter
                                    )
                                    Screen.AiFlyer -> AiFlyerStudioScreen(
                                        presidentName = userName,
                                        chapterName = userChapter
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
                                    Screen.Profile -> ProfileScreen(
                                        name = userName,
                                        chapterName = userChapter,
                                        role = userRole,
                                        onLogout = {
                                            prefs.edit().clear().apply()
                                            token = null
                                        },
                                        onProfileUpdated = { updatedName, updatedChapter, photoPath, logoPath ->
                                            userName = updatedName
                                            userChapter = updatedChapter
                                            userPhotoPath = photoPath
                                            chapterLogoPath = logoPath
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
