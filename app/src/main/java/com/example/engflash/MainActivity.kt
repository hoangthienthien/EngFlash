package com.example.engflash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import com.example.engflash.ui.navigation.NavGraph
import com.example.engflash.ui.navigation.Routes
import com.example.engflash.ui.theme.EngFlashTheme
import com.google.firebase.auth.FirebaseAuth

import com.example.engflash.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NotificationHelper.createNotificationChannel(this)
        
        enableEdgeToEdge()
        setContent {
            val app = application as EngFlashApplication
            val themeMode by app.themeManager.themeMode.collectAsState()
            val isDark = themeMode ?: isSystemInDarkTheme()

            EngFlashTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val prefs = getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
                    val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)

                    val startDestination = if (!hasSeenOnboarding) {
                        Routes.ONBOARDING
                    } else if (FirebaseAuth.getInstance().currentUser != null) {
                        Routes.HOME
                    } else {
                        Routes.LOGIN
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}