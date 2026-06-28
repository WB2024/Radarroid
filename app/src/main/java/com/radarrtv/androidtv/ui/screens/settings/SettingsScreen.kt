package com.radarrtv.androidtv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.radarrtv.androidtv.ui.components.TvFocusSurface
import com.radarrtv.androidtv.ui.navigation.Routes
import com.radarrtv.androidtv.ui.theme.*

data class SettingsEntry(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val settingsEntries = listOf(
    SettingsEntry(Routes.SETTINGS_MEDIA_MANAGEMENT, "Media Management", "Naming, importing, and root folders", Icons.Default.FolderOpen),
    SettingsEntry(Routes.SETTINGS_NAMING, "Naming", "File and folder naming conventions", Icons.Default.DriveFileRenameOutline),
    SettingsEntry(Routes.SETTINGS_QUALITY, "Quality Profiles", "Define quality preferences and cutoffs", Icons.Default.HighQuality),
    SettingsEntry(Routes.SETTINGS_CUSTOM_FORMATS, "Custom Formats", "Advanced quality classification rules", Icons.Default.Tune),
    SettingsEntry(Routes.SETTINGS_INDEXERS, "Indexers", "Configure NZB and torrent indexers", Icons.Default.RssFeed),
    SettingsEntry(Routes.SETTINGS_DOWNLOAD_CLIENTS, "Download Clients", "Configure download applications", Icons.Default.CloudDownload),
    SettingsEntry(Routes.SETTINGS_IMPORT_LISTS, "Import Lists", "Automatic movie addition from lists", Icons.Default.List),
    SettingsEntry(Routes.SETTINGS_NOTIFICATIONS, "Notifications", "Alert and push notification connections", Icons.Default.Notifications),
    SettingsEntry(Routes.SETTINGS_TAGS, "Tags", "Manage tags for organizing content", Icons.Default.Label),
    SettingsEntry(Routes.SETTINGS_ROOT_FOLDERS, "Root Folders", "Manage media library locations", Icons.Default.Storage),
    SettingsEntry(Routes.SETTINGS_HOST, "General", "Host, URL, and authentication settings", Icons.Default.Settings),
    SettingsEntry(Routes.SETTINGS_TMDB, "TMDB", "The Movie Database API key for metadata", Icons.Default.Movie)
)

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(settingsEntries.size) { idx ->
                val entry = settingsEntries[idx]
                SettingsItem(
                    entry = entry,
                    onClick = { onNavigate(entry.route) }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(entry: SettingsEntry, onClick: () -> Unit) {
    TvFocusSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        defaultBg = RadarrCard,
        focusedBg = RadarrSurface
    ) { isFocused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused) RadarrBlueDark else RadarrSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = if (isFocused) RadarrWhite else RadarrBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isFocused) RadarrWhite else RadarrWhite.copy(alpha = 0.9f)
                )
                Text(
                    entry.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RadarrMuted
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = if (isFocused) RadarrBlue else RadarrMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
