package com.jci2027.lead360.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChapterWebsiteBuilderScreen(
    chapterName: String,
    presidentName: String,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current

    // State for builder settings
    val templates = listOf(
        WebsiteTemplate("tpl_modern_presidential", "🏛️ Presidential Prestige", "Bold Navy & Gold hero banner, leader profile, instant WhatsApp RSVP, and photo gallery.", Color(0xFF091322)),
        WebsiteTemplate("tpl_dynamic_community", "🌍 Impact & Projects", "Live impact stats counter, active projects tracker, and new member induction form.", Color(0xFF0284C7)),
        WebsiteTemplate("tpl_business_networking", "💼 Chamber Networking", "Member business showcase, B2B directory, sponsor tiers, and direct meeting booking.", Color(0xFF0F766E))
    )
    var selectedTemplate by remember { mutableStateOf(templates.first()) }

    var domainSlug by remember {
        mutableStateOf(
            chapterName.lowercase()
                .replace("jci", "")
                .trim()
                .replace(" ", "-")
                .ifBlank { "my-chapter" }
        )
    }

    var enableAutoPosting by remember { mutableStateOf(true) }
    var enableWhatsAppApi by remember { mutableStateOf(true) }
    var enableMemberPortal by remember { mutableStateOf(true) }
    var whatsappNumber by remember { mutableStateOf("+91 98765 43210") }
    var isPublished by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x330097D8)
                    ) {
                        Text(
                            text = "OFFICIAL CHAPTER WEB SUITE",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3310B981)
                    ) {
                        Text(
                            text = "PREMIUM SUITE",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Launch Chapter Website & Member Portal",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Deploy a dedicated web portal for $chapterName with automated flyers sync, WhatsApp API dispatch, and private member logins.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Active Live Domain Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔗", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "https://$domainSlug.jci2027.org",
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 1: Template Selection
        Text(
            text = "1. CHOOSE YOUR CHAPTER THEME TEMPLATE",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF475569)
        )

        templates.forEach { tpl ->
            val isSelected = selectedTemplate.id == tpl.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTemplate = tpl },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (isSelected) Color(0xFF0097D8) else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(tpl.themeColor)
                            )
                            Text(tpl.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }

                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text("✓ Selected", color = Color(0xFF0284C7), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(tpl.description, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }

        // Section 2: Integrations & Powerful Auto-Features
        Text(
            text = "2. AUTOMATED INTEGRATIONS & REVENUE SUITE",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF475569)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Auto-Post Creative Integration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Publish Studio Flyers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Every flyer generated in Flyer Studio instantly updates on the website event section.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = enableAutoPosting,
                        onCheckedChange = { enableAutoPosting = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0097D8))
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // WhatsApp Cloud API Integration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Official WhatsApp Cloud Broadcast", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Automatic event reminder & RSVP bot for registered members.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = enableWhatsAppApi,
                        onCheckedChange = { enableWhatsAppApi = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0097D8))
                    )
                }

                if (enableWhatsAppApi) {
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("Sender WhatsApp Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Member Login Portal & Monetization Model
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dedicated Member Login Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Free access for President, Secretary & Treasurer. Member portal access enabled via chapter subscription.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = enableMemberPortal,
                        onCheckedChange = { enableMemberPortal = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0097D8))
                    )
                }
            }
        }

        // Section 3: Domain Customization
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌐 Custom Subdomain Slug", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Your website will be instantly live on HTTPS with an automatic SSL certificate.", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp))

                OutlinedTextField(
                    value = domainSlug,
                    onValueChange = { domainSlug = it.trim().lowercase() },
                    label = { Text("Subdomain name") },
                    prefix = { Text("https://", color = Color(0xFF64748B)) },
                    suffix = { Text(".jci2027.org", color = Color(0xFF0097D8), fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Action Button: Deploy / Update Live Site
        Button(
            onClick = {
                isPublished = true
                Toast.makeText(context, "🎉 Website updated & live at https://$domainSlug.jci2027.org", Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isPublished) "✓ Website Live & Synchronized" else "🚀 Publish Chapter Website Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

data class WebsiteTemplate(
    val id: String,
    val title: String,
    val description: String,
    val themeColor: Color
)

