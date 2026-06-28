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
        // ADB config injection: adb shell am start -n .../.MainActivity --es server_url "http://..." --es api_key "..." --es tmdb_api_key "..."
        intent?.getStringExtra("server_url")?.takeIf { it.isNotBlank() }?.let { prefs.serverUrl = it }
        intent?.getStringExtra("api_key")?.takeIf { it.isNotBlank() }?.let { prefs.apiKey = it }
        intent?.getStringExtra("tmdb_api_key")?.takeIf { it.isNotBlank() }?.let { prefs.tmdbApiKey = it }
        setContent {
            RadarrTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RadarrNavHost(prefs = prefs)
                }
            }
        }
    }
}
