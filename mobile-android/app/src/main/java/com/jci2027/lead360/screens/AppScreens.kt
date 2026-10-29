package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FramesScreen() {
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
                        .height(220.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️ Select a Photo Frame to preview", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun LgbScreen(chapterName: String) {
    val lgbMembers = listOf(
        Triple("President", "JC Doraswamy Raju", "+91 98765 43210"),
        Triple("Secretary", "JC Secretary Member", "+91 98765 43211"),
        Triple("Treasurer", "JC Treasurer Member", "+91 98765 43212"),
        Triple("VP Management", "JC Vice President 1", "+91 98765 43213"),
        Triple("VP Community", "JC Vice President 2", "+91 98765 43214"),
        Triple("VP Individual", "JC Vice President 3", "+91 98765 43215"),
        Triple("VP International", "JC Vice President 4", "+91 98765 43216")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("👥 Local Governing Board (LGB 2027)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Text("Executive Board of $chapterName", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        lgbMembers.forEach { (designation, name, phone) ->
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
                        Text(designation, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(phone, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = { /* Call/WhatsApp */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Connect", color = Color(0xFF0369A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(onEventSelect: (String) -> Unit = {}) {
    val events = listOf(
        Triple("15th Jan 2027", "1st LOM General Body Meeting", "Mandatory 100% Efficiency"),
        Triple("26th Jan 2027", "Republic Day Community Outreach", "National Flag Day"),
        Triple("14th Feb 2027", "Chapter Training & Development", "Zone Development Program"),
        Triple("08th Mar 2027", "International Women's Day Program", "Global UN Day"),
        Triple("15th Apr 2027", "2nd LOM Board Meeting", "Mandatory Compliance")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📅 JCI 2027 Official Calendar", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Text("100% Efficiency milestones & special international days", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        events.forEach { (date, title, category) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF37023))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    name: String,
    chapterName: String,
    role: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF0097D8), RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.toString() ?: "J",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Text(chapterName, fontSize = 13.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Sign Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
        }
    }
}

