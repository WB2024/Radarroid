package com.radarrtv.androidtv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.ui.screens.*
import com.radarrtv.androidtv.ui.screens.settings.*

object Routes {
    const val SETUP = "setup"
    const val MAIN = "main"
    const val MOVIES = "movies"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val CALENDAR = "calendar"
    const val ACTIVITY = "activity"
    const val WANTED = "wanted"
    const val COLLECTIONS = "collections"
    const val DISCOVER = "discover"
    const val SETTINGS = "settings"
    const val SETTINGS_QUALITY = "settings/quality_profiles"
    const val SETTINGS_INDEXERS = "settings/indexers"
    const val SETTINGS_DOWNLOAD_CLIENTS = "settings/download_clients"
    const val SETTINGS_NOTIFICATIONS = "settings/notifications"
    const val SETTINGS_CUSTOM_FORMATS = "settings/custom_formats"
    const val SETTINGS_IMPORT_LISTS = "settings/import_lists"
    const val SETTINGS_MEDIA_MANAGEMENT = "settings/media_management"
    const val SETTINGS_NAMING = "settings/naming"
    const val SETTINGS_TAGS = "settings/tags"
    const val SETTINGS_ROOT_FOLDERS = "settings/root_folders"
    const val SETTINGS_HOST = "settings/host"
    const val SYSTEM = "system"

    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
}

@Composable
fun RadarrNavHost(prefs: UserPreferences) {
    val rootNav = rememberNavController()
    val startDest = if (prefs.isConfigured()) Routes.MAIN else Routes.SETUP

    NavHost(navController = rootNav, startDestination = startDest) {
        composable(Routes.SETUP) {
            SetupScreen(prefs = prefs, onComplete = {
                rootNav.navigate(Routes.MAIN) {
                    popUpTo(Routes.SETUP) { inclusive = true }
                }
            })
        }
        composable(Routes.MAIN) {
            val repo = remember { RadarrRepository(prefs) }
            MainScreen(
                repo = repo,
                prefs = prefs,
                onLogout = {
                    prefs.clear()
                    rootNav.navigate(Routes.SETUP) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    repo: RadarrRepository
) {
    NavHost(navController = navController, startDestination = Routes.MOVIES) {
        composable(Routes.MOVIES) {
            MoviesScreen(repo = repo, onMovieClick = { id ->
                navController.navigate(Routes.movieDetail(id))
            })
        }
        composable(
            route = Routes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { back ->
            val movieId = back.arguments?.getInt("movieId") ?: 0
            MovieDetailScreen(
                movieId = movieId,
                repo = repo,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(repo = repo)
        }
        composable(Routes.ACTIVITY) {
            ActivityScreen(repo = repo)
        }
        composable(Routes.WANTED) {
            WantedScreen(repo = repo, onMovieClick = { id ->
                navController.navigate(Routes.movieDetail(id))
            })
        }
        composable(Routes.COLLECTIONS) {
            CollectionsScreen(repo = repo)
        }
        composable(Routes.DISCOVER) {
            DiscoverScreen(
                repo = repo,
                onMovieAdded = { id -> navController.navigate(Routes.movieDetail(id)) },
                onMovieClick = { id -> navController.navigate(Routes.movieDetail(id)) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigate = { navController.navigate(it) })
        }
        composable(Routes.SETTINGS_QUALITY) {
            QualityProfilesScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_INDEXERS) {
            IndexersScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_DOWNLOAD_CLIENTS) {
            DownloadClientsScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_NOTIFICATIONS) {
            NotificationsScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_CUSTOM_FORMATS) {
            CustomFormatsScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_IMPORT_LISTS) {
            ImportListsScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_MEDIA_MANAGEMENT) {
            MediaManagementScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_NAMING) {
            NamingScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_TAGS) {
            TagsScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_ROOT_FOLDERS) {
            RootFoldersScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_HOST) {
            HostConfigScreen(repo = repo, onBack = { navController.popBackStack() })
        }
        composable(Routes.SYSTEM) {
            SystemScreen(repo = repo)
        }
    }
}
