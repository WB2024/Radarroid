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
import com.radarrtv.androidtv.data.api.model.HostConfig
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HostConfigScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<HostConfig>>(UiState.Loading) }
    var config by remember { mutableStateOf<HostConfig?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state = try {
            val c = repo.getHostConfig()
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
            Text("General Settings", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
            Spacer(Modifier.weight(1f))
            if (config != null) {
                TvFocusButton(
                    onClick = {
                        scope.launch {
                            saving = true
                            try { repo.updateHostConfig(config!!) } catch (_: Exception) {}
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
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Host", style = MaterialTheme.typography.titleSmall, color = RadarrBlue)
                        TvTextField(
                            value = c.instanceName,
                            onValueChange = { config = c.copy(instanceName = it) },
                            label = "Instance Name"
                        )
                        TvTextField(
                            value = c.urlBase,
                            onValueChange = { config = c.copy(urlBase = it) },
                            label = "URL Base",
                            placeholder = "/radarr"
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Security", style = MaterialTheme.typography.titleSmall, color = RadarrBlue)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(end = 12.dp)) {
                                Column {
                                    Text("API Key", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                                    Spacer(Modifier.height(4.dp))
                                    Text(c.apiKey, style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                                }
                            }
                        }
                        Text(
                            "Authentication: ${c.authenticationMethod}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RadarrMuted
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RadarrCard).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Updates", style = MaterialTheme.typography.titleSmall, color = RadarrBlue)
                        Text(
                            "Branch: ${c.branch}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RadarrMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automatic Updates", style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                                Text("Automatically install updates", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                            }
                            Switch(
                                checked = c.updateAutomatically,
                                onCheckedChange = { config = c.copy(updateAutomatically = it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue, checkedTrackColor = RadarrBlueDark)
                            )
                        }
                    }
                }
            }
        }
    }
}
