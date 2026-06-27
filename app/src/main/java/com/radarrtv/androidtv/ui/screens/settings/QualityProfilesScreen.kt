package com.radarrtv.androidtv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.QualityProfile
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QualityProfilesScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<List<QualityProfile>>>(UiState.Loading) }
    var deleteTarget by remember { mutableStateOf<QualityProfile?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getQualityProfiles()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TvFocusButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back")
            }
            Spacer(Modifier.width(16.dp))
            Text("Quality Profiles", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        }
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { load() }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyScreen("No quality profiles")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data, key = { it.id }) { profile ->
                            QualityProfileRow(
                                profile = profile,
                                onDelete = { deleteTarget = profile }
                            )
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Profile?", color = RadarrWhite) },
            text = { Text("Delete \"${profile.name}\"? This cannot be undone.", color = RadarrWhite) },
            confirmButton = {
                TvFocusButton(onClick = {
                    scope.launch {
                        try { repo.deleteQualityProfile(profile.id); load() }
                        catch (_: Exception) {}
                        deleteTarget = null
                    }
                }, isDanger = true) { Text("Delete") }
            },
            dismissButton = {
                TvFocusButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
            containerColor = RadarrSurface
        )
    }
}

@Composable
private fun QualityProfileRow(profile: QualityProfile, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.name, style = MaterialTheme.typography.titleSmall, color = RadarrWhite)
            val allowedQualities = profile.items
                .filter { it.allowed }
                .mapNotNull { it.quality?.name ?: it.name }
                .take(5)
                .joinToString(", ")
            if (allowedQualities.isNotEmpty()) {
                Text(allowedQualities, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
            }
            if (profile.upgradeAllowed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = RadarrBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Upgrades allowed", style = MaterialTheme.typography.bodyMedium, color = RadarrBlue)
                }
            }
        }
        TvFocusButton(onClick = onDelete, isDanger = true) {
            Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
        }
    }
}
