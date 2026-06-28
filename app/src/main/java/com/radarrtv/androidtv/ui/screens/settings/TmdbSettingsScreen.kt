package com.radarrtv.androidtv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*

@Composable
fun TmdbSettingsScreen(prefs: UserPreferences, onBack: () -> Unit) {
    var apiKey by remember { mutableStateOf(prefs.tmdbApiKey) }
    var saved by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TvFocusButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back")
            }
            Spacer(Modifier.width(16.dp))
            Text("TMDB Settings", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
            Spacer(Modifier.weight(1f))
            TvFocusButton(
                onClick = {
                    prefs.tmdbApiKey = apiKey.trim()
                    saved = true
                },
                isPrimary = true,
                enabled = apiKey.trim() != prefs.tmdbApiKey || !saved
            ) {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (saved) "Saved" else "Save")
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RadarrCard)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("The Movie Database (TMDB)", style = MaterialTheme.typography.titleSmall, color = RadarrBlue)

            Text(
                "Radarr uses TMDB as its primary metadata source. Providing a TMDB API key " +
                "enables Radarroid to fetch additional data such as trailers, cast, crew, and " +
                "extended movie details directly from TMDB.",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )

            TvTextField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    saved = false
                },
                label = "TMDB API Key (v3 auth)",
                placeholder = "Enter your TMDB API read access token…",
                isPassword = true
            )

            if (apiKey.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = RadarrGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "API key configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RadarrGreen
                    )
                }
            }
        }
    }
}
