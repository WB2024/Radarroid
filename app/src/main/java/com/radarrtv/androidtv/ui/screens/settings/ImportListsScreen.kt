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
import com.radarrtv.androidtv.data.api.model.ImportList
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ImportListsScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<List<ImportList>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getImportLists()) }
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
            Text("Import Lists", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        }
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { load() }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyScreen("No import lists configured")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data, key = { it.id }) { list ->
                            ImportListRow(
                                importList = list,
                                onDelete = {
                                    scope.launch {
                                        try { repo.deleteImportList(list.id); load() }
                                        catch (_: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportListRow(importList: ImportList, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(importList.name, style = MaterialTheme.typography.titleSmall, color = RadarrWhite)
                Spacer(Modifier.width(8.dp))
                if (importList.enabled) StatusBadge("Enabled", RadarrGreen) else StatusBadge("Disabled", RadarrMuted)
            }
            Text(importList.implementationName, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
            Text(
                "Root: ${importList.rootFolderPath.ifBlank { "—" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
        TvFocusButton(onClick = onDelete, isDanger = true) {
            Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
        }
    }
}
