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
import com.radarrtv.androidtv.data.api.model.RootFolder
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RootFoldersScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<List<RootFolder>>>(UiState.Loading) }
    var newPath by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getRootFolders()) }
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
            Text("Root Folders", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        }
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newPath,
                onValueChange = { newPath = it },
                placeholder = { Text("/path/to/movies", color = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RadarrBlue, unfocusedBorderColor = RadarrBorder,
                    focusedTextColor = RadarrWhite, unfocusedTextColor = RadarrWhite,
                    focusedContainerColor = RadarrCard, unfocusedContainerColor = RadarrSurfaceVariant
                )
            )
            Spacer(Modifier.width(12.dp))
            TvFocusButton(
                onClick = {
                    if (newPath.isNotBlank()) {
                        scope.launch {
                            try { repo.addRootFolder(newPath.trim()); newPath = ""; load() }
                            catch (_: Exception) {}
                        }
                    }
                },
                isPrimary = true,
                enabled = newPath.isNotBlank()
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { load() }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyScreen("No root folders configured")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data, key = { it.id }) { folder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RadarrCard)
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(folder.path, style = MaterialTheme.typography.titleSmall, color = RadarrWhite)
                                    if (folder.freeSpace != null) {
                                        Text(
                                            "Free: ${formatBytes(folder.freeSpace)} / ${formatBytes(folder.totalSpace ?: 0)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RadarrMuted
                                        )
                                    }
                                    if (!folder.accessible) {
                                        StatusBadge("Inaccessible", RadarrRed)
                                    }
                                }
                                TvFocusButton(onClick = {
                                    scope.launch {
                                        try { repo.deleteRootFolder(folder.id); load() }
                                        catch (_: Exception) {}
                                    }
                                }, isDanger = true) {
                                    Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
