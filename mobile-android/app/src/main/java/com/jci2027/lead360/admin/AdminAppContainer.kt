package com.jci2027.lead360.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.UserDto
import com.jci2027.lead360.vendor.model.CatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class AdminTab(val title: String, val icon: String) {
    VENDORS("Vendors & Products", "🏪"),
    USERS("Leaders & Staff DB", "👥"),
    TEMPLATES("Flyer Studio", "🎨")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppContainer(
    token: String,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AdminTab.VENDORS) }

    var usersList by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var catalogItems by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load Super Admin live dataset
    fun refreshData() {
        isLoading = true
        coroutineScope.launch {
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

                    val uRes = api.getAllUsers("Bearer $token")
                    if (uRes.isSuccessful && uRes.body()?.success == true) {
                        usersList = uRes.body()!!.users
                    }

                    val cRes = api.getCatalog()
                    if (cRes.isSuccessful && cRes.body()?.success == true) {
                        catalogItems = cRes.body()!!.items
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "👑 Super Admin Control Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "Zone IV Master Administration",
                            fontSize = 11.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color(0xFFEF4444))
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
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0097D8) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = tab }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tab.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title.split(" ")[0],
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0097D8))
                }
            } else {
                when (selectedTab) {
                    AdminTab.VENDORS -> AdminVendorsView(
                        token = token,
                        vendors = usersList.filter { it.role.equals("VENDOR", ignoreCase = true) },
                        catalogItems = catalogItems,
                        onItemDeleted = { refreshData() }
                    )
                    AdminTab.USERS -> AdminUsersView(
                        users = usersList
                    )
                    AdminTab.TEMPLATES -> AdminTemplatesView()
                }
            }
        }
    }
}

@Composable
fun AdminVendorsView(
    token: String,
    vendors: List<UserDto>,
    catalogItems: List<CatalogItem>,
    onItemDeleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Vendors Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Official Approved Vendors (${vendors.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (vendors.isEmpty()) {
                        Text("No vendors registered in database", color = Color(0xFF64748B), fontSize = 13.sp)
                    } else {
                        vendors.forEach { v ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(v.businessName ?: v.name ?: "Official Vendor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(v.email ?: "", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text("✓ Approved", color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Product Catalog Moderation (${catalogItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
        }

        items(catalogItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${item.category} • ₹${String.format("%,.0f", item.price)}", fontSize = 12.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.SemiBold)
                        Text("Lead Time: ${item.leadTime}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val client = OkHttpClient.Builder().build()
                                    val retrofit = Retrofit.Builder()
                                        .baseUrl("https://lead.sriddha.com")
                                        .client(client)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
                                    val api = retrofit.create(ApiService::class.java)
                                    val res = api.deleteCatalogItem("Bearer $token", item.id)
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                        onItemDeleted()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersView(
    users: List<UserDto>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = users.filter {
        val q = searchQuery.lowercase().trim()
        (it.name?.lowercase()?.contains(q) == true) ||
        (it.chapterName?.lowercase()?.contains(q) == true) ||
        (it.role?.lowercase()?.contains(q) == true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("🔍 Search by name, chapter, or role...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(u.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (u.role?.uppercase()) {
                                    "SUPER_ADMIN" -> Color(0xFFFEE2E2)
                                    "VENDOR" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFE0F2FE)
                                }
                            ) {
                                Text(
                                    u.role ?: "PRESIDENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (u.role?.uppercase()) {
                                        "SUPER_ADMIN" -> Color(0xFFB91C1C)
                                        "VENDOR" -> Color(0xFF92400E)
                                        else -> Color(0xFF0369A1)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(u.chapterName ?: "JCI Chapter", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(u.email ?: "", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTemplatesView() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎨", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Official Flyer & Frame Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Templates managed on web portal are synchronized live with Android devices.", color = Color(0xFF64748B), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
