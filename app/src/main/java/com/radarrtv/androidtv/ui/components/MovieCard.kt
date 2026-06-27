package com.radarrtv.androidtv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.model.Movie
import com.radarrtv.androidtv.ui.theme.*

@Composable
fun MovieCard(
    movie: Movie,
    posterUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isFocused) RadarrBlue else Color.Transparent,
        label = "cardBorder"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(3.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(RadarrCard)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (movie.hasFile) {
                Box(Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                    StatusBadge("✓", RadarrGreen)
                }
            } else if (movie.monitored) {
                Box(Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                    StatusBadge("●", RadarrOrange)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isFocused) RadarrSurface else RadarrCard)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (movie.year > 0) "${movie.title} (${movie.year})" else movie.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFocused) RadarrWhite else RadarrMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
