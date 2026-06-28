package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.TmdbApiClient
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.data.repository.TmdbRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TmdbMovieDetailDialog(
    tmdbId: Int,
    tmdbRepo: TmdbRepository,
    radarrRepo: RadarrRepository,
    libraryMap: Map<Int, Int>,
    onLibraryUpdated: (tmdbId: Int, radarrId: Int) -> Unit,
    onDismiss: () -> Unit,
    onBrowseSimilar: (tmdbId: Int, title: String) -> Unit = { _, _ -> },
    onBrowseRecommendations: (tmdbId: Int, title: String) -> Unit = { _, _ -> }
) {
    var detailState by remember { mutableStateOf<UiState<TmdbMovieDetail>>(UiState.Loading) }
    var showAddDialog by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf<List<QualityProfile>>(emptyList()) }
    var rootFolders by remember { mutableStateOf<List<RootFolder>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tmdbId) {
        detailState = try {
            UiState.Success(tmdbRepo.getMovieDetail(tmdbId))
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load details")
        }
    }

    LaunchedEffect(Unit) {
        launch { try { profiles = radarrRepo.getQualityProfiles() } catch (_: Exception) {} }
        launch { try { rootFolders = radarrRepo.getRootFolders() } catch (_: Exception) {} }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RadarrSurface)
        ) {
            when (val s = detailState) {
                is UiState.Loading -> LoadingScreen("Loading details…")
                is UiState.Error -> ErrorScreen(s.message, onRetry = {
                    detailState = UiState.Loading
                    scope.launch {
                        detailState = try {
                            UiState.Success(tmdbRepo.getMovieDetail(tmdbId))
                        } catch (e: Exception) {
                            UiState.Error(e.message ?: "Failed")
                        }
                    }
                })
                is UiState.Success -> {
                    val movie = s.data
                    val inLibrary = libraryMap.containsKey(movie.id)
                    val radarrId = libraryMap[movie.id]

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Poster panel
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        ) {
                            if (movie.backdropPath != null) {
                                AsyncImage(
                                    model = tmdbRepo.backdropUrl(movie.backdropPath),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(
                                                    androidx.compose.ui.graphics.Color.Transparent,
                                                    RadarrSurface
                                                ),
                                                startX = 500f
                                            )
                                        )
                                )
                            }
                            if (movie.posterPath != null) {
                                AsyncImage(
                                    model = tmdbRepo.posterUrl(movie.posterPath),
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(220.dp)
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }

                        // Detail panel
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Close button
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TvFocusButton(onClick = onDismiss) {
                                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                                }
                            }

                            // Title + year
                            Text(
                                movie.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = RadarrWhite
                            )
                            if (!movie.tagline.isNullOrBlank()) {
                                Text(
                                    "\"${movie.tagline}\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = RadarrMuted,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            // Meta row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (movie.year.isNotBlank()) StatusBadge(movie.year, RadarrBlueDark)
                                if (movie.runtime != null && movie.runtime > 0) {
                                    StatusBadge("${movie.runtime}m", RadarrCard)
                                }
                                if (movie.voteAverage > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = RadarrYellow, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            "${"%.1f".format(movie.voteAverage)} (${movie.voteCount})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RadarrYellow
                                        )
                                    }
                                }
                                if (!movie.status.isNullOrBlank()) StatusBadge(movie.status, RadarrMuted)
                            }

                            // Genres
                            if (movie.genres.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    movie.genres.forEach { genre ->
                                        StatusBadge(genre.name, RadarrBlueDark)
                                    }
                                }
                            }

                            // Overview
                            if (!movie.overview.isNullOrBlank()) {
                                Text(
                                    movie.overview,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = RadarrWhite.copy(alpha = 0.85f)
                                )
                            }

                            // Stats
                            if (movie.budget > 0 || movie.revenue > 0) {
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    if (movie.budget > 0) {
                                        Column {
                                            Text("Budget", style = MaterialTheme.typography.bodySmall, color = RadarrMuted)
                                            Text(formatMillions(movie.budget), style = MaterialTheme.typography.bodyMedium, color = RadarrWhite)
                                        }
                                    }
                                    if (movie.revenue > 0) {
                                        Column {
                                            Text("Revenue", style = MaterialTheme.typography.bodySmall, color = RadarrMuted)
                                            Text(formatMillions(movie.revenue), style = MaterialTheme.typography.bodyMedium, color = RadarrWhite)
                                        }
                                    }
                                    if (!movie.originalLanguage.isNullOrBlank()) {
                                        Column {
                                            Text("Language", style = MaterialTheme.typography.bodySmall, color = RadarrMuted)
                                            Text(movie.originalLanguage.uppercase(), style = MaterialTheme.typography.bodyMedium, color = RadarrWhite)
                                        }
                                    }
                                }
                            }

                            // Studios
                            if (movie.productionCompanies.isNotEmpty()) {
                                Text("Studios", style = MaterialTheme.typography.bodySmall, color = RadarrMuted)
                                Text(
                                    movie.productionCompanies.take(3).joinToString(" · ") { it.name },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RadarrWhite
                                )
                            }

                            // Cast
                            val cast = movie.credits?.cast?.take(12) ?: emptyList()
                            if (cast.isNotEmpty()) {
                                SectionHeader("Cast")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(cast) { member ->
                                        CastChip(member, tmdbRepo)
                                    }
                                }
                            }

                            // Director
                            val directors = movie.credits?.crew?.filter { it.job == "Director" } ?: emptyList()
                            if (directors.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Director:", style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                                    Text(directors.joinToString(", ") { it.name }, style = MaterialTheme.typography.bodyMedium, color = RadarrWhite)
                                }
                            }

                            // Action buttons
                            HorizontalDivider(color = RadarrBorder)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (inLibrary) {
                                    TvFocusButton(onClick = {}, isPrimary = false) {
                                        Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = RadarrGreen)
                                        Spacer(Modifier.width(6.dp))
                                        Text("In Library", color = RadarrGreen)
                                    }
                                } else {
                                    TvFocusButton(onClick = { showAddDialog = true }, isPrimary = true) {
                                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Add to Radarr")
                                    }
                                }
                                TvFocusButton(onClick = { onBrowseSimilar(movie.id, movie.title) }) {
                                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Similar")
                                }
                                TvFocusButton(onClick = { onBrowseRecommendations(movie.id, movie.title) }) {
                                    Icon(Icons.Default.Recommend, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Recommended")
                                }
                                if (!movie.imdbId.isNullOrBlank()) {
                                    StatusBadge("IMDb: ${movie.imdbId}", RadarrMuted)
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    if (showAddDialog) {
                        val detail = s.data
                        AddToRadarrDialog(
                            tmdbMovie = detail,
                            profiles = profiles,
                            rootFolders = rootFolders,
                            onAdd = { req ->
                                scope.launch {
                                    try {
                                        val added = radarrRepo.addMovie(req)
                                        onLibraryUpdated(added.tmdbId, added.id)
                                        showAddDialog = false
                                    } catch (_: Exception) {
                                        showAddDialog = false
                                    }
                                }
                            },
                            onDismiss = { showAddDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CastChip(member: TmdbCastMember, tmdbRepo: TmdbRepository) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(RadarrCard)
        ) {
            if (member.profilePath != null) {
                AsyncImage(
                    model = tmdbRepo.profileUrl(member.profilePath),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = RadarrMuted,
                    modifier = Modifier.align(Alignment.Center).size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            member.name,
            style = MaterialTheme.typography.labelSmall,
            color = RadarrWhite,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!member.character.isNullOrBlank()) {
            Text(
                member.character,
                style = MaterialTheme.typography.labelSmall,
                color = RadarrMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddToRadarrDialog(
    tmdbMovie: TmdbMovieDetail,
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
                "Add ${tmdbMovie.title}${if (tmdbMovie.year.isNotBlank()) " (${tmdbMovie.year})" else ""}",
                color = RadarrWhite
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (!tmdbMovie.overview.isNullOrBlank()) {
                    Text(
                        tmdbMovie.overview.take(200) + if (tmdbMovie.overview.length > 200) "…" else "",
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
                        DropdownMenu(expanded = profileMenuOpen, onDismissRequest = { profileMenuOpen = false }, modifier = Modifier.background(RadarrSurface)) {
                            profiles.forEach { p ->
                                DropdownMenuItem(text = { Text(p.name, color = RadarrWhite) }, onClick = { selectedProfile = p.id; profileMenuOpen = false })
                            }
                        }
                    }
                }
                if (rootFolders.isNotEmpty()) {
                    Text("Root Folder", color = RadarrMuted, style = MaterialTheme.typography.bodyMedium)
                    Box {
                        TvFocusButton(onClick = { folderMenuOpen = true }) {
                            Text(selectedFolder.ifBlank { "Select…" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = folderMenuOpen, onDismissRequest = { folderMenuOpen = false }, modifier = Modifier.background(RadarrSurface)) {
                            rootFolders.forEach { f ->
                                DropdownMenuItem(text = { Text(f.path, color = RadarrWhite) }, onClick = { selectedFolder = f.path; folderMenuOpen = false })
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Monitored", color = RadarrWhite, modifier = Modifier.weight(1f))
                    Switch(checked = monitored, onCheckedChange = { monitored = it }, colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Search on add", color = RadarrWhite, modifier = Modifier.weight(1f))
                    Switch(checked = searchOnAdd, onCheckedChange = { searchOnAdd = it }, colors = SwitchDefaults.colors(checkedThumbColor = RadarrBlue))
                }
            }
        },
        confirmButton = {
            TvFocusButton(
                onClick = {
                    adding = true
                    onAdd(
                        AddMovieRequest(
                            title = tmdbMovie.title,
                            tmdbId = tmdbMovie.id,
                            year = tmdbMovie.year.toIntOrNull() ?: 0,
                            qualityProfileId = selectedProfile,
                            rootFolderPath = selectedFolder,
                            monitored = monitored,
                            addOptions = AddOptions(searchForMovie = searchOnAdd),
                            images = tmdbMovie.posterPath?.let {
                                listOf(MediaCover(coverType = "poster", url = TmdbApiClient.posterUrl(it)))
                            } ?: emptyList()
                        )
                    )
                },
                isPrimary = true,
                enabled = !adding && selectedFolder.isNotBlank()
            ) {
                if (adding) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("+ Add to Radarr")
            }
        },
        dismissButton = { TvFocusButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = RadarrSurface
    )
}

private fun formatMillions(value: Long): String =
    "$" + when {
        value >= 1_000_000_000L -> "%.1fB".format(value / 1_000_000_000.0)
        value >= 1_000_000L -> "%.0fM".format(value / 1_000_000.0)
        else -> value.toString()
    }
