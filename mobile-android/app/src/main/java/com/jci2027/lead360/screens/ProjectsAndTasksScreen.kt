package com.jci2027.lead360.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class ProjectItem(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val venue: String,
    val chair: String,
    val progress: Float,
    val status: String,
    val tasksCount: Int,
    val budget: String
)

data class TaskItem(
    val id: String,
    val title: String,
    val assignee: String,
    val dueDate: String,
    val priority: String,
    val isDone: Boolean,
    val projectTitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsAndTasksScreen(
    chapterName: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Projects, 1: Tasks
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }

    val projects = remember {
        mutableStateListOf(
            ProjectItem("p1", "JCI Youth Leadership Conclave 2027", "Training & Empowerment", "24 Jan 2027", "SV University Auditorium", "JC Rahul Reddy", 0.75f, "In Progress", 8, "₹45,000"),
            ProjectItem("p2", "Mega Health & Blood Donation Camp", "Community Impact", "08 Feb 2027", "Red Cross Bhavan", "JC Priya Sharma", 0.40f, "Planning", 5, "₹20,000"),
            ProjectItem("p3", "Business Networking & B2B Expo", "Business & Commerce", "22 Feb 2027", "Hotel Bliss Royal Hall", "JC Vikram Verma", 0.20f, "Planning", 6, "₹75,000"),
            ProjectItem("p4", "Save Water Green Tree Drive", "Eco & Environment", "12 Mar 2027", "Tirupati City Circle", "JC Sneha Patel", 0.90f, "Near Finish", 4, "₹15,000")
        )
    }

    val tasks = remember {
        mutableStateListOf(
            TaskItem("t1", "Finalize Keynote Speaker hospitality & travel", "JC Rahul Reddy", "18 Jan", "HIGH", false, "Youth Conclave"),
            TaskItem("t2", "Dispatch WhatsApp invitations & RSVP link", "JC Secretary", "19 Jan", "URGENT", false, "Youth Conclave"),
            TaskItem("t3", "Print Backdrop Banner & Stage Standees", "JC Kiran (Treasurer)", "20 Jan", "MEDIUM", true, "Youth Conclave"),
            TaskItem("t4", "Secure Blood Bank mobile van clearance", "JC Priya Sharma", "25 Jan", "HIGH", false, "Blood Donation Camp"),
            TaskItem("t5", "Book Expo Venue deposit with receipt upload", "JC Treasurer", "28 Jan", "HIGH", false, "B2B Expo"),
            TaskItem("t6", "Order 150 eco plant saplings", "JC Sneha Patel", "05 Mar", "LOW", false, "Green Tree Drive")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Projects & Event Workspace", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                        Text(chapterName, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (selectedTab == 0) showCreateProjectDialog = true else showCreateTaskDialog = true
                    }) {
                        Text(if (selectedTab == 0) "+ New Project" else "+ New Task", fontWeight = FontWeight.Bold, color = Color(0xFF0097D8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0097D8)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🚀 Projects (${projects.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("✅ Tasks (${tasks.count { !it.isDone }} Open)", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (selectedTab == 0) {
                // PROJECTS LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(projects) { project ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            project.category,
                                            color = Color(0xFF0284C7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = if (project.status == "In Progress") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            project.status,
                                            color = if (project.status == "In Progress") Color(0xFF16A34A) else Color(0xFFD97706),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Text(project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Spacer(Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📅 ${project.date}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Spacer(Modifier.width(12.dp))
                                    Text("📍 ${project.venue}", fontSize = 12.sp, color = Color(0xFF64748B))
                                }

                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Lead: ${project.chair}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                    Text("Budget: ${project.budget}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }

                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = project.progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF0097D8),
                                    trackColor = Color(0xFFF1F5F9)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${(project.progress * 100).toInt()}% Completed", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text("${project.tasksCount} Tasks", fontSize = 11.sp, color = Color(0xFF0097D8), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // TASKS LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks) { task ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { checked ->
                                        val idx = tasks.indexOf(task)
                                        if (idx != -1) {
                                            tasks[idx] = task.copy(isDone = checked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0097D8))
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        task.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (task.isDone) Color(0xFF94A3B8) else Color(0xFF0F172A)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("👤 ${task.assignee}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(Modifier.width(10.dp))
                                        Text("⏳ Due ${task.dueDate}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(Modifier.width(10.dp))
                                        Text("📂 ${task.projectTitle}", fontSize = 11.sp, color = Color(0xFF0284C7))
                                    }
                                }
                                Surface(
                                    color = when (task.priority) {
                                        "URGENT" -> Color(0xFFFEE2E2)
                                        "HIGH" -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        task.priority,
                                        color = when (task.priority) {
                                            "URGENT" -> Color(0xFFDC2626)
                                            "HIGH" -> Color(0xFFD97706)
                                            else -> Color(0xFF64748B)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateProjectDialog) {
        var pTitle by remember { mutableStateOf("") }
        var pCategory by remember { mutableStateOf("Community Impact") }
        var pDate by remember { mutableStateOf("") }
        var pLead by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = { Text("Create Chapter Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = pTitle, onValueChange = { pTitle = it }, label = { Text("Project Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCategory, onValueChange = { pCategory = it }, label = { Text("Category (e.g. Training, Health, B2B)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDate, onValueChange = { pDate = it }, label = { Text("Date & Venue") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pLead, onValueChange = { pLead = it }, label = { Text("Project Chairman / Lead") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pTitle.isNotBlank()) {
                        projects.add(0, ProjectItem("p${System.currentTimeMillis()}", pTitle, pCategory, if (pDate.isNotBlank()) pDate else "TBD", "Main Hall", if (pLead.isNotBlank()) pLead else "JC Officer", 0.1f, "Planning", 2, "₹10,000"))
                        Toast.makeText(context, "Project Created!", Toast.LENGTH_SHORT).show()
                        showCreateProjectDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateProjectDialog = false }) { Text("Cancel") } }
        )
    }

    // New Task Dialog
    if (showCreateTaskDialog) {
        var tTitle by remember { mutableStateOf("") }
        var tAssignee by remember { mutableStateOf("") }
        var tDue by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = { Text("Add Project Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = tTitle, onValueChange = { tTitle = it }, label = { Text("Task Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tAssignee, onValueChange = { tAssignee = it }, label = { Text("Assignee (Officer/Member)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tDue, onValueChange = { tDue = it }, label = { Text("Due Date (e.g. 20 Jan)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (tTitle.isNotBlank()) {
                        tasks.add(0, TaskItem("t${System.currentTimeMillis()}", tTitle, if (tAssignee.isNotBlank()) tAssignee else "Me", if (tDue.isNotBlank()) tDue else "Today", "HIGH", false, "General"))
                        Toast.makeText(context, "Task Assigned!", Toast.LENGTH_SHORT).show()
                        showCreateTaskDialog = false
                    }
                }) { Text("Add Task") }
            },
            dismissButton = { TextButton(onClick = { showCreateTaskDialog = false }) { Text("Cancel") } }
        )
    }
}

