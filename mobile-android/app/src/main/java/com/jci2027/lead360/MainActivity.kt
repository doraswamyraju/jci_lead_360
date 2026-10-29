package com.jci2027.lead360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jci2027.lead360.auth.LoginScreen
import com.jci2027.lead360.navigation.Screen
import com.jci2027.lead360.president.PresidentFlyerStudioScreen
import com.jci2027.lead360.screens.DashboardOverviewScreen
import com.jci2027.lead360.screens.FramesScreen
import com.jci2027.lead360.screens.InteractiveCalendarScreen
import com.jci2027.lead360.screens.LgbAndPstManagementScreen
import com.jci2027.lead360.screens.NotificationCenterModal
import com.jci2027.lead360.screens.ProfileScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var token by remember { mutableStateOf<String?>(null) }
                var userRole by remember { mutableStateOf("PRESIDENT") }
                var userName by remember { mutableStateOf("JC Doraswamy Raju") }
                var userChapter by remember { mutableStateOf("JCI Tirupati Innovations") }

                if (token == null) {
                    LoginScreen(
                        onLoginSuccess = { t, role, name, chapter ->
                            token = t
                            userRole = role
                            userName = name
                            userChapter = chapter
                        }
                    )
                } else {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                    var showNotificationModal by remember { mutableStateOf(false) }

                    // Navigation items for BottomBar & Drawer
                    val bottomNavItems = listOf(
                        Screen.Dashboard,
                        Screen.Flyers,
                        Screen.Calendar,
                        Screen.Lgb,
                        Screen.Frames
                    )

                    // Notification Modal
                    if (showNotificationModal) {
                        Dialog(onDismissRequest = { showNotificationModal = false }) {
                            NotificationCenterModal(onClose = { showNotificationModal = false })
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(300.dp),
                                drawerContainerColor = Color(0xFF091322)
                            ) {
                                // Drawer Header with JCI LEAD360 Official Logo Pill
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A))
                                        .padding(20.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 14.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.lead360_logo),
                                            contentDescription = "JCI LEAD360 Logo",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .padding(6.dp)
                                        )
                                    }
                                    Text(
                                        text = userName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = userChapter,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x330097D8)
                                    ) {
                                        Text(
                                            text = "YEAR 2027 • CHAPTER PRESIDENT",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Drawer Items
                                val drawerItems = listOf(
                                    Screen.Dashboard,
                                    Screen.Flyers,
                                    Screen.Calendar,
                                    Screen.Lgb,
                                    Screen.Frames,
                                    Screen.Profile
                                )

                                drawerItems.forEach { screen ->
                                    val isSelected = currentScreen.id == screen.id
                                    NavigationDrawerItem(
                                        icon = { Text(screen.icon, fontSize = 18.sp) },
                                        label = { Text(screen.title, fontWeight = FontWeight.SemiBold) },
                                        selected = isSelected,
                                        onClick = {
                                            currentScreen = screen
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = Color(0xFF0097D8),
                                            selectedTextColor = Color.White,
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Sign Out
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            token = null
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🚪", fontSize = 16.sp)
                                    Text("Sign Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                modifier = Modifier.padding(end = 10.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.lead360_logo),
                                                    contentDescription = "Logo",
                                                    modifier = Modifier.height(28.dp).padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "JCI LEAD360",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = userChapter,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFBAE6FD),
                                                    maxLines = 1
                                                )
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
                                        // Notification Bell
                                        IconButton(onClick = { showNotificationModal = true }) {
                                            Text("🔔", fontSize = 18.sp)
                                        }
                                        // Profile Avatar
                                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color(0xFF0097D8), RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
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
                                        val isSelected = currentScreen.id == screen.id
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentScreen = screen },
                                            icon = { Text(screen.icon, fontSize = 18.sp) },
                                            label = {
                                                Text(
                                                    screen.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFF0097D8),
                                                selectedTextColor = Color(0xFF0097D8),
                                                indicatorColor = Color(0xFFE0F2FE),
                                                unselectedIconColor = Color(0xFF64748B),
                                                unselectedTextColor = Color(0xFF64748B)
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentScreen) {
                                    Screen.Dashboard -> DashboardOverviewScreen(
                                        userName = userName,
                                        chapterName = userChapter,
                                        onNavigateTab = { tabId ->
                                            currentScreen = when (tabId) {
                                                "flyers" -> Screen.Flyers
                                                "calendar" -> Screen.Calendar
                                                "lgb" -> Screen.Lgb
                                                "frames" -> Screen.Frames
                                                else -> Screen.Dashboard
                                            }
                                        }
                                    )
                                    Screen.Flyers -> PresidentFlyerStudioScreen(
                                        token = token!!,
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
                                        onLogout = { token = null }
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
