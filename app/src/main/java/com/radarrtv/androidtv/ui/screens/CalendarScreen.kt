package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.data.api.model.Movie
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(repo: RadarrRepository) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var state by remember { mutableStateOf<UiState<List<Movie>>>(UiState.Loading) }

    LaunchedEffect(yearMonth) {
        state = UiState.Loading
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()
        state = try { UiState.Success(repo.getCalendar(start, end)) }
        catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Calendar", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
            Spacer(Modifier.weight(1f))
            TvFocusButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, null, Modifier.size(22.dp))
            }
            Text(
                yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge,
                color = RadarrWhite,
                modifier = Modifier.padding(horizontal = 16.dp).widthIn(min = 200.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TvFocusButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, null, Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message)
            is UiState.Success -> {
                val movies = s.data
                if (movies.isEmpty()) {
                    EmptyScreen("No releases this month")
                } else {
                    // Group by release date
                    val grouped = movies
                        .groupBy { movie ->
                            (movie.inCinemas ?: movie.physicalRelease ?: movie.digitalRelease ?: "")
                                .take(10)
                        }
                        .toSortedMap()

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        grouped.forEach { (dateStr, dayMovies) ->
                            item {
                                val label = try {
                                    val d = LocalDate.parse(dateStr)
                                    d.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                                } catch (_: Exception) { dateStr }
                                Text(
                                    label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadarrBlue
                                )
                            }
                            items(dayMovies, key = { it.id }) { movie ->
                                CalendarMovieRow(movie, repo)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMovieRow(movie: Movie, repo: RadarrRepository) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .padding(12.dp)
    ) {
        // Mini poster
        Box(
            modifier = Modifier
                .size(50.dp, 75.dp)
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
            val releaseType = when {
                !movie.inCinemas.isNullOrBlank() -> "In Cinemas"
                !movie.physicalRelease.isNullOrBlank() -> "Physical"
                !movie.digitalRelease.isNullOrBlank() -> "Digital"
                else -> ""
            }
            if (releaseType.isNotBlank()) {
                Text(releaseType, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
            }
            if (movie.runtime > 0) {
                Text("${movie.runtime} min", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
            }
        }
        val statusColor = when {
            movie.hasFile -> RadarrGreen
            movie.monitored -> RadarrOrange
            else -> RadarrMuted
        }
        val statusText = when {
            movie.hasFile -> "Downloaded"
            movie.monitored -> "Monitored"
            else -> "Not Monitored"
        }
        StatusBadge(statusText, statusColor)
    }
}
