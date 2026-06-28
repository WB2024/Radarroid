package com.radarrtv.androidtv.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radarrtv.androidtv.data.api.model.TmdbGenre
import com.radarrtv.androidtv.data.api.model.TmdbMovie
import com.radarrtv.androidtv.data.api.model.TmdbPagedResult
import com.radarrtv.androidtv.data.repository.RadarrRepository
import com.radarrtv.androidtv.data.repository.TmdbRepository
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.ui.components.*
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Mode Definition ──────────────────────────────────────────────────────────

private enum class FinderMode(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SEARCH("Search", Icons.Default.Search),
    TRENDING("Trending", Icons.Default.TrendingUp),
    POPULAR("Popular", Icons.Default.LocalFireDepartment),
    TOP_RATED("Top Rated", Icons.Default.Star),
    NOW_PLAYING("In Cinemas", Icons.Default.Theaters),
    UPCOMING("Upcoming", Icons.Default.EventAvailable),
    DISCOVER("Discover", Icons.Default.Tune),
    BY_PERSON("By Person", Icons.Default.Person),
    BY_COLLECTION("Collections", Icons.Default.Collections),
    BY_COMPANY("By Studio", Icons.Default.Business),
    BY_KEYWORD("By Keyword", Icons.Default.Tag)
}

// ── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun TitleFinderScreen(
    radarrRepo: RadarrRepository,
    prefs: UserPreferences,
    onMovieAdded: (radarrId: Int) -> Unit = {}
) {
    val tmdbRepo = remember(prefs.tmdbApiKey) { TmdbRepository(prefs) }
    var activeMode by remember { mutableStateOf(FinderMode.TRENDING) }
    // Maps tmdbId → Radarr library id
    var libraryMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var selectedTmdbId by remember { mutableStateOf<Int?>(null) }
    // For similar/recommendations launched from the detail dialog
    var similarContext by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var recommendContext by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        launch { libraryMap = try { radarrRepo.getMovies().associate { it.tmdbId to it.id } } catch (_: Exception) { emptyMap() } }
    }

    if (prefs.tmdbApiKey.isBlank()) {
        NoTmdbKeyWarning()
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
        // Header
        Text("Title Finder", style = MaterialTheme.typography.headlineMedium, color = RadarrWhite)
        Spacer(Modifier.height(12.dp))

        // Mode tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinderMode.values().forEach { mode ->
                ModeTab(
                    mode = mode,
                    selected = activeMode == mode,
                    onClick = {
                        activeMode = mode
                        similarContext = null
                        recommendContext = null
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Contextual sub-title for similar/recommend
        val contextLabel = when {
            activeMode == FinderMode.DISCOVER && similarContext != null ->
                "Similar to: ${similarContext!!.second}"
            activeMode == FinderMode.DISCOVER && recommendContext != null ->
                "Recommended from: ${recommendContext!!.second}"
            else -> null
        }
        if (contextLabel != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(contextLabel, style = MaterialTheme.typography.bodyMedium, color = RadarrMuted)
                Spacer(Modifier.width(8.dp))
                TvFocusButton(onClick = { similarContext = null; recommendContext = null }) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Content
        when (activeMode) {
            FinderMode.SEARCH -> SearchMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
            FinderMode.TRENDING -> TrendingMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
            FinderMode.POPULAR -> PagedListMode("Popular", libraryMap, { tmdbRepo.getPopular(it) }) { selectedTmdbId = it }
            FinderMode.TOP_RATED -> PagedListMode("Top Rated", libraryMap, { tmdbRepo.getTopRated(it) }) { selectedTmdbId = it }
            FinderMode.NOW_PLAYING -> PagedListMode("In Cinemas", libraryMap, { tmdbRepo.getNowPlaying(it) }) { selectedTmdbId = it }
            FinderMode.UPCOMING -> PagedListMode("Upcoming", libraryMap, { tmdbRepo.getUpcoming(it) }) { selectedTmdbId = it }
            FinderMode.DISCOVER -> DiscoverMode(tmdbRepo, libraryMap, similarContext, recommendContext) { selectedTmdbId = it }
            FinderMode.BY_PERSON -> ByPersonMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
            FinderMode.BY_COLLECTION -> ByCollectionMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
            FinderMode.BY_COMPANY -> ByCompanyMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
            FinderMode.BY_KEYWORD -> ByKeywordMode(tmdbRepo, libraryMap) { selectedTmdbId = it }
        }
    }

    // Detail dialog
    selectedTmdbId?.let { tmdbId ->
        TmdbMovieDetailDialog(
            tmdbId = tmdbId,
            tmdbRepo = tmdbRepo,
            radarrRepo = radarrRepo,
            libraryMap = libraryMap,
            onLibraryUpdated = { tid, rid ->
                libraryMap = libraryMap + (tid to rid)
                onMovieAdded(rid)
            },
            onDismiss = { selectedTmdbId = null },
            onBrowseSimilar = { id, title ->
                similarContext = id to title
                recommendContext = null
                activeMode = FinderMode.DISCOVER
                selectedTmdbId = null
            },
            onBrowseRecommendations = { id, title ->
                recommendContext = id to title
                similarContext = null
                activeMode = FinderMode.DISCOVER
                selectedTmdbId = null
            }
        )
    }
}

// ── Mode Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun ModeTab(mode: FinderMode, selected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        when { selected -> RadarrBlueDark; isFocused -> RadarrSurface; else -> RadarrCard },
        label = "tabBg"
    )
    val contentColor = if (selected || isFocused) RadarrWhite else RadarrMuted
    val borderColor by animateColorAsState(
        if (isFocused) RadarrBlue else Color.Transparent, label = "tabBorder"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(mode.icon, null, tint = contentColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(mode.label, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}

// ── No Key Warning ───────────────────────────────────────────────────────────

@Composable
private fun NoTmdbKeyWarning() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Key, null, tint = RadarrOrange, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("TMDB API Key Required", style = MaterialTheme.typography.titleLarge, color = RadarrWhite)
            Spacer(Modifier.height(8.dp))
            Text(
                "Add your TMDB API access token in Settings → TMDB to use Title Finder.",
                style = MaterialTheme.typography.bodyLarge,
                color = RadarrMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Results Grid + Pagination ─────────────────────────────────────────────────

@Composable
private fun ResultsGrid(
    movies: List<TmdbMovie>,
    libraryMap: Map<Int, Int>,
    page: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Pagination bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                "${movies.size} results",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted,
                modifier = Modifier.weight(1f)
            )
            TvFocusButton(onClick = onPrev, enabled = page > 1) {
                Icon(Icons.Default.ChevronLeft, null, Modifier.size(18.dp))
            }
            Text(
                "  $page / $totalPages  ",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrWhite
            )
            TvFocusButton(onClick = onNext, enabled = page < totalPages) {
                Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies, key = { it.id }) { movie ->
                TmdbMovieCard(
                    movie = movie,
                    inLibrary = libraryMap.containsKey(movie.id),
                    onClick = { onMovieClick(movie.id) }
                )
            }
        }
    }
}

// ── TMDB Movie Card ───────────────────────────────────────────────────────────

@Composable
fun TmdbMovieCard(
    movie: TmdbMovie,
    inLibrary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isFocused) RadarrBlue else Color.Transparent, label = "cardBorder"
    )
    val posterUrl = remember(movie.posterPath) {
        if (movie.posterPath != null) "https://image.tmdb.org/t/p/w342${movie.posterPath}" else ""
    }

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
            if (posterUrl.isNotBlank()) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Movie, null, tint = RadarrBorder, modifier = Modifier.align(Alignment.Center).size(40.dp))
            }
            // Library / rating badges
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (inLibrary) StatusBadge("✓", RadarrGreen)
                if (movie.voteAverage > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RadarrBg.copy(alpha = 0.85f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "%.1f".format(movie.voteAverage),
                            style = MaterialTheme.typography.labelSmall,
                            color = RadarrYellow
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isFocused) RadarrSurface else RadarrCard)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFocused) RadarrWhite else RadarrWhite.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (movie.year.isNotBlank()) {
                    Text(
                        movie.year,
                        style = MaterialTheme.typography.bodySmall,
                        color = RadarrMuted
                    )
                }
            }
        }
    }
}

