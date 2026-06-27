package com.radarrtv.androidtv.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.Movie
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*

private enum class LibraryFilter(val label: String) {
    ALL("All"),
    DOWNLOADED("Downloaded"),
    MISSING("Missing"),
    MONITORED("Monitored"),
    UNMONITORED("Unmonitored")
}

private enum class LibrarySort(val label: String) {
    TITLE("Title"),
    YEAR("Year"),
    ADDED("Added"),
    RATING("Rating"),
    SIZE("Size")
}

@Composable
fun MoviesScreen(
    repo: RadarrRepository,
    onMovieClick: (Int) -> Unit
) {
    var uiState by remember { mutableStateOf<UiState<List<Movie>>>(UiState.Loading) }
    var filter by remember { mutableStateOf(LibraryFilter.ALL) }
    var sort by remember { mutableStateOf(LibrarySort.TITLE) }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        uiState = UiState.Loading
        uiState = try {
            UiState.Success(repo.getMovies())
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load movies")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Movies",
                style = MaterialTheme.typography.headlineMedium,
                color = RadarrWhite
            )
            Spacer(Modifier.weight(1f))

            // Filter dropdown
            Box {
                TvFocusButton(onClick = { filterMenuOpen = !filterMenuOpen }) {
                    Text("Filter: ${filter.label}")
                }
                DropdownMenu(
                    expanded = filterMenuOpen,
                    onDismissRequest = { filterMenuOpen = false },
                    modifier = Modifier.background(RadarrSurface)
                ) {
                    LibraryFilter.values().forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.label, color = if (f == filter) RadarrBlue else RadarrWhite) },
                            onClick = { filter = f; filterMenuOpen = false }
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Sort dropdown
            Box {
                TvFocusButton(onClick = { sortMenuOpen = !sortMenuOpen }) {
                    Icon(Icons.Default.Sort, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sort: ${sort.label}")
                }
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false },
                    modifier = Modifier.background(RadarrSurface)
                ) {
                    LibrarySort.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.label, color = if (s == sort) RadarrBlue else RadarrWhite) },
                            onClick = { sort = s; sortMenuOpen = false }
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            TvFocusButton(onClick = {
                uiState = UiState.Loading
            }) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val s = uiState) {
            is UiState.Loading -> {
                LaunchedEffect(uiState) {
                    if (uiState is UiState.Loading) {
                        uiState = try {
                            UiState.Success(repo.getMovies())
                        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                            UiState.Error(e.message ?: "Failed")
                        }
                    }
                }
                LoadingScreen()
            }
            is UiState.Error -> ErrorScreen(s.message) {
                uiState = UiState.Loading
            }
            is UiState.Success -> {
                val movies = filterAndSort(s.data, filter, sort)
                Text(
                    "${movies.size} movies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RadarrMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (movies.isEmpty()) {
                    EmptyScreen("No movies match the current filter")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(movies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                posterUrl = repo.posterUrl(movie.id),
                                onClick = { onMovieClick(movie.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun filterAndSort(
    movies: List<Movie>,
    filter: LibraryFilter,
    sort: LibrarySort
): List<Movie> {
    val filtered = when (filter) {
        LibraryFilter.ALL -> movies
        LibraryFilter.DOWNLOADED -> movies.filter { it.hasFile }
        LibraryFilter.MISSING -> movies.filter { !it.hasFile && it.monitored }
        LibraryFilter.MONITORED -> movies.filter { it.monitored }
        LibraryFilter.UNMONITORED -> movies.filter { !it.monitored }
    }
    return when (sort) {
        LibrarySort.TITLE -> filtered.sortedBy { it.sortTitle.ifBlank { it.title } }
        LibrarySort.YEAR -> filtered.sortedByDescending { it.year }
        LibrarySort.ADDED -> filtered.sortedByDescending { it.added }
        LibrarySort.RATING -> filtered.sortedByDescending { it.ratings?.tmdb?.value ?: 0.0 }
        LibrarySort.SIZE -> filtered.sortedByDescending { it.sizeOnDisk ?: 0L }
    }
}
