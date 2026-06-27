package com.radarrtv.androidtv.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.NamingConfig
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NamingScreen(repo: RadarrRepository, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<NamingConfig>>(UiState.Loading) }
    var config by remember { mutableStateOf<NamingConfig?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state = try {
            val c = repo.getNamingConfig()
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
            Text("Naming", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
            Spacer(Modifier.weight(1f))
            if (config != null) {
                TvFocusButton(
                    onClick = {
                        scope.launch {
                            saving = true
                            try { repo.updateNamingConfig(config!!) } catch (_: Exception) {}
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
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Rename Movies", style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                            Text("Rename movie files to the configured format", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                        }
                        Switch(
                            checked = c.renameMovies,
                            onCheckedChange = { config = c.copy(renameMovies = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue, checkedTrackColor = RadarrBlueDark)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Replace Illegal Characters", style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                            Text("Replace or strip illegal characters from titles", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                        }
                        Switch(
                            checked = c.replaceIllegalCharacters,
                            onCheckedChange = { config = c.copy(replaceIllegalCharacters = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue, checkedTrackColor = RadarrBlueDark)
                        )
                    }
                    TvTextField(
                        value = c.standardMovieFormat,
                        onValueChange = { config = c.copy(standardMovieFormat = it) },
                        label = "Standard Movie Format",
                        placeholder = "{Movie Title} ({Release Year}) {Quality Full}"
                    )
                    TvTextField(
                        value = c.movieFolderFormat,
                        onValueChange = { config = c.copy(movieFolderFormat = it) },
                        label = "Movie Folder Format",
                        placeholder = "{Movie Title} ({Release Year})"
                    )
                    Text(
                        "Available tokens: {Movie Title}, {Release Year}, {Quality Full}, {Quality Title}, {Resolution}, {Edition Tags}, {Release Group}, {MediaInfo VideoCodec}, {MediaInfo AudioCodec}, {IMDB Id}, {TMDb Id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RadarrMuted
                    )
                }
            }
        }
    }
}
