package com.jci2027.lead360.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.network.ApiService
import com.jci2027.lead360.network.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Real Production Android Login Screen
 * Authenticates with live Hostinger backend (https://lead.sriddha.com)
 * Handles token generation, role verification, and error feedback.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (token: String, role: String, name: String, chapterName: String) -> Unit,
    baseUrl: String = "https://lead.sriddha.com"
) {
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF091428), Color(0xFF0097D8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "JCI LEAD360",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "President & PST Command Portal • 2027",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 22.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email or Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter your email and password"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                                val client = OkHttpClient.Builder().addInterceptor(logging).build()
                                val retrofit = Retrofit.Builder()
                                    .baseUrl(baseUrl)
                                    .client(client)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

                                val api = retrofit.create(ApiService::class.java)
                                val response = withContext(Dispatchers.IO) {
                                    api.login(LoginRequest(email.trim(), password))
                                }

                                if (response.isSuccessful && response.body()?.success == true) {
                                    val body = response.body()!!
                                    val token = body.token ?: ""
                                    val user = body.user
                                    val role = user?.role ?: "PRESIDENT"
                                    val name = user?.name ?: "Chapter President"
                                    val chapter = user?.chapterName ?: "JCI Chapter"

                                    isLoading = false
                                    onLoginSuccess(token, role, name, chapter)
                                } else {
                                    isLoading = false
                                    errorMessage = response.body()?.message ?: "Invalid email or password. Status: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Network Error: ${e.localizedMessage ?: "Unable to connect to lead.sriddha.com"}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097D8)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign In to Command Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
