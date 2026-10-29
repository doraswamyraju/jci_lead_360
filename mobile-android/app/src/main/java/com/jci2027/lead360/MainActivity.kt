package com.jci2027.lead360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.auth.LoginScreen
import com.jci2027.lead360.navigation.Screen
import com.jci2027.lead360.president.PresidentFlyerStudioScreen
import com.jci2027.lead360.screens.CalendarScreen
import com.jci2027.lead360.screens.FramesScreen
import com.jci2027.lead360.screens.LgbScreen
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
                var userName by remember { mutableStateOf("JC President") }
                var userChapter by remember { mutableStateOf("JCI Chapter") }

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
                    // Full App Shell with Drawer, TopBar, and BottomBar
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Flyers) }

                    val bottomNavItems = listOf(
                        Screen.Flyers,
                        Screen.Frames,
                        Screen.Lgb,
                        Screen.Calendar,
                        Screen.Profile
                    )

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(300.dp),
                                drawerContainerColor = Color(0xFF091322)
                            ) {
                                // Drawer Header
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A))
                                        .padding(20.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Text(
                                            text = " JCI LEAD360 ",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0097D8),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x330097D8)
                                    ) {
                                        Text(
                                            text = "YEAR 2027",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Drawer Items
                                bottomNavItems.forEach { screen ->
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
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Drawer Footer Logout
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
                                        Column {
                                            Text(
                                                text = "JCI LEAD360",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "$userChapter • 2027",
                                                fontSize = 11.sp,
                                                color = Color(0xFFBAE6FD)
                                            )
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
                                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color(0xFF0097D8), RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                    Screen.Flyers -> PresidentFlyerStudioScreen(
                                        token = token!!,
                                        presidentName = userName,
                                        chapterName = userChapter
                                    )
                                    Screen.Frames -> FramesScreen()
                                    Screen.Lgb -> LgbScreen(chapterName = userChapter)
                                    Screen.Calendar -> CalendarScreen(
                                        onEventSelect = {
                                            currentScreen = Screen.Flyers
                                        }
                                    )
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
