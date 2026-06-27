package com.radarrtv.androidtv.data.repository

import com.radarrtv.androidtv.data.api.ApiClient
import com.radarrtv.androidtv.data.api.RadarrApiService
import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.preferences.UserPreferences

class RadarrRepository(prefs: UserPreferences) {
    private val api: RadarrApiService = ApiClient.create(prefs.serverUrl, prefs.apiKey)
    val serverUrl: String = prefs.serverUrl
    val apiKey: String = prefs.apiKey

    fun posterUrl(movieId: Int) =
        "${serverUrl.trimEnd('/')}/api/v3/mediacover/$movieId/poster.jpg?apikey=$apiKey"

    fun backdropUrl(movieId: Int) =
        "${serverUrl.trimEnd('/')}/api/v3/mediacover/$movieId/fanart.jpg?apikey=$apiKey"

    fun mediaUrl(path: String): String {
        val base = serverUrl.trimEnd('/')
        val p = if (path.startsWith('/')) path else "/$path"
        return "$base$p?apikey=$apiKey"
    }

    // Movies
    suspend fun getMovies() = api.getMovies()
    suspend fun getMovie(id: Int) = api.getMovie(id)
    suspend fun addMovie(req: AddMovieRequest) = api.addMovie(req)
    suspend fun editMovie(movie: Movie) = api.editMovie(movie.id, movie)
    suspend fun deleteMovie(id: Int, deleteFiles: Boolean = false) =
        api.deleteMovie(id, deleteFiles)

    // Lookup
    suspend fun lookupMovie(term: String) = api.lookupMovie(term)
    suspend fun lookupMovieByTmdbId(tmdbId: Int) = api.lookupMovieByTmdbId(tmdbId)

    // Files
    suspend fun getMovieFiles(movieId: Int) = api.getMovieFiles(movieId)
    suspend fun deleteMovieFile(fileId: Int) = api.deleteMovieFile(fileId)

    // Queue
    suspend fun getQueue() = api.getQueue()
    suspend fun removeFromQueue(id: Int, removeFromClient: Boolean = false, blocklist: Boolean = false) =
        api.removeFromQueue(id, removeFromClient, blocklist)

    // History
    suspend fun getHistory(page: Int = 1, pageSize: Int = 50) = api.getHistory(page, pageSize)
    suspend fun getMovieHistory(movieId: Int) = api.getMovieHistory(movieId)

    // Releases
    suspend fun searchReleases(movieId: Int) = api.searchReleases(movieId)
    suspend fun grabRelease(req: GrabReleaseRequest) = api.grabRelease(req)

    // Calendar
    suspend fun getCalendar(start: String, end: String) = api.getCalendar(start, end)

    // Wanted
    suspend fun getWantedMissing(page: Int = 1, pageSize: Int = 50) =
        api.getWantedMissing(page, pageSize)
    suspend fun getWantedCutoff(page: Int = 1, pageSize: Int = 50) =
        api.getWantedCutoff(page, pageSize)

    // Collections
    suspend fun getCollections() = api.getCollections()
    suspend fun getCollection(id: Int) = api.getCollection(id)
    suspend fun updateCollection(collection: MovieCollection) =
        api.updateCollection(collection.id, collection)

    // Blocklist
    suspend fun getBlocklist(page: Int = 1) = api.getBlocklist(page)
    suspend fun deleteBlocklistItem(id: Int) = api.deleteBlocklistItem(id)

    // Quality Profiles
    suspend fun getQualityProfiles() = api.getQualityProfiles()
    suspend fun getQualityProfile(id: Int) = api.getQualityProfile(id)
    suspend fun createQualityProfile(profile: QualityProfile) = api.createQualityProfile(profile)
    suspend fun updateQualityProfile(profile: QualityProfile) =
        api.updateQualityProfile(profile.id, profile)
    suspend fun deleteQualityProfile(id: Int) = api.deleteQualityProfile(id)

