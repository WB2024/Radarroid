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
import kotlinx.coroutines.launch

@Composable
fun WantedScreen(repo: RadarrRepository, onMovieClick: (Int) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Missing", "Cutoff Unmet")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Wanted", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        }
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
            0 -> WantedTab(isMissing = true, repo = repo, onMovieClick = onMovieClick)
            1 -> WantedTab(isMissing = false, repo = repo, onMovieClick = onMovieClick)
        }
    }
}

@Composable
private fun WantedTab(
    isMissing: Boolean,
    repo: RadarrRepository,
    onMovieClick: (Int) -> Unit
) {
    var state by remember { mutableStateOf<UiState<WantedResponse>>(UiState.Loading) }
    var page by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try {
                if (isMissing) UiState.Success(repo.getWantedMissing(page))
                else UiState.Success(repo.getWantedCutoff(page))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed")
            }
        }
    }

    LaunchedEffect(page, isMissing) { load() }

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
                    Text("${s.data.totalRecords} movies", style = MaterialTheme.typography.bodyLarge, color = RadarrMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Search all
                        TvFocusButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val ids = s.data.records.map { it.id }
                                        if (ids.isNotEmpty()) {
                                            repo.postCommand(CommandRequest("MoviesSearch", movieIds = ids))
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                            isPrimary = true
                        ) {
                            Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Search All")
                        }
                        TvFocusButton(onClick = { if (page > 1) page-- }, enabled = page > 1) {
                            Icon(Icons.Default.ChevronLeft, null, Modifier.size(18.dp))
                        }
                        Text("Page $page", style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
                        TvFocusButton(onClick = { if (page * 50 < s.data.totalRecords) page++ }, enabled = page * 50 < s.data.totalRecords) {
                            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (s.data.records.isEmpty()) {
                    EmptyScreen(if (isMissing) "No missing monitored movies" else "All movies meet their cutoff")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(s.data.records, key = { it.id }) { movie ->
                            WantedMovieRow(
                                movie = movie,
                                repo = repo,
                                onClick = { onMovieClick(movie.id) },
                                onSearch = {
                                    scope.launch {
                                        try { repo.postCommand(CommandRequest("MoviesSearch", movieIds = listOf(movie.id))) }
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
private fun WantedMovieRow(
    movie: Movie,
    repo: RadarrRepository,
    onClick: () -> Unit,
    onSearch: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(RadarrSurfaceVariant)
        ) {
            coil.compose.AsyncImage(
                model = repo.posterUrl(movie.id),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${movie.title} (${movie.year})",
                style = MaterialTheme.typography.titleSmall,
                color = RadarrWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val availability = movie.minimumAvailability.replaceFirstChar { it.uppercase() }
            Text(
                "Min: $availability · ${movie.status.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TvFocusButton(onClick = onSearch, isPrimary = true) {
                Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Search")
            }
            TvFocusButton(onClick = onClick) {
                Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Details")
            }
        }
    }
}
