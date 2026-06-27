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
import com.radarrtv.androidtv.data.api.model.MediaManagementConfig
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MediaManagementScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<MediaManagementConfig>>(UiState.Loading) }
    var config by remember { mutableStateOf<MediaManagementConfig?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state = try {
            val c = repo.getMediaManagement()
            config = c
            UiState.Success(c)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TvFocusButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back")
            }
            Spacer(Modifier.width(16.dp))
            Text("Media Management", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
            Spacer(Modifier.weight(1f))
            if (config != null) {
                TvFocusButton(
                    onClick = {
                        scope.launch {
                            saving = true
                            try { repo.updateMediaManagement(config!!) }
                            catch (_: Exception) {}
                            saving = false
                        }
                    },
                    isPrimary = true,
                    enabled = !saving
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.Save, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Save") }
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message)
            is UiState.Success -> {
                val c = config ?: return@Column
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    SectionCard("File Management") {
                        ToggleSetting(
                            label = "Delete Empty Folders",
                            description = "Delete empty movie folders after deleting files",
                            value = c.deleteEmptyFolders,
                            onChange = { config = c.copy(deleteEmptyFolders = it) }
                        )
                        ToggleSetting(
                            label = "Skip Free Space Check",
                            description = "Skip disk free space check when importing",
                            value = c.skipFreeSpaceCheckWhenImporting,
                            onChange = { config = c.copy(skipFreeSpaceCheckWhenImporting = it) }
                        )
                        ToggleSetting(
                            label = "Copy Using Hard Links",
                            description = "Use hard links instead of copying when importing",
                            value = c.copyUsingHardlinks,
                            onChange = { config = c.copy(copyUsingHardlinks = it) }
                        )
                        ToggleSetting(
                            label = "Import Extra Files",
                            description = "Import extra files (subtitles, nfo, etc.)",
                            value = c.importExtraFiles,
                            onChange = { config = c.copy(importExtraFiles = it) }
                        )
                        if (c.importExtraFiles) {
                            TvTextField(
                                value = c.extraFileExtensions,
                                onValueChange = { config = c.copy(extraFileExtensions = it) },
                                label = "Extra File Extensions",
                                placeholder = "srt,nfo,jpg"
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    SectionCard("Recycle Bin") {
                        TvTextField(
                            value = c.recycleBin,
                            onValueChange = { config = c.copy(recycleBin = it) },
                            label = "Recycle Bin Path",
                            placeholder = "/path/to/recycle/bin"
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Cleanup after ${c.recycleBinCleanupDays} days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RadarrMuted
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    SectionCard("Permissions") {
                        ToggleSetting(
                            label = "Set Permissions (Linux)",
                            description = "Set file and folder permissions",
                            value = c.setPermissionsLinux,
                            onChange = { config = c.copy(setPermissionsLinux = it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = RadarrBlue)
        content()
    }
}

@Composable
private fun ToggleSetting(
    label: String,
    description: String,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
        }
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue, checkedTrackColor = RadarrBlueDark)
        )
    }
}
