package com.jci2027.lead360.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CalendarEventItem(
    val day: Int,
    val title: String,
    val category: String,
    val fullDate: String,
    val hasFlyer: Boolean = true,
    val hasInvitation: Boolean = false,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveCalendarScreen(
    onNavigateToFlyer: (title: String, date: String) -> Unit
) {
    val currentMonthName = "January 2027"
    var selectedDay by remember { mutableStateOf(15) }

    val monthEvents = remember {
        mapOf(
            15 to listOf(
                CalendarEventItem(
                    day = 15,
                    title = "1st LOM General Body Meeting & Board Meeting",
                    category = "⭐ 100% Efficiency",
                    fullDate = "Friday, 15th January 2027 • 6:30 PM",
                    hasFlyer = true,
                    hasInvitation = true,
                    description = "Mandatory adoption of Chapter Plan of Action, Budget 2027, and committee appointments."
                )
            ),
            26 to listOf(
                CalendarEventItem(
                    day = 26,
                    title = "Republic Day Flag Hoisting & Community Outreach",
                    category = "🌍 Special National Day",
                    fullDate = "Tuesday, 26th January 2027 • 8:30 AM",
                    hasFlyer = true,
                    hasInvitation = false,
                    description = "Flag salutation, fellowship breakfast, and community service project."
                )
            ),
            31 to listOf(
                CalendarEventItem(
                    day = 31,
                    title = "Submission of 1st Month Efficiency Report",
                    category = "🏛️ Compliance Deadline",
                    fullDate = "Sunday, 31st January 2027 • 11:59 PM",
                    hasFlyer = false,
                    hasInvitation = false,
                    description = "Official reporting on JCI India national portal for 100% Efficiency points."
                )
            )
        )
    }

    val selectedDayEvents = monthEvents[selectedDay] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Month Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📅 Interactive Year Calendar", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(currentMonthName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x330097D8)
                ) {
                    Text("JCI 2027", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        // Monthly Interactive Grid (1 to 31)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Day of Week Headers
                val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { d ->
                        Text(
                            text = d,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid (31 Days)
                val daysList = (1..31).toList()
                // Let's render in chunks of 7 for clean rows
                daysList.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { dayNumber ->
                            val isSelected = (dayNumber == selectedDay)
                            val hasEvent = monthEvents.containsKey(dayNumber)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF0097D8)
                                            hasEvent -> Color(0xFFE0F2FE)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { selectedDay = dayNumber },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || hasEvent) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            hasEvent -> Color(0xFF0284C7)
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                    if (hasEvent && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(Color(0xFFF37023), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                        // Pad last week if under 7
                        val remaining = 7 - week.size
                        repeat(remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Details for Selected Date
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "PROGRAMS FOR DAY $selectedDay",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF475569)
            )

            if (selectedDayEvents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🍃 No scheduled mandatory events", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("Tap day 15, 26, or 31 to inspect official calendar milestones", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            } else {
                selectedDayEvents.forEach { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(event.category, color = Color(0xFF92400E), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                                Text("Day $selectedDay", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            Text(event.fullDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0284C7))
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(event.description, fontSize = 12.sp, color = Color(0xFF475569))

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1-Tap Action: Open Pre-filled Flyer or Invitation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (event.hasFlyer) {
                                    Button(
                                        onClick = {
                                            onNavigateToFlyer(event.title, event.fullDate)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("⚡ Open 1-Tap Flyer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                if (event.hasInvitation) {
                                    Button(
                                        onClick = {
                                            onNavigateToFlyer("Official Invitation: ${event.title}", event.fullDate)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💌 Invitation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
