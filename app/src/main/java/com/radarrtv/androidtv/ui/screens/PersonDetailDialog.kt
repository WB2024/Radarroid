package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.repository.TmdbRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PersonDetailDialog(
    personId: Int,
    tmdbRepo: TmdbRepository,
    libraryMap: Map<Int, Int>,
    onDismiss: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    var personState by remember { mutableStateOf<UiState<TmdbPersonDetail>>(UiState.Loading) }
    var creditsState by remember { mutableStateOf<UiState<TmdbPersonMovieCredits>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(personId) {
        launch {
            personState = try {
                UiState.Success(tmdbRepo.getPersonDetail(personId))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed")
            }
        }
        launch {
            creditsState = try {
                UiState.Success(tmdbRepo.getPersonMovieCredits(personId))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed")
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RadarrSurface)
        ) {
            when (val ps = personState) {
                is UiState.Loading -> LoadingScreen("Loading person…")
                is UiState.Error -> ErrorScreen(ps.message, onRetry = {
                    personState = UiState.Loading
                    scope.launch {
                        personState = try { UiState.Success(tmdbRepo.getPersonDetail(personId)) }
                        catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                    }
                })
                is UiState.Success -> {
                    val person = ps.data
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left panel: photo + bio
                        Column(
                            modifier = Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                                .background(RadarrBg)
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Profile photo
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .background(RadarrCard)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                if (person.profilePath != null) {
                                    AsyncImage(
                                        model = tmdbRepo.profileUrl(person.profilePath),
                                        contentDescription = person.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person, null,
                                        tint = RadarrMuted,
                                        modifier = Modifier.align(Alignment.Center).size(60.dp)
                                    )
                                }
                            }

                            Text(
                                person.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = RadarrWhite,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (!person.knownForDepartment.isNullOrBlank()) {
                                Text(
                                    person.knownForDepartment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RadarrBlue,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            HorizontalDivider(color = RadarrBorder)

                            if (!person.birthday.isNullOrBlank()) {
                                Column {
                                    Text("Born", style = MaterialTheme.typography.labelSmall, color = RadarrMuted)
                                    Text(person.birthday, style = MaterialTheme.typography.bodySmall, color = RadarrWhite)
                                }
                            }
                            if (!person.deathday.isNullOrBlank()) {
                                Column {
                                    Text("Died", style = MaterialTheme.typography.labelSmall, color = RadarrMuted)
                                    Text(person.deathday!!, style = MaterialTheme.typography.bodySmall, color = RadarrWhite)
                                }
                            }
                            if (!person.placeOfBirth.isNullOrBlank()) {
                                Column {
                                    Text("Birthplace", style = MaterialTheme.typography.labelSmall, color = RadarrMuted)
                                    Text(person.placeOfBirth!!, style = MaterialTheme.typography.bodySmall, color = RadarrWhite)
                                }
                            }

                            if (person.biography.isNotBlank()) {
                                HorizontalDivider(color = RadarrBorder)
                                Text("Biography", style = MaterialTheme.typography.labelSmall, color = RadarrMuted)
                                Text(
                                    person.biography.take(800).let { if (person.biography.length > 800) "$it…" else it },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RadarrWhite.copy(alpha = 0.85f),
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                                )
                            }
                        }

                        // Right panel: filmography grid
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filmography",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = RadarrWhite
                                )
                                TvFocusButton(onClick = onDismiss) {
                                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            when (val cs = creditsState) {
                                is UiState.Loading -> LoadingScreen("Loading filmography…")
                                is UiState.Error -> Text("Failed to load filmography", color = RadarrMuted)
                                is UiState.Success -> {
                                    val films = (cs.data.cast + cs.data.crew)
                                        .distinctBy { it.id }
                                        .filter { !it.title.isNullOrBlank() && it.voteCount > 10 }
                                        .sortedByDescending { it.popularity }
                                        .take(40)

                                    if (films.isEmpty()) {
                                        Text("No films found", color = RadarrMuted)
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Adaptive(minSize = 140.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(films, key = { it.id }) { movie ->
                                                FilmographyCard(
                                                    movie = movie,
                                                    tmdbRepo = tmdbRepo,
                                                    inLibrary = libraryMap.containsKey(movie.id),
                                                    onClick = { onMovieClick(movie.id) }
                                                )
                                            }
                                        }
                                    }
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
private fun FilmographyCard(
    movie: TmdbMovie,
    tmdbRepo: TmdbRepository,
    inLibrary: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
            .border(
                2.dp,
                if (isFocused) RadarrBlue else if (inLibrary) RadarrGreen.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        if (movie.posterPath != null) {
            AsyncImage(
                model = tmdbRepo.posterUrl(movie.posterPath),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.Movie, null,
                tint = RadarrMuted,
                modifier = Modifier.align(Alignment.Center).size(32.dp)
            )
        }

        // Bottom overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(6.dp)
        ) {
            Column {
                Text(
                    movie.title ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadarrWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (movie.year.isNotBlank()) {
                    Text(movie.year, style = MaterialTheme.typography.labelSmall, color = RadarrMuted)
                }
            }
        }

        if (inLibrary) {
            Icon(
                Icons.Default.CheckCircle, null,
                tint = RadarrGreen,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp)
            )
        }

        if (isFocused && movie.voteAverage > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RadarrBg.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = RadarrYellow, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(2.dp))
                Text("${"%.1f".format(movie.voteAverage)}", style = MaterialTheme.typography.labelSmall, color = RadarrYellow)
            }
        }
    }
}
