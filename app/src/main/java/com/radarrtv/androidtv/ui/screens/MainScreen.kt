package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.components.TvNavItem
import com.radarrtv.androidtv.ui.navigation.MainNavHost
import com.radarrtv.androidtv.ui.navigation.Routes
import com.radarrtv.androidtv.ui.theme.*

data class NavEntry(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val navEntries = listOf(
    NavEntry(Routes.MOVIES, "Movies", Icons.Default.Movie),
    NavEntry(Routes.TITLE_FINDER, "Title Finder", Icons.Default.Explore),
    NavEntry(Routes.DISCOVER, "Add Movie", Icons.Default.AddCircle),
    NavEntry(Routes.CALENDAR, "Calendar", Icons.Default.CalendarMonth),
    NavEntry(Routes.ACTIVITY, "Activity", Icons.Default.Download),
    NavEntry(Routes.WANTED, "Wanted", Icons.Default.Warning),
    NavEntry(Routes.COLLECTIONS, "Collections", Icons.Default.Collections),
    NavEntry(Routes.SETTINGS, "Settings", Icons.Default.Settings),
    NavEntry(Routes.SYSTEM, "System", Icons.Default.Info)
)

@Composable
fun MainScreen(
    repo: RadarrRepository,
    prefs: UserPreferences,
    onLogout: () -> Unit
) {
    val innerNav = rememberNavController()
    val backStackEntry by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.MOVIES

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(RadarrBg)
    ) {
        // Sidebar
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(RadarrSurfaceVariant)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "RADARR",
                style = MaterialTheme.typography.titleLarge,
                color = RadarrBlue,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                prefs.serverUrl.removePrefix("http://").removePrefix("https://").substringBefore("/"),
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(20.dp))

            navEntries.forEach { entry ->
                val isSelected = currentRoute.startsWith(entry.route)
                TvNavItem(
                    icon = entry.icon,
                    label = entry.label,
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            innerNav.navigate(entry.route) {
                                popUpTo(Routes.MOVIES) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = RadarrBorder)
            Spacer(Modifier.height(8.dp))
            TvNavItem(
                icon = Icons.Default.Logout,
                label = "Disconnect",
                selected = false,
                onClick = onLogout
            )
            Spacer(Modifier.height(8.dp))
        }

        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            MainNavHost(navController = innerNav, repo = repo, prefs = prefs)
        }
    }
}