    // Indexers
    suspend fun getIndexers() = api.getIndexers()
    suspend fun getIndexerSchema() = api.getIndexerSchema()
    suspend fun getIndexer(id: Int) = api.getIndexer(id)
    suspend fun createIndexer(indexer: Indexer) = api.createIndexer(indexer)
    suspend fun updateIndexer(indexer: Indexer) = api.updateIndexer(indexer.id, indexer)
    suspend fun deleteIndexer(id: Int) = api.deleteIndexer(id)
    suspend fun testAllIndexers() = api.testAllIndexers()

    // Download Clients
    suspend fun getDownloadClients() = api.getDownloadClients()
    suspend fun getDownloadClientSchema() = api.getDownloadClientSchema()
    suspend fun createDownloadClient(client: DownloadClient) = api.createDownloadClient(client)
    suspend fun updateDownloadClient(client: DownloadClient) =
        api.updateDownloadClient(client.id, client)
    suspend fun deleteDownloadClient(id: Int) = api.deleteDownloadClient(id)

    // Notifications
    suspend fun getNotifications() = api.getNotifications()
    suspend fun getNotificationSchema() = api.getNotificationSchema()
    suspend fun createNotification(n: Notification) = api.createNotification(n)
    suspend fun updateNotification(n: Notification) = api.updateNotification(n.id, n)
    suspend fun deleteNotification(id: Int) = api.deleteNotification(id)

    // Import Lists
    suspend fun getImportLists() = api.getImportLists()
    suspend fun getImportListSchema() = api.getImportListSchema()
    suspend fun createImportList(list: ImportList) = api.createImportList(list)
    suspend fun updateImportList(list: ImportList) = api.updateImportList(list.id, list)
    suspend fun deleteImportList(id: Int) = api.deleteImportList(id)

    // Custom Formats
    suspend fun getCustomFormats() = api.getCustomFormats()
    suspend fun createCustomFormat(fmt: CustomFormat) = api.createCustomFormat(fmt)
    suspend fun updateCustomFormat(fmt: CustomFormat) = api.updateCustomFormat(fmt.id, fmt)
    suspend fun deleteCustomFormat(id: Int) = api.deleteCustomFormat(id)

    // Metadata
    suspend fun getMetadataProviders() = api.getMetadataProviders()
    suspend fun updateMetadataProvider(provider: MetadataProvider) =
        api.updateMetadataProvider(provider.id, provider)

    // Tags
    suspend fun getTags() = api.getTags()
    suspend fun createTag(label: String) = api.createTag(Tag(label = label))
    suspend fun deleteTag(id: Int) = api.deleteTag(id)

    // Root Folders
    suspend fun getRootFolders() = api.getRootFolders()
    suspend fun addRootFolder(path: String) = api.addRootFolder(mapOf("path" to path))
    suspend fun deleteRootFolder(id: Int) = api.deleteRootFolder(id)

    // Config
    suspend fun getHostConfig() = api.getHostConfig()
    suspend fun updateHostConfig(config: HostConfig) = api.updateHostConfig(config.id, config)
    suspend fun getUiConfig() = api.getUiConfig()
    suspend fun updateUiConfig(config: UiConfig) = api.updateUiConfig(config.id, config)
    suspend fun getNamingConfig() = api.getNamingConfig()
    suspend fun updateNamingConfig(config: NamingConfig) = api.updateNamingConfig(config.id, config)
    suspend fun getMediaManagement() = api.getMediaManagement()
    suspend fun updateMediaManagement(config: MediaManagementConfig) =
        api.updateMediaManagement(config.id, config)

    // System
    suspend fun getSystemStatus() = api.getSystemStatus()
    suspend fun restartSystem() = api.restartSystem()
    suspend fun getHealthChecks() = api.getHealthChecks()
    suspend fun getHealth() = api.getHealthChecks()
    suspend fun getDiskSpace() = api.getDiskSpace()
    suspend fun getScheduledTasks() = api.getScheduledTasks()
    suspend fun getTasks() = api.getScheduledTasks()
    suspend fun getLogs(page: Int = 1, pageSize: Int = 50, level: String? = null) =
        api.getLogs(page, pageSize, level)
    suspend fun getCommands() = api.getCommands()
    suspend fun postCommand(req: CommandRequest) = api.postCommand(req)
    suspend fun sendCommand(name: String, movieIds: List<Int>? = null) =
        api.postCommand(CommandRequest(name = name, movieIds = movieIds))
}
