package com.radarrtv.androidtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.ui.navigation.RadarrNavHost
import com.radarrtv.androidtv.ui.theme.RadarrTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = UserPreferences(applicationContext)
        setContent {
            RadarrTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RadarrNavHost(prefs = prefs)
                }
            }
        }
    }
}
