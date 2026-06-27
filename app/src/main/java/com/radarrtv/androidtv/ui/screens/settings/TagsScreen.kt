package com.radarrtv.androidtv.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.Tag
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TagsScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<List<Tag>>>(UiState.Loading) }
    var newTagLabel by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getTags()) }
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
            Text("Tags", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        }
        Spacer(Modifier.height(20.dp))

        // Add tag row
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTagLabel,
                onValueChange = { newTagLabel = it },
                placeholder = { Text("New tag label…", color = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RadarrBlue,
                    unfocusedBorderColor = RadarrBorder,
                    focusedTextColor = RadarrWhite,
                    unfocusedTextColor = RadarrWhite,
                    focusedContainerColor = RadarrCard,
                    unfocusedContainerColor = RadarrSurfaceVariant
                )
            )
            Spacer(Modifier.width(12.dp))
            TvFocusButton(
                onClick = {
                    if (newTagLabel.isNotBlank()) {
                        scope.launch {
                            try { repo.createTag(newTagLabel.trim()); newTagLabel = ""; load() }
                            catch (_: Exception) {}
                        }
                    }
                },
                isPrimary = true,
                enabled = newTagLabel.isNotBlank()
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
                    EmptyScreen("No tags defined")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(s.data, key = { it.id }) { tag ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatusBadge(tag.label, RadarrBlueDark, modifier = Modifier.weight(1f).padding(end = 12.dp))
                                TvFocusButton(onClick = {
                                    scope.launch {
                                        try { repo.deleteTag(tag.id); load() }
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
