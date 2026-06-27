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
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

private enum class SystemTab { STATUS, TASKS, LOGS, HEALTH }

@Composable
fun SystemScreen(repo: RadarrRepository) {
    var activeTab by remember { mutableStateOf(SystemTab.STATUS) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("System", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SystemTab.values().forEach { tab ->
                TvFocusButton(
                    onClick = { activeTab = tab },
                    isPrimary = activeTab == tab
                ) {
                    Text(tab.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
            Spacer(Modifier.weight(1f))
            TvFocusButton(onClick = {
                scope.launch {
                    try { repo.sendCommand("ApplicationUpdate") } catch (_: Exception) {}
                }
            }) {
                Icon(Icons.Default.Upgrade, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Check for Updates")
            }
        }
        Spacer(Modifier.height(16.dp))

        when (activeTab) {
            SystemTab.STATUS -> StatusTab(repo)
            SystemTab.TASKS -> TasksTab(repo)
            SystemTab.LOGS -> LogsTab(repo)
            SystemTab.HEALTH -> HealthTab(repo)
        }
    }
}

@Composable
private fun StatusTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<SystemStatus>>(UiState.Loading) }
    var diskState by remember { mutableStateOf<UiState<List<DiskSpace>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            state = try { UiState.Success(repo.getSystemStatus()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
        scope.launch {
            diskState = try { UiState.Success(repo.getDiskSpace()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            when (val s = state) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(s.message)
                is UiState.Success -> {
                    val status = s.data
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader("Application")
                        StatusRow("Version", status.version)
                        StatusRow("Build Time", status.buildTime)
                        StatusRow("Branch", status.branch)
                        StatusRow("Startup Path", status.startupPath)
                        StatusRow("App Data", status.appData)
                        StatusRow("OS Name", status.osName)
                        StatusRow("OS Version", status.osVersion)
                        StatusRow("Runtime Version", status.runtimeVersion)
                        StatusRow("Package Version", status.packageVersion ?: "—")
                        if (status.isProduction) StatusBadge("Production", RadarrGreen)
                        else StatusBadge("Development", RadarrOrange)
                    }
                }
            }
        }
        item {
            when (val d = diskState) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
                is UiState.Error -> Unit
                is UiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader("Disk Space")
                        d.data.forEach { disk ->
                            DiskSpaceRow(disk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiskSpaceRow(disk: DiskSpace) {
    val usedBytes = disk.totalSpace - disk.freeSpace
    val usedFraction = if (disk.totalSpace > 0) usedBytes.toFloat() / disk.totalSpace else 0f
    val barColor = when {
        usedFraction > 0.9f -> RadarrRed
        usedFraction > 0.75f -> RadarrOrange
        else -> RadarrGreen
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(disk.path, style = MaterialTheme.typography.bodyLarge, color = RadarrWhite, modifier = Modifier.weight(1f))
            Text(
                "${formatBytes(disk.freeSpace)} free / ${formatBytes(disk.totalSpace)}",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
        Spacer(Modifier.height(4.dp))
        TvProgressBar(progress = usedFraction, color = barColor)
    }
}

@Composable
private fun TasksTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<List<ScheduledTask>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getTasks()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(Unit) { load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { load() }
        is UiState.Success -> {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(s.data, key = { it.id }) { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard)
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.name, style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                            Text(
                                "Last: ${task.lastExecution.take(19).replace("T", " ")} · Next: ${task.nextExecution.take(19).replace("T", " ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RadarrMuted
                            )
                            Text(
                                "Interval: ${task.interval}min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RadarrMuted
                            )
                        }
                        TvFocusButton(onClick = {
                            scope.launch {
                                try { repo.sendCommand(task.taskName); load() }
                                catch (_: Exception) {}
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<List<LogEntry>>>(UiState.Loading) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try {
                val resp = repo.getLogs(page = page, pageSize = 50)
                totalPages = (resp.totalRecords + 49) / 50
                UiState.Success(resp.records)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed")
            }
        }
    }

    LaunchedEffect(page) { load() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TvFocusButton(onClick = { if (page > 1) page-- }, enabled = page > 1) {
                Icon(Icons.Default.ChevronLeft, null, Modifier.size(18.dp))
            }
            Text(" Page $page / $totalPages ", color = RadarrWhite, modifier = Modifier.padding(horizontal = 8.dp))
            TvFocusButton(onClick = { if (page < totalPages) page++ }, enabled = page < totalPages) {
                Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { load() }
            is UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(s.data, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(RadarrSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            val levelColor = when (entry.level.lowercase()) {
                                "error", "fatal" -> RadarrRed
                                "warn" -> RadarrOrange
                                "info" -> RadarrBlue
                                else -> RadarrMuted
                            }
                            Text(
                                entry.level.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = levelColor,
                                modifier = Modifier.width(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                entry.time.take(19).replace("T", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = RadarrMuted,
                                modifier = Modifier.width(120.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                entry.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RadarrWhite,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthTab(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<List<HealthCheck>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(repo.getHealth()) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(Unit) { load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { load() }
        is UiState.Success -> {
            if (s.data.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = RadarrGreen, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Health Issues", style = MaterialTheme.typography.titleMedium, color = RadarrGreen)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.data, key = { it.source }) { check ->
                        val typeColor = when (check.type.lowercase()) {
                            "error" -> RadarrRed
                            "warning" -> RadarrOrange
                            else -> RadarrBlue
                        }
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RadarrCard)
                                .padding(16.dp)
                        ) {
                            Icon(
                                if (check.type.lowercase() == "error") Icons.Default.Error else Icons.Default.Warning,
                                null,
                                tint = typeColor,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(check.source, style = MaterialTheme.typography.titleSmall, color = RadarrWhite)
                                    Spacer(Modifier.width(8.dp))
                                    StatusBadge(check.type.uppercase(), typeColor)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(check.message, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                                if (!check.wikiUrl.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(check.wikiUrl ?: "", style = MaterialTheme.typography.bodyMedium, color = RadarrBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted, modifier = Modifier.width(160.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = RadarrWhite, modifier = Modifier.weight(1f))
    }
}
