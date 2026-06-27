package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(repo: RadarrRepository, onMovieAdded: (Int) -> Unit, onMovieClick: (Int) -> Unit = onMovieAdded) {
    var query by remember { mutableStateOf("") }
    var resultsState by remember { mutableStateOf<UiState<List<Movie>>>(UiState.Success(emptyList())) }
    // Maps tmdbId → Radarr library id for already-added movies
    var libraryMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var profiles by remember { mutableStateOf<List<QualityProfile>>(emptyList()) }
    var rootFolders by remember { mutableStateOf<List<RootFolder>>(emptyList()) }
    var addDialogMovie by remember { mutableStateOf<Movie?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        launch { libraryMap = repo.getMovies().associate { it.tmdbId to it.id } }
        launch { profiles = try { repo.getQualityProfiles() } catch (_: Exception) { emptyList() } }
        launch { rootFolders = try { repo.getRootFolders() } catch (_: Exception) { emptyList() } }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Add Movie", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(20.dp))

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { q ->
                query = q
                searchJob?.cancel()
                if (q.length >= 2) {
                    resultsState = UiState.Loading
                    searchJob = scope.launch {
                        delay(500)
                        resultsState = try { UiState.Success(repo.lookupMovie(q)) }
                        catch (e: Exception) { UiState.Error(e.message ?: "Search failed") }
                    }
                } else if (q.isEmpty()) {
                    resultsState = UiState.Success(emptyList())
                }
            },
            placeholder = { Text("Search for a movie title…", color = RadarrMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = RadarrMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RadarrBlue,
                unfocusedBorderColor = RadarrBorder,
                focusedContainerColor = RadarrCard,
                unfocusedContainerColor = RadarrSurfaceVariant,
                focusedTextColor = RadarrWhite,
                unfocusedTextColor = RadarrWhite,
                cursorColor = RadarrBlue
            )
        )

        Spacer(Modifier.height(16.dp))

        when (val s = resultsState) {
            is UiState.Loading -> LoadingScreen("Searching…")
            is UiState.Error -> ErrorScreen(s.message)
            is UiState.Success -> {
                if (s.data.isEmpty() && query.length >= 2) {
                    EmptyScreen("No results found")
                } else if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Type at least 2 characters to search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RadarrMuted
                        )
                    }
                } else {
                    Text(
                        "${s.data.size} results",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RadarrMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.data.take(60), key = { it.tmdbId }) { movie ->
                            val libraryId = libraryMap[movie.tmdbId]
                            val inLibrary = libraryId != null
                            MovieCard(
                                movie = movie.copy(hasFile = inLibrary, monitored = inLibrary),
                                posterUrl = run {
                                    val poster = movie.images.firstOrNull { it.coverType == "poster" }
                                    poster?.remoteUrl ?: poster?.url?.let {
                                        if (it.startsWith("http")) it
                                        else "${repo.serverUrl.trimEnd('/')}$it?apikey=${repo.apiKey}"
                                    } ?: ""
                                },
                                onClick = {
                                    if (inLibrary && libraryId != null) onMovieClick(libraryId)
                                    else addDialogMovie = movie
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    addDialogMovie?.let { movie ->
        AddMovieDialog(
            movie = movie,
            profiles = profiles,
            rootFolders = rootFolders,
            onAdd = { req ->
                scope.launch {
                    try {
                        val added = repo.addMovie(req)
                        libraryMap = libraryMap + (added.tmdbId to added.id)
                        addDialogMovie = null
                        onMovieAdded(added.id)
                    } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                        addDialogMovie = null
                    }
                }
            },
            onDismiss = { addDialogMovie = null }
        )
    }
}

@Composable
private fun AddMovieDialog(
    movie: Movie,
    profiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    onAdd: (AddMovieRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProfile by remember { mutableStateOf(profiles.firstOrNull()?.id ?: 1) }
    var selectedFolder by remember { mutableStateOf(rootFolders.firstOrNull()?.path ?: "") }
    var monitored by remember { mutableStateOf(true) }
    var searchOnAdd by remember { mutableStateOf(true) }
    var profileMenuOpen by remember { mutableStateOf(false) }
    var folderMenuOpen by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add ${movie.title}${if (movie.year > 0) " (${movie.year})" else ""}",
                color = RadarrWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.withVerticalScroll()) {
                if (!movie.overview.isNullOrBlank()) {
                    Text(
                        movie.overview.take(200) + if (movie.overview.length > 200) "…" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RadarrMuted
                    )
                }
                if (profiles.isNotEmpty()) {
                    Text("Quality Profile", color = RadarrMuted, style = MaterialTheme.typography.bodyMedium)
                    Box {
                        TvFocusButton(onClick = { profileMenuOpen = true }) {
                            Text(profiles.find { it.id == selectedProfile }?.name ?: "Select…")
                        }
                        DropdownMenu(
                            expanded = profileMenuOpen,
                            onDismissRequest = { profileMenuOpen = false },
                            modifier = Modifier.background(RadarrSurface)
                        ) {
                            profiles.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name, color = RadarrWhite) },
                                    onClick = { selectedProfile = p.id; profileMenuOpen = false }
                                )
                            }
                        }
                    }
                }
                if (rootFolders.isNotEmpty()) {
                    Text("Root Folder", color = RadarrMuted, style = MaterialTheme.typography.bodyMedium)
                    Box {
                        TvFocusButton(onClick = { folderMenuOpen = true }) {
                            Text(selectedFolder.ifBlank { "Select…" })
                        }
                        DropdownMenu(
                            expanded = folderMenuOpen,
                            onDismissRequest = { folderMenuOpen = false },
                            modifier = Modifier.background(RadarrSurface)
                        ) {
                            rootFolders.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.path, color = RadarrWhite) },
                                    onClick = { selectedFolder = f.path; folderMenuOpen = false }
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Monitored", color = RadarrWhite, modifier = Modifier.weight(1f))
                    Switch(checked = monitored, onCheckedChange = { monitored = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Search on add", color = RadarrWhite, modifier = Modifier.weight(1f))
                    Switch(checked = searchOnAdd, onCheckedChange = { searchOnAdd = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue))
                }
            }
        },
        confirmButton = {
            TvFocusButton(
                onClick = {
                    adding = true
                    onAdd(
                        AddMovieRequest(
                            title = movie.title,
                            tmdbId = movie.tmdbId,
                            year = movie.year,
                            qualityProfileId = selectedProfile,
                            rootFolderPath = selectedFolder,
                            monitored = monitored,
                            addOptions = AddOptions(searchForMovie = searchOnAdd),
                            images = movie.images
                        )
                    )
                },
                isPrimary = true,
                enabled = !adding && selectedFolder.isNotBlank()
            ) {
                if (adding) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("+ Add Movie")
            }
        },
        dismissButton = {
            TvFocusButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = RadarrSurface
    )
}

@Composable
private fun Modifier.withVerticalScroll(): Modifier {
    val scrollState = rememberScrollState()
    return this.verticalScroll(scrollState)
}