// ── SEARCH MODE ───────────────────────────────────────────────────────────────

@Composable
private fun SearchMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var yearFilter by remember { mutableStateOf("") }
    var resultsState by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>>(UiState.Success(TmdbPagedResult())) }
    var page by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun search(p: Int = 1) {
        if (query.length < 2) return
        page = p
        searchJob?.cancel()
        resultsState = UiState.Loading
        searchJob = scope.launch {
            delay(if (p == 1) 400L else 0L)
            resultsState = try {
                UiState.Success(tmdbRepo.searchMovies(query, p, yearFilter.toIntOrNull()))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Search failed")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; if (it.length >= 2) search(1) else if (it.isEmpty()) resultsState = UiState.Success(TmdbPagedResult()) },
                placeholder = { Text("Search movies…", color = RadarrMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = searchFieldColors()
            )
            OutlinedTextField(
                value = yearFilter,
                onValueChange = { yearFilter = it; if (query.length >= 2) search(1) },
                placeholder = { Text("Year", color = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.width(100.dp),
                colors = searchFieldColors()
            )
        }
        Spacer(Modifier.height(12.dp))

        when (val s = resultsState) {
            is UiState.Loading -> LoadingScreen("Searching…")
            is UiState.Error -> ErrorScreen(s.message)
            is UiState.Success -> {
                val result = s.data
                if (result.results.isEmpty() && query.length >= 2) {
                    EmptyScreen("No results for \"$query\"")
                } else if (result.results.isEmpty()) {
                    EmptyScreen("Type at least 2 characters to search")
                } else {
                    ResultsGrid(
                        movies = result.results,
                        libraryMap = libraryMap,
                        page = result.page,
                        totalPages = result.totalPages,
                        onPrev = { search(page - 1) },
                        onNext = { search(page + 1) },
                        onMovieClick = onMovieClick
                    )
                }
            }
        }
    }
}

// ── TRENDING MODE ─────────────────────────────────────────────────────────────

@Composable
private fun TrendingMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var timeWindow by remember { mutableStateOf("week") }
    var page by remember { mutableIntStateOf(1) }
    var state by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(tmdbRepo.getTrending(timeWindow, page)) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(timeWindow, page) { load() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            TvFocusButton(onClick = { timeWindow = "day"; page = 1 }, isPrimary = timeWindow == "day") { Text("Today") }
            TvFocusButton(onClick = { timeWindow = "week"; page = 1 }, isPrimary = timeWindow == "week") { Text("This Week") }
        }
        PagedContent(state, libraryMap, page, onPrev = { page-- }, onNext = { page++ }, onMovieClick = onMovieClick)
    }
}

// ── PAGED LIST MODE (Popular / Top Rated / Now Playing / Upcoming) ─────────────

@Composable
private fun PagedListMode(
    label: String,
    libraryMap: Map<Int, Int>,
    fetcher: suspend (Int) -> TmdbPagedResult<TmdbMovie>,
    onMovieClick: (Int) -> Unit
) {
    var page by remember { mutableIntStateOf(1) }
    var state by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            state = UiState.Loading
            state = try { UiState.Success(fetcher(page)) }
            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(page) { load() }

    PagedContent(state, libraryMap, page, onPrev = { page-- }, onNext = { page++ }, onMovieClick = onMovieClick)
}

// ── DISCOVER MODE ─────────────────────────────────────────────────────────────

private val SORT_OPTIONS = listOf(
    "popularity.desc" to "Most Popular",
    "popularity.asc" to "Least Popular",
    "vote_average.desc" to "Highest Rated",
    "vote_average.asc" to "Lowest Rated",
    "primary_release_date.desc" to "Newest",
    "primary_release_date.asc" to "Oldest",
    "revenue.desc" to "Highest Grossing",
    "title.asc" to "Title A-Z",
    "title.desc" to "Title Z-A"
)

@Composable
private fun DiscoverMode(
    tmdbRepo: TmdbRepository,
    libraryMap: Map<Int, Int>,
    similarContext: Pair<Int, String>?,
    recommendContext: Pair<Int, String>?,
    onMovieClick: (Int) -> Unit
) {
    var genres by remember { mutableStateOf<List<TmdbGenre>>(emptyList()) }
    var selectedGenres by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var sortBy by remember { mutableStateOf("popularity.desc") }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var yearFilter by remember { mutableStateOf("") }
    var minRating by remember { mutableStateOf("") }
    var minVotes by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(1) }
    var state by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        genres = try { tmdbRepo.getGenres() } catch (_: Exception) { emptyList() }
    }

    // Auto-run when similar/recommend context changes
    LaunchedEffect(similarContext, recommendContext) {
        page = 1
        if (similarContext != null || recommendContext != null) {
            state = UiState.Loading
            state = try {
                val result = when {
                    similarContext != null -> tmdbRepo.getSimilar(similarContext.first, 1)
                    else -> tmdbRepo.getRecommendations(recommendContext!!.first, 1)
                }
                UiState.Success(result)
            } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    fun runDiscover(p: Int = 1) {
        page = p
        scope.launch {
            state = UiState.Loading
            state = try {
                if (similarContext != null) {
                    UiState.Success(tmdbRepo.getSimilar(similarContext.first, p))
                } else if (recommendContext != null) {
                    UiState.Success(tmdbRepo.getRecommendations(recommendContext.first, p))
                } else {
                    UiState.Success(
                        tmdbRepo.discover(
                            sortBy = sortBy,
                            genreIds = selectedGenres.toList(),
                            year = yearFilter.toIntOrNull(),
                            minRating = minRating.toFloatOrNull(),
                            minVotes = minVotes.toIntOrNull(),
                            language = language.trim().ifBlank { null },
                            page = p
                        )
                    )
                }
            } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    LaunchedEffect(page) {
        if (similarContext != null || recommendContext != null) {
            scope.launch {
                state = UiState.Loading
                state = try {
                    val result = when {
                        similarContext != null -> tmdbRepo.getSimilar(similarContext.first, page)
                        else -> tmdbRepo.getRecommendations(recommendContext!!.first, page)
                    }
                    UiState.Success(result)
                } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filters (only show when not in similar/recommend context)
        if (similarContext == null && recommendContext == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RadarrCard)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sort
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sort:", color = RadarrMuted, style = MaterialTheme.typography.bodyMedium)
                    Box {
                        TvFocusButton(onClick = { sortMenuOpen = true }) {
                            Text(SORT_OPTIONS.find { it.first == sortBy }?.second ?: sortBy)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }, modifier = Modifier.background(RadarrSurface)) {
                            SORT_OPTIONS.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (value == sortBy) RadarrBlue else RadarrWhite) },
                                    onClick = { sortBy = value; sortMenuOpen = false }
                                )
                            }
                        }
                    }
                }

                // Genre chips
                if (genres.isNotEmpty()) {
                    Text("Genres:", color = RadarrMuted, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { genre ->
                            val selected = selectedGenres.contains(genre.id)
                            GenreChip(genre.name, selected) {
                                selectedGenres = if (selected) selectedGenres - genre.id else selectedGenres + genre.id
                            }
                        }
                    }
                }

                // Extra filters row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = yearFilter,
                        onValueChange = { yearFilter = it },
                        placeholder = { Text("Year", color = RadarrMuted) },
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        colors = searchFieldColors()
                    )
                    OutlinedTextField(
                        value = minRating,
                        onValueChange = { minRating = it },
                        placeholder = { Text("Min ★", color = RadarrMuted) },
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        colors = searchFieldColors()
                    )
                    OutlinedTextField(
                        value = minVotes,
                        onValueChange = { minVotes = it },
                        placeholder = { Text("Min votes", color = RadarrMuted) },
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        colors = searchFieldColors()
                    )
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        placeholder = { Text("Lang (en)", color = RadarrMuted) },
                        singleLine = true,
                        modifier = Modifier.width(110.dp),
                        colors = searchFieldColors()
                    )
                    TvFocusButton(onClick = { runDiscover(1) }, isPrimary = true) {
                        Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Discover")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        PagedContent(state, libraryMap, page, onPrev = { page-- }, onNext = { page++ }, onMovieClick = onMovieClick)
    }
}

