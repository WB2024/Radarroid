package com.radarrtv.androidtv.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityScreen(repo: RadarrRepository) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Queue", "History", "Blocklist")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Activity", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = RadarrSurfaceVariant,
            contentColor = RadarrBlue
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, color = if (selectedTab == idx) RadarrBlue else RadarrMuted) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> QueueTab(repo)
            1 -> HistoryTab(repo)
            2 -> BlocklistTab(repo)
        }
    }
}

@Composable
private fun QueueTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<QueueResponse>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getQueue()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(Unit) {
        load()
        while (true) {
            delay(10_000)
            try {
                val q = repo.getQueue()
                state = UiState.Success(q)
            } catch (_: Exception) {}
        }
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { load() }
        is UiState.Success -> {
            val records = s.data.records
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${s.data.totalRecords} items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RadarrMuted
                )
                TvFocusButton(onClick = { load() }) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh")
                }
            }
            Spacer(Modifier.height(12.dp))
            if (records.isEmpty()) {
                EmptyScreen("Queue is empty — no active downloads")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(records, key = { it.id }) { item ->
                        QueueItemRow(
                            item = item,
                            onRemove = {
                                scope.launch {
                                    try {
                                        repo.removeFromQueue(item.id)
                                        load()
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(item: QueueItem, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.movie?.title ?: item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = RadarrWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    item.quality?.quality?.name?.let { StatusBadge(it, RadarrBlueDark) }
                    Text(item.status, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                    if (item.size > 0) Text(formatBytes(item.size), style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                    item.estimatedCompletionTime?.let { Text("ETA: ${fmtEta(it)}", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted) }
                }
            }
            TvFocusButton(onClick = onRemove, isDanger = true) {
                Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
            }
        }
        if (item.size > 0) {
            Spacer(Modifier.height(8.dp))
            TvProgressBar(
                progress = (item.progressPct / 100.0).toFloat(),
                color = when {
                    item.status == "completed" -> RadarrGreen
                    item.status.contains("fail", ignoreCase = true) -> RadarrRed
                    else -> RadarrBlue
                }
            )
            Text(
                "${item.progressPct.toInt()}% · ${formatBytes(item.size - item.sizeleft)} of ${formatBytes(item.size)}",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun fmtEta(iso: String): String {
    return try {
        val t = Instant.parse(iso).toEpochMilli() - System.currentTimeMillis()
        if (t <= 0) return "soon"
        val m = (t / 60000).toInt()
        if (m < 60) "$m min" else "${m / 60}h ${m % 60}m"
    } catch (_: Exception) { "—" }
}

@Composable
private fun HistoryTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<HistoryResponse>>(UiState.Loading) }
    var page by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getHistory(page)) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(page) { load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { load() }
        is UiState.Success -> {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${s.data.totalRecords} total", style = MaterialTheme.typography.bodyLarge, color = RadarrMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TvFocusButton(onClick = { if (page > 1) page-- }, enabled = page > 1) {
                            Icon(Icons.Default.ChevronLeft, null, Modifier.size(18.dp))
                        }
                        Text("Page $page", style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                        TvFocusButton(
                            onClick = { if (page * 50 < s.data.totalRecords) page++ },
                            enabled = page * 50 < s.data.totalRecords
                        ) {
                            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(s.data.records, key = { it.id }) { item ->
                        HistoryItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(item: HistoryItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(RadarrCard)
            .padding(12.dp)
    ) {
        val color = when (item.eventType) {
            "grabbed" -> RadarrBlue
            "downloadFolderImported" -> RadarrGreen
            "downloadFailed" -> RadarrRed
            "movieFileDeleted" -> RadarrOrange
            else -> RadarrMuted
        }
        StatusBadge(item.eventTypeDisplay, color, modifier = Modifier.width(80.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.movie?.title ?: "Movie #${item.movieId}",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.sourceTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            item.quality?.quality?.name?.let { StatusBadge(it, RadarrBlueDark) }
            Text(item.date.take(10), style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
        }
    }
}

@Composable
private fun BlocklistTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<BlocklistResponse>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getBlocklist()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(Unit) { load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { load() }
        is UiState.Success -> {
            if (s.data.records.isEmpty()) {
                EmptyScreen("Blocklist is empty")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(s.data.records, key = { it.id }) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(RadarrCard)
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.movie?.title ?: "Movie #${item.movieId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RadarrWhite
                                )
                                Text(item.sourceTitle, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            TvFocusButton(onClick = {
                                scope.launch {
                                    try { repo.deleteBlocklistItem(item.id); load() }
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
