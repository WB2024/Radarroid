package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MovieDetailScreen(
    movieId: Int,
    repo: RadarrRepository,
    onBack: () -> Unit
) {
    var movieState by remember { mutableStateOf<UiState<Movie>>(UiState.Loading) }
    var historyState by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var filesState by remember { mutableStateOf<List<MovieFile>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        movieState = UiState.Loading
        try {
            val movie = repo.getMovie(movieId)
            movieState = UiState.Success(movie)
            launch { try { historyState = repo.getMovieHistory(movieId) } catch (_: Exception) {} }
            launch { try { filesState = repo.getMovieFiles(movieId) } catch (_: Exception) {} }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
            movieState = UiState.Error(e.message ?: "Failed to load movie")
        }
    }

    when (val s = movieState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) {
            movieState = UiState.Loading
        }
        is UiState.Success -> {
            val movie = s.data
            Row(modifier = Modifier.fillMaxSize().background(RadarrBg)) {
                // Left: Poster
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(RadarrSurfaceVariant)
                ) {
                    AsyncImage(
                        model = repo.posterUrl(movie.id),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Backdrop overlay gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        RadarrBg.copy(alpha = 0.8f)
                                    ),
                                    startY = 300f
                                )
                            )
                    )
                }

                // Right: Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title + year
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            movie.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = RadarrWhite,
                            modifier = Modifier.weight(1f)
                        )
                        if (movie.year > 0) {
                            Text(
                                "(${movie.year})",
                                style = MaterialTheme.typography.headlineMedium,
                                color = RadarrMuted,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Meta row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        val rating = movie.ratings?.tmdb?.value
                        if (rating != null && rating > 0) {
                            Icon(Icons.Default.Star, null, tint = RadarrYellow, modifier = Modifier.size(18.dp))
                            Text(
                                " ${"%.1f".format(rating)}  ·  ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = RadarrMuted
                            )
                        }
                        if (movie.runtime > 0) {
                            Text("${movie.runtime} min  ·  ", style = MaterialTheme.typography.bodyLarge, color = RadarrMuted)
                        }
                        if (!movie.certification.isNullOrBlank()) {
                            StatusBadge(movie.certification, RadarrMuted)
                            Spacer(Modifier.width(8.dp))
                        }
                        val statusColor = when {
                            movie.hasFile -> RadarrGreen
                            movie.monitored -> RadarrOrange
                            else -> RadarrMuted
                        }
                        val statusText = when {
                            movie.hasFile -> "Downloaded"
                            movie.monitored -> "Monitored – Missing"
                            else -> "Unmonitored"
                        }
                        StatusBadge(statusText, statusColor)
                    }

                    // Genres
                    if (movie.genres.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            movie.genres.take(5).forEach { genre ->
                                StatusBadge(genre, RadarrBlueDark)
                            }
                        }
                    }

                    // Overview
                    if (!movie.overview.isNullOrBlank()) {
                        Text(
                            movie.overview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = RadarrWhite.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    // Stats grid
                    Spacer(Modifier.height(20.dp))
                    DetailStatsRow(movie, filesState)

                    // Action buttons
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvFocusButton(onClick = { showSearchDialog = true }, isPrimary = true) {
                            Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Search Releases")
                        }
                        TvFocusButton(onClick = {
                            scope.launch {
                                try {
                                    repo.postCommand(
                                        com.radarrtv.androidtv.data.api.model.CommandRequest(
                                            "MoviesSearch",
                                            movieIds = listOf(movie.id)
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Automatic Search")
                        }
                        TvFocusButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit")
                        }
                        TvFocusButton(onClick = { showDeleteDialog = true }, isDanger = true) {
                            Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delete")
                        }
                        TvFocusButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                    }

                    // Files
                    if (filesState.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        SectionHeader("Files")
                        filesState.forEach { file ->
                            MovieFileRow(
                                file = file,
                                onDelete = {
                                    scope.launch {
                                        try {
                                            repo.deleteMovieFile(file.id)
                                            filesState = filesState.filter { it.id != file.id }
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                        }
                    }

                    // History
                    if (historyState.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        SectionHeader("History")
                        historyState.take(20).forEach { item ->
                            HistoryRow(item)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            if (showDeleteDialog) {
                DeleteMovieDialog(
                    movie = movie,
                    onConfirm = { deleteFiles ->
                        scope.launch {
                            try {
                                repo.deleteMovie(movie.id, deleteFiles)
                                onBack()
                            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) { }
                            showDeleteDialog = false
                        }
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            if (showSearchDialog) {
                InteractiveSearchDialog(
                    movie = movie,
                    repo = repo,
                    onDismiss = { showSearchDialog = false }
                )
            }

            if (showEditDialog) {
                EditMovieDialog(
                    movie = movie,
                    repo = repo,
                    onSaved = { updated ->
                        movieState = UiState.Success(updated)
                        showEditDialog = false
                    },
                    onDismiss = { showEditDialog = false }
                )
            }
        }
    }
}

@Composable
private fun DetailStatsRow(movie: Movie, files: List<MovieFile>) {
    val file = files.firstOrNull()
    val quality = file?.quality?.quality?.name ?: "—"
    val size = movie.sizeOnDisk?.let { formatBytes(it) } ?: "—"

    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        StatItem("Studio", movie.studio ?: "—")
        StatItem("Quality", quality)
        StatItem("Size", size)
        if (!movie.imdbId.isNullOrBlank()) StatItem("IMDb", movie.imdbId)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
        Text(value, style = MaterialTheme.typography.titleSmall, color = RadarrWhite)
    }
}

@Composable
private fun MovieFileRow(file: MovieFile, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(RadarrCard)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                file.relativePath.substringAfterLast('/').ifBlank { file.path },
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    file.quality?.quality?.name?.let { append(it) }
                    if (file.size > 0) append("  ·  ${formatBytes(file.size)}")
                    file.languages.firstOrNull()?.name?.let { append("  ·  $it") }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
        TvFocusButton(onClick = onDelete, isDanger = true) {
            Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
        }
    }
}

@Composable
private fun HistoryRow(item: HistoryItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(RadarrCard)
            .padding(12.dp)
    ) {
        val color = when (item.eventType) {
            "grabbed" -> RadarrBlue
            "downloadFolderImported" -> RadarrGreen
            "downloadFailed" -> RadarrRed
            else -> RadarrMuted
        }
        StatusBadge(item.eventTypeDisplay, color, modifier = Modifier.width(90.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.sourceTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.date.take(10),
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
        item.quality?.quality?.name?.let { q ->
            StatusBadge(q, RadarrBlueDark)
        }
    }
}

@Composable
private fun DeleteMovieDialog(
    movie: Movie,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var deleteFiles by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Movie?", color = RadarrWhite) },
        text = {
            Column {
                Text(
                    "Remove \"${movie.title}\" from Radarr?",
                    color = RadarrWhite
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = deleteFiles,
                        onCheckedChange = { deleteFiles = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = RadarrRed,
                            uncheckedColor = RadarrMuted
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Also delete files from disk", color = RadarrWhite)
                }
            }
        },
        confirmButton = {
            TvFocusButton(onClick = { onConfirm(deleteFiles) }, isDanger = true) {
                Text("Delete")
            }
        },
        dismissButton = {
            TvFocusButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = RadarrSurface
    )
}

@Composable
private fun EditMovieDialog(
    movie: Movie,
    repo: RadarrRepository,
    onSaved: (Movie) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember { mutableStateOf(movie.monitored) }
    var minimumAvailability by remember { mutableStateOf(movie.minimumAvailability) }
    var profilesState by remember { mutableStateOf<List<com.radarrtv.androidtv.data.api.model.QualityProfile>>(emptyList()) }
    var selectedProfile by remember { mutableStateOf(movie.qualityProfileId) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { profilesState = repo.getQualityProfiles() } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Movie", color = RadarrWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Monitored", color = RadarrWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = monitored,
                        onCheckedChange = { monitored = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue)
                    )
                }
                if (profilesState.isNotEmpty()) {
                    Text("Quality Profile", color = RadarrMuted)
                    var profileMenuOpen by remember { mutableStateOf(false) }
                    Box {
                        TvFocusButton(onClick = { profileMenuOpen = true }) {
                            Text(profilesState.find { it.id == selectedProfile }?.name ?: "Select…")
                        }
                        DropdownMenu(
                            expanded = profileMenuOpen,
                            onDismissRequest = { profileMenuOpen = false },
                            modifier = Modifier.background(RadarrSurface)
                        ) {
                            profilesState.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name, color = RadarrWhite) },
                                    onClick = { selectedProfile = p.id; profileMenuOpen = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TvFocusButton(
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            val updated = repo.editMovie(
                                movie.copy(
                                    monitored = monitored,
                                    qualityProfileId = selectedProfile,
                                    minimumAvailability = minimumAvailability
                                )
                            )
                            onSaved(updated)
                        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                            saving = false
                        }
                    }
                },
                isPrimary = true,
                enabled = !saving
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = {
            TvFocusButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = RadarrSurface
    )
}

@Composable
private fun InteractiveSearchDialog(
    movie: Movie,
    repo: RadarrRepository,
    onDismiss: () -> Unit
) {
    var releasesState by remember { mutableStateOf<UiState<List<Release>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()
    var grabbingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        releasesState = try {
            UiState.Success(
                repo.searchReleases(movie.id)
                    .sortedWith(compareBy({ it.rejected }, { -(it.qualityWeight) }))
            )
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
            UiState.Error(e.message ?: "Search failed")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Interactive Search – ${movie.title}", color = RadarrWhite, modifier = Modifier.weight(1f))
                TvFocusButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, Modifier.size(18.dp)) }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                when (val rs = releasesState) {
                    is UiState.Loading -> LoadingScreen("Searching indexers…")
                    is UiState.Error -> ErrorScreen(rs.message)
                    is UiState.Success -> {
                        if (rs.data.isEmpty()) {
                            EmptyScreen("No releases found")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(rs.data.take(100)) { release ->
                                    ReleaseRow(
                                        release = release,
                                        isGrabbing = grabbingId == release.guid,
                                        onGrab = {
                                            scope.launch {
                                                grabbingId = release.guid
                                                try {
                                                    repo.grabRelease(
                                                        GrabReleaseRequest(
                                                            guid = release.guid,
                                                            indexerId = release.indexerId,
                                                            movieId = movie.id
                                                        )
                                                    )
                                                } catch (_: Exception) {}
                                                grabbingId = null
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = RadarrSurface
    )
}

@Composable
private fun ReleaseRow(
    release: Release,
    isGrabbing: Boolean,
    onGrab: () -> Unit
) {
    val protoColor = if (release.protocol == "torrent") RadarrGreen else RadarrBlue
    val bgColor = if (release.rejected) RadarrCard.copy(alpha = 0.5f) else RadarrCard

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Title row: [badge] [title…] [Grab]
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Inline protocol badge — no fixed width, wraps its content
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(protoColor.copy(alpha = 0.2f))
                    .border(1.dp, protoColor.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    if (release.protocol == "torrent") "TRK" else "NZB",
                    style = MaterialTheme.typography.labelSmall,
                    color = protoColor
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                release.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (release.rejected) RadarrMuted else RadarrWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TvFocusButton(onClick = onGrab, enabled = !isGrabbing, isPrimary = !release.rejected) {
                if (isGrabbing) CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.Download, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Grab", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        // Single info line — all metadata joined, truncates cleanly
        val info = buildList {
            add(release.qualityName)
            if (release.indexer.isNotBlank()) add(release.indexer)
            if (release.size > 0) add(formatBytes(release.size))
            add(release.formattedAge)
            if (release.protocol == "torrent" && release.seeders != null)
                add("${release.seeders}S/${release.leechers ?: 0}L")
        }.joinToString(" · ")
        Text(info, style = MaterialTheme.typography.bodySmall, color = RadarrMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        // Rejection reason
        if (release.rejected && release.rejections.isNotEmpty()) {
            Text(
                release.rejections.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = RadarrRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