// ── BY PERSON MODE ─────────────────────────────────────────────────────────────

@Composable
private fun ByPersonMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var peopleState by remember { mutableStateOf<UiState<List<com.radarrtv.androidtv.data.api.model.TmdbPerson>>>(UiState.Success(emptyList())) }
    var moviesState by remember { mutableStateOf<UiState<List<TmdbMovie>>?>(null) }
    var selectedPerson by remember { mutableStateOf<com.radarrtv.androidtv.data.api.model.TmdbPerson?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    selectedPerson = null
                    moviesState = null
                    searchJob?.cancel()
                    if (q.length >= 2) {
                        peopleState = UiState.Loading
                        searchJob = scope.launch {
                            delay(400)
                            peopleState = try { UiState.Success(tmdbRepo.searchPeople(q).results) }
                            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                        }
                    } else {
                        peopleState = UiState.Success(emptyList())
                    }
                },
                placeholder = { Text("Search actor, director…", color = RadarrMuted) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = searchFieldColors()
            )
            if (selectedPerson != null) {
                TvFocusButton(onClick = { selectedPerson = null; moviesState = null; query = "" }) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (selectedPerson == null) {
            when (val s = peopleState) {
                is UiState.Loading -> LoadingScreen("Searching people…")
                is UiState.Error -> ErrorScreen(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty() && query.length >= 2) EmptyScreen("No people found")
                    else if (s.data.isEmpty()) EmptyScreen("Type a name to search for an actor or director")
                    else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 130.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.data.take(40), key = { it.id }) { person ->
                                PersonCard(person, tmdbRepo) {
                                    selectedPerson = person
                                    moviesState = UiState.Loading
                                    scope.launch {
                                        moviesState = try {
                                            val credits = tmdbRepo.getPersonMovieCredits(person.id)
                                            UiState.Success(
                                                (credits.cast + credits.crew)
                                                    .distinctBy { it.id }
                                                    .sortedByDescending { it.popularity }
                                            )
                                        } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val ms = moviesState
            val person = selectedPerson!!
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    Icon(Icons.Default.Person, null, tint = RadarrBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Filmography: ${person.name}", style = MaterialTheme.typography.titleMedium, color = RadarrWhite)
                    person.knownForDepartment?.let { dept ->
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(dept, RadarrBlueDark)
                    }
                }
                when (ms) {
                    null, is UiState.Loading -> LoadingScreen("Loading filmography…")
                    is UiState.Error -> ErrorScreen(ms.message)
                    is UiState.Success -> {
                        if (ms.data.isEmpty()) EmptyScreen("No movies found")
                        else LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ms.data, key = { it.id }) { movie ->
                                TmdbMovieCard(movie = movie, inLibrary = libraryMap.containsKey(movie.id), onClick = { onMovieClick(movie.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── BY COLLECTION MODE ────────────────────────────────────────────────────────

@Composable
private fun ByCollectionMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var collectionsState by remember { mutableStateOf<UiState<List<com.radarrtv.androidtv.data.api.model.TmdbCollection>>>(UiState.Success(emptyList())) }
    var moviesState by remember { mutableStateOf<UiState<List<TmdbMovie>>?>(null) }
    var selectedCollection by remember { mutableStateOf<com.radarrtv.androidtv.data.api.model.TmdbCollection?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    selectedCollection = null
                    moviesState = null
                    searchJob?.cancel()
                    if (q.length >= 2) {
                        collectionsState = UiState.Loading
                        searchJob = scope.launch {
                            delay(400)
                            collectionsState = try { UiState.Success(tmdbRepo.searchCollections(q).results) }
                            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                        }
                    } else collectionsState = UiState.Success(emptyList())
                },
                placeholder = { Text("Search movie collections…", color = RadarrMuted) },
                leadingIcon = { Icon(Icons.Default.Collections, null, tint = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = searchFieldColors()
            )
            if (selectedCollection != null) {
                TvFocusButton(onClick = { selectedCollection = null; moviesState = null; query = "" }) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Clear")
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (selectedCollection == null) {
            when (val s = collectionsState) {
                is UiState.Loading -> LoadingScreen("Searching collections…")
                is UiState.Error -> ErrorScreen(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty() && query.length >= 2) EmptyScreen("No collections found")
                    else if (s.data.isEmpty()) EmptyScreen("Type a collection name, e.g. Marvel, Star Wars, Indiana Jones")
                    else LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.data.take(30), key = { it.id }) { col ->
                            CollectionCard(col, tmdbRepo) {
                                selectedCollection = col
                                moviesState = UiState.Loading
                                scope.launch {
                                    moviesState = try {
                                        val detail = tmdbRepo.getCollectionDetail(col.id)
                                        UiState.Success(detail.parts.sortedBy { it.releaseDate })
                                    } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val col = selectedCollection!!
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    Icon(Icons.Default.Collections, null, tint = RadarrBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(col.name, style = MaterialTheme.typography.titleMedium, color = RadarrWhite)
                }
                when (val ms = moviesState) {
                    null, is UiState.Loading -> LoadingScreen()
                    is UiState.Error -> ErrorScreen(ms.message)
                    is UiState.Success -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(ms.data, key = { it.id }) { movie ->
                            TmdbMovieCard(movie = movie, inLibrary = libraryMap.containsKey(movie.id), onClick = { onMovieClick(movie.id) })
                        }
                    }
                }
            }
        }
    }
}

// ── BY COMPANY MODE ───────────────────────────────────────────────────────────

@Composable
private fun ByCompanyMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var companiesState by remember { mutableStateOf<UiState<List<com.radarrtv.androidtv.data.api.model.TmdbCompany>>>(UiState.Success(emptyList())) }
    var page by remember { mutableIntStateOf(1) }
    var moviesState by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>?>(null) }
    var selectedCompany by remember { mutableStateOf<com.radarrtv.androidtv.data.api.model.TmdbCompany?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun loadCompanyMovies(companyId: Int, p: Int) {
        page = p
        scope.launch {
            moviesState = UiState.Loading
            moviesState = try {
                UiState.Success(tmdbRepo.discover(withCompanies = companyId.toString(), page = p))
            } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    selectedCompany = null
                    moviesState = null
                    searchJob?.cancel()
                    if (q.length >= 2) {
                        companiesState = UiState.Loading
                        searchJob = scope.launch {
                            delay(400)
                            companiesState = try { UiState.Success(tmdbRepo.searchCompanies(q).results) }
                            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                        }
                    } else companiesState = UiState.Success(emptyList())
                },
                placeholder = { Text("Search studios, distributors…", color = RadarrMuted) },
                leadingIcon = { Icon(Icons.Default.Business, null, tint = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = searchFieldColors()
            )
            if (selectedCompany != null) {
                TvFocusButton(onClick = { selectedCompany = null; moviesState = null; query = "" }) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Clear")
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (selectedCompany == null) {
            when (val s = companiesState) {
                is UiState.Loading -> LoadingScreen("Searching studios…")
                is UiState.Error -> ErrorScreen(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty() && query.length >= 2) EmptyScreen("No studios found")
                    else if (s.data.isEmpty()) EmptyScreen("Search a studio name, e.g. Warner, A24, Pixar")
                    else LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.data.take(30), key = { it.id }) { company ->
                            CompanyCard(company) { loadCompanyMovies(company.id, 1); selectedCompany = company }
                        }
                    }
                }
            }
        } else {
            val company = selectedCompany!!
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    Icon(Icons.Default.Business, null, tint = RadarrBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(company.name, style = MaterialTheme.typography.titleMedium, color = RadarrWhite)
                    company.originCountry?.let { Spacer(Modifier.width(8.dp)); StatusBadge(it, RadarrMuted) }
                }
                PagedContent(moviesState ?: UiState.Loading, libraryMap, page,
                    onPrev = { loadCompanyMovies(company.id, page - 1) },
                    onNext = { loadCompanyMovies(company.id, page + 1) },
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

// ── BY KEYWORD MODE ───────────────────────────────────────────────────────────

@Composable
private fun ByKeywordMode(tmdbRepo: TmdbRepository, libraryMap: Map<Int, Int>, onMovieClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var keywordsState by remember { mutableStateOf<UiState<List<com.radarrtv.androidtv.data.api.model.TmdbKeyword>>>(UiState.Success(emptyList())) }
    var page by remember { mutableIntStateOf(1) }
    var moviesState by remember { mutableStateOf<UiState<TmdbPagedResult<TmdbMovie>>?>(null) }
    var selectedKeyword by remember { mutableStateOf<com.radarrtv.androidtv.data.api.model.TmdbKeyword?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun loadKeywordMovies(keywordId: Int, p: Int) {
        page = p
        scope.launch {
            moviesState = UiState.Loading
            moviesState = try {
                UiState.Success(tmdbRepo.discover(withKeywords = keywordId.toString(), page = p))
            } catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    selectedKeyword = null
                    moviesState = null
                    searchJob?.cancel()
                    if (q.length >= 2) {
                        keywordsState = UiState.Loading
                        searchJob = scope.launch {
                            delay(400)
                            keywordsState = try { UiState.Success(tmdbRepo.searchKeywords(q).results) }
                            catch (e: Exception) { UiState.Error(e.message ?: "Failed") }
                        }
                    } else keywordsState = UiState.Success(emptyList())
                },
                placeholder = { Text("Search keywords, e.g. heist, time travel, superhero…", color = RadarrMuted) },
                leadingIcon = { Icon(Icons.Default.Tag, null, tint = RadarrMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = searchFieldColors()
            )
            if (selectedKeyword != null) {
                TvFocusButton(onClick = { selectedKeyword = null; moviesState = null; query = "" }) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Clear")
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (selectedKeyword == null) {
            when (val s = keywordsState) {
                is UiState.Loading -> LoadingScreen("Searching keywords…")
                is UiState.Error -> ErrorScreen(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty() && query.length >= 2) EmptyScreen("No keywords found")
                    else if (s.data.isEmpty()) EmptyScreen("Type a keyword to browse themed movies")
                    else Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        s.data.take(50).forEach { keyword ->
                            var kFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (kFocused) RadarrBlueDark else RadarrCard)
                                    .border(2.dp, if (kFocused) RadarrBlue else Color.Transparent, RoundedCornerShape(20.dp))
                                    .clickable { selectedKeyword = keyword; loadKeywordMovies(keyword.id, 1) }
                                    .onFocusChanged { kFocused = it.isFocused }
                                    .focusable()
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(keyword.name, style = MaterialTheme.typography.bodyMedium, color = RadarrWhite)
                            }
                        }
                    }
                }
            }
        } else {
            val keyword = selectedKeyword!!
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    Icon(Icons.Default.Tag, null, tint = RadarrBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Keyword: ${keyword.name}", style = MaterialTheme.typography.titleMedium, color = RadarrWhite)
                }
                PagedContent(moviesState ?: UiState.Loading, libraryMap, page,
                    onPrev = { loadKeywordMovies(keyword.id, page - 1) },
                    onNext = { loadKeywordMovies(keyword.id, page + 1) },
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

// ── Shared Helpers ────────────────────────────────────────────────────────────

@Composable
private fun PagedContent(
    state: UiState<TmdbPagedResult<TmdbMovie>>?,
    libraryMap: Map<Int, Int>,
    page: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    when (state) {
        null, is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.message)
        is UiState.Success -> {
            val result = state.data
            if (result.results.isEmpty()) EmptyScreen("No results found")
            else ResultsGrid(result.results, libraryMap, page, result.totalPages, onPrev, onNext, onMovieClick)
        }
    }
}

@Composable
private fun GenreChip(name: String, selected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) RadarrBlueDark else if (isFocused) RadarrSurface else RadarrCard)
            .border(2.dp, if (isFocused || selected) RadarrBlue else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(name, style = MaterialTheme.typography.labelMedium, color = if (selected || isFocused) RadarrWhite else RadarrMuted)
    }
}

@Composable
private fun PersonCard(person: com.radarrtv.androidtv.data.api.model.TmdbPerson, tmdbRepo: TmdbRepository, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val profileUrl = remember(person.profilePath) {
        if (person.profilePath != null) "https://image.tmdb.org/t/p/w185${person.profilePath}" else ""
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, if (isFocused) RadarrBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .background(if (isFocused) RadarrSurface else RadarrCard)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape).background(RadarrBorder)
        ) {
            if (profileUrl.isNotBlank()) {
                AsyncImage(model = profileUrl, contentDescription = person.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Default.Person, null, tint = RadarrMuted, modifier = Modifier.align(Alignment.Center).size(36.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(person.name, style = MaterialTheme.typography.bodyMedium, color = RadarrWhite, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        person.knownForDepartment?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = RadarrMuted, maxLines = 1)
        }
    }
}

@Composable
private fun CollectionCard(collection: com.radarrtv.androidtv.data.api.model.TmdbCollection, tmdbRepo: TmdbRepository, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val posterUrl = remember(collection.posterPath) {
        if (collection.posterPath != null) "https://image.tmdb.org/t/p/w342${collection.posterPath}" else ""
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(3.dp, if (isFocused) RadarrBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(RadarrCard)) {
            if (posterUrl.isNotBlank()) {
                AsyncImage(model = posterUrl, contentDescription = collection.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Default.Collections, null, tint = RadarrBorder, modifier = Modifier.align(Alignment.Center).size(40.dp))
            }
        }
        Box(modifier = Modifier.fillMaxWidth().background(if (isFocused) RadarrSurface else RadarrCard).padding(8.dp)) {
            Text(collection.name, style = MaterialTheme.typography.bodyMedium, color = RadarrWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CompanyCard(company: com.radarrtv.androidtv.data.api.model.TmdbCompany, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) RadarrSurface else RadarrCard)
            .border(2.dp, if (isFocused) RadarrBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(16.dp)
    ) {
        Column {
            Text(company.name, style = MaterialTheme.typography.bodyLarge, color = RadarrWhite)
            company.originCountry?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = RadarrMuted)
            }
        }
    }
}

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RadarrBlue,
    unfocusedBorderColor = RadarrBorder,
    focusedContainerColor = RadarrCard,
    unfocusedContainerColor = RadarrSurfaceVariant,
    focusedTextColor = RadarrWhite,
    unfocusedTextColor = RadarrWhite,
    cursorColor = RadarrBlue
)
