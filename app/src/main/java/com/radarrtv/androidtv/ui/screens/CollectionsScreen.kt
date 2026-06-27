package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.model.MovieCollection
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*

@Composable
fun CollectionsScreen(repo: RadarrRepository) {
    var state by remember { mutableStateOf<UiState<List<MovieCollection>>>(UiState.Loading) }

    LaunchedEffect(Unit) {
        state = try { UiState.Success(repo.getCollections()) }
        catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Collections", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) {
                state = UiState.Loading
            }
            is UiState.Success -> {
                val collections = s.data
                Text(
                    "${collections.size} collections",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RadarrMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (collections.isEmpty()) {
                    EmptyScreen("No collections found")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 240.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(collections, key = { it.id }) { collection ->
                            CollectionCard(collection = collection)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(collection: MovieCollection) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RadarrCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(RadarrSurfaceVariant)
        ) {
            val posterUrl = collection.posterUrl
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = collection.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                StatusBadge(
                    "${collection.downloadedCount}/${collection.movieCount}",
                    if (collection.downloadedCount == collection.movieCount) RadarrGreen else RadarrOrange
                )
            }
            if (collection.monitored) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    StatusBadge("Monitored", RadarrBlue)
                }
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                collection.title,
                style = MaterialTheme.typography.titleSmall,
                color = RadarrWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${collection.movieCount} movies · ${collection.downloadedCount} downloaded",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )
        }
    }
}
