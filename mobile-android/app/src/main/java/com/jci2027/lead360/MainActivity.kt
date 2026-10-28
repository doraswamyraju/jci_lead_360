package com.jci2027.lead360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.jci2027.lead360.auth.LoginScreen
import com.jci2027.lead360.president.PresidentFlyerStudioScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                        PresidentFlyerStudioScreen(
                            token = token!!,
                            presidentName = userName,
                            chapterName = userChapter,
                            onLogout = {
                                token = null
                            }
                        )
                    }
                }
            }
        }
    }
}
