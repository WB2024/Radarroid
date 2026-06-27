package com.radarrtv.androidtv.data.api

import com.radarrtv.androidtv.data.api.model.*
import com.radarrtv.androidtv.data.api.model.Tag
import retrofit2.http.*

interface RadarrApiService {

    // ── Movies ──────────────────────────────────────────────────────────────
    @GET("movie")
    suspend fun getMovies(): List<Movie>

    @GET("movie/{id}")
    suspend fun getMovie(@Path("id") id: Int): Movie

    @POST("movie")
    suspend fun addMovie(@Body request: AddMovieRequest): Movie

    @PUT("movie/{id}")
    suspend fun editMovie(@Path("id") id: Int, @Body movie: Movie): Movie

    @DELETE("movie/{id}")
    suspend fun deleteMovie(
        @Path("id") id: Int,
        @Query("deleteFiles") deleteFiles: Boolean = false,
        @Query("addImportExclusion") addImportExclusion: Boolean = false
    )

    // ── Movie Lookup ─────────────────────────────────────────────────────────
    @GET("movie/lookup")
    suspend fun lookupMovie(@Query("term") term: String): List<Movie>

    @GET("movie/lookup/tmdb")
    suspend fun lookupMovieByTmdbId(@Query("tmdbId") tmdbId: Int): Movie

    // ── Movie Files ──────────────────────────────────────────────────────────
    @GET("moviefile")
    suspend fun getMovieFiles(@Query("movieId") movieId: Int): List<MovieFile>

    @DELETE("moviefile/{id}")
    suspend fun deleteMovieFile(@Path("id") id: Int)

    // ── Queue ────────────────────────────────────────────────────────────────
    @GET("queue")
    suspend fun getQueue(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100,
        @Query("includeMovie") includeMovie: Boolean = true
    ): QueueResponse

    @DELETE("queue/{id}")
    suspend fun removeFromQueue(
        @Path("id") id: Int,
        @Query("removeFromClient") removeFromClient: Boolean = false,
        @Query("blocklist") blocklist: Boolean = false
    )

    @POST("queue/grab/{id}")
    suspend fun grabFromQueue(@Path("id") id: Int)

    // ── History ──────────────────────────────────────────────────────────────
    @GET("history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("sortKey") sortKey: String = "date",
        @Query("sortDirection") sortDirection: String = "descending",
        @Query("includeMovie") includeMovie: Boolean = true
    ): HistoryResponse

    @GET("history/movie")
    suspend fun getMovieHistory(
        @Query("movieId") movieId: Int,
        @Query("includeMovie") includeMovie: Boolean = true
    ): List<HistoryItem>

    // ── Releases ─────────────────────────────────────────────────────────────
    @GET("release")
    suspend fun searchReleases(@Query("movieId") movieId: Int): List<Release>

    @POST("release")
    suspend fun grabRelease(@Body request: GrabReleaseRequest): Release

    // ── Calendar ─────────────────────────────────────────────────────────────
    @GET("calendar")
    suspend fun getCalendar(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("unmonitored") unmonitored: Boolean = false
    ): List<Movie>

    // ── Wanted ───────────────────────────────────────────────────────────────
    @GET("wanted/missing")
    suspend fun getWantedMissing(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("sortKey") sortKey: String = "releaseDate",
        @Query("sortDirection") sortDirection: String = "descending",
        @Query("monitored") monitored: Boolean = true,
        @Query("includeImages") includeImages: Boolean = true
    ): WantedResponse

    @GET("wanted/cutoff")
    suspend fun getWantedCutoff(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("monitored") monitored: Boolean = true,
        @Query("includeImages") includeImages: Boolean = true
    ): WantedResponse

    // ── Collections ──────────────────────────────────────────────────────────
    @GET("collection")
    suspend fun getCollections(@Query("tmdbId") tmdbId: Int? = null): List<MovieCollection>

    @GET("collection/{id}")
    suspend fun getCollection(@Path("id") id: Int): MovieCollection

    @PUT("collection/{id}")
    suspend fun updateCollection(@Path("id") id: Int, @Body collection: MovieCollection): MovieCollection

    // ── Blocklist ────────────────────────────────────────────────────────────
    @GET("blocklist")
    suspend fun getBlocklist(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): BlocklistResponse

    @DELETE("blocklist/{id}")
    suspend fun deleteBlocklistItem(@Path("id") id: Int)

    // ── Quality Profiles ─────────────────────────────────────────────────────
    @GET("qualityprofile")
    suspend fun getQualityProfiles(): List<QualityProfile>

    @GET("qualityprofile/{id}")
    suspend fun getQualityProfile(@Path("id") id: Int): QualityProfile

    @POST("qualityprofile")
    suspend fun createQualityProfile(@Body profile: QualityProfile): QualityProfile

    @PUT("qualityprofile/{id}")
    suspend fun updateQualityProfile(@Path("id") id: Int, @Body profile: QualityProfile): QualityProfile

    @DELETE("qualityprofile/{id}")
    suspend fun deleteQualityProfile(@Path("id") id: Int)

    // ── Indexers ─────────────────────────────────────────────────────────────
    @GET("indexer")
    suspend fun getIndexers(): List<Indexer>

    @GET("indexer/schema")
    suspend fun getIndexerSchema(): List<Indexer>

    @GET("indexer/{id}")
    suspend fun getIndexer(@Path("id") id: Int): Indexer

    @POST("indexer")
    suspend fun createIndexer(@Body indexer: Indexer): Indexer

    @PUT("indexer/{id}")
    suspend fun updateIndexer(@Path("id") id: Int, @Body indexer: Indexer): Indexer

    @DELETE("indexer/{id}")
    suspend fun deleteIndexer(@Path("id") id: Int)

    @POST("indexer/testall")
    suspend fun testAllIndexers()

    // ── Download Clients ─────────────────────────────────────────────────────
    @GET("downloadclient")
    suspend fun getDownloadClients(): List<DownloadClient>

    @GET("downloadclient/schema")
    suspend fun getDownloadClientSchema(): List<DownloadClient>

    @GET("downloadclient/{id}")
    suspend fun getDownloadClient(@Path("id") id: Int): DownloadClient

    @POST("downloadclient")
    suspend fun createDownloadClient(@Body client: DownloadClient): DownloadClient

    @PUT("downloadclient/{id}")
    suspend fun updateDownloadClient(@Path("id") id: Int, @Body client: DownloadClient): DownloadClient

    @DELETE("downloadclient/{id}")
    suspend fun deleteDownloadClient(@Path("id") id: Int)

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notification")
    suspend fun getNotifications(): List<Notification>

    @GET("notification/schema")
    suspend fun getNotificationSchema(): List<Notification>

    @POST("notification")
    suspend fun createNotification(@Body notification: Notification): Notification

    @PUT("notification/{id}")
    suspend fun updateNotification(@Path("id") id: Int, @Body notification: Notification): Notification

    @DELETE("notification/{id}")
    suspend fun deleteNotification(@Path("id") id: Int)

    // ── Import Lists ──────────────────────────────────────────────────────────
    @GET("importlist")
    suspend fun getImportLists(): List<ImportList>

    @GET("importlist/schema")
    suspend fun getImportListSchema(): List<ImportList>

    @POST("importlist")
    suspend fun createImportList(@Body importList: ImportList): ImportList

    @PUT("importlist/{id}")
    suspend fun updateImportList(@Path("id") id: Int, @Body importList: ImportList): ImportList

    @DELETE("importlist/{id}")
    suspend fun deleteImportList(@Path("id") id: Int)

    // ── Custom Formats ────────────────────────────────────────────────────────
    @GET("customformat")
    suspend fun getCustomFormats(): List<CustomFormat>

    @POST("customformat")
    suspend fun createCustomFormat(@Body format: CustomFormat): CustomFormat

    @PUT("customformat/{id}")
    suspend fun updateCustomFormat(@Path("id") id: Int, @Body format: CustomFormat): CustomFormat

    @DELETE("customformat/{id}")
    suspend fun deleteCustomFormat(@Path("id") id: Int)

    // ── Metadata ──────────────────────────────────────────────────────────────
    @GET("metadata")
    suspend fun getMetadataProviders(): List<MetadataProvider>

    @PUT("metadata/{id}")
    suspend fun updateMetadataProvider(@Path("id") id: Int, @Body provider: MetadataProvider): MetadataProvider

    // ── Tags ──────────────────────────────────────────────────────────────────
    @GET("tag")
    suspend fun getTags(): List<Tag>

    @POST("tag")
    suspend fun createTag(@Body tag: Tag): Tag

    @PUT("tag/{id}")
    suspend fun updateTag(@Path("id") id: Int, @Body tag: Tag): Tag

    @DELETE("tag/{id}")
    suspend fun deleteTag(@Path("id") id: Int)

    // ── Root Folders ─────────────────────────────────────────────────────────
    @GET("rootfolder")
    suspend fun getRootFolders(): List<RootFolder>

    @POST("rootfolder")
    suspend fun addRootFolder(@Body body: Map<String, String>): RootFolder

    @DELETE("rootfolder/{id}")
    suspend fun deleteRootFolder(@Path("id") id: Int)

    // ── Config ────────────────────────────────────────────────────────────────
    @GET("config/host")
    suspend fun getHostConfig(): HostConfig

    @PUT("config/host/{id}")
    suspend fun updateHostConfig(@Path("id") id: Int, @Body config: HostConfig): HostConfig

    @GET("config/ui")
    suspend fun getUiConfig(): UiConfig

    @PUT("config/ui/{id}")
    suspend fun updateUiConfig(@Path("id") id: Int, @Body config: UiConfig): UiConfig

    @GET("config/naming")
    suspend fun getNamingConfig(): NamingConfig

    @PUT("config/naming/{id}")
    suspend fun updateNamingConfig(@Path("id") id: Int, @Body config: NamingConfig): NamingConfig

    @GET("config/naming/examples")
    suspend fun getNamingExamples(
        @Query("renameMovies") renameMovies: Boolean,
        @Query("standardMovieFormat") format: String
    ): NamingExample

    @GET("config/mediamanagement")
    suspend fun getMediaManagement(): MediaManagementConfig

    @PUT("config/mediamanagement/{id}")
    suspend fun updateMediaManagement(
        @Path("id") id: Int,
        @Body config: MediaManagementConfig
    ): MediaManagementConfig

    // ── System ────────────────────────────────────────────────────────────────
    @GET("system/status")
    suspend fun getSystemStatus(): SystemStatus

    @POST("system/restart")
    suspend fun restartSystem()

    @GET("health")
    suspend fun getHealthChecks(): List<HealthCheck>

    @GET("diskspace")
    suspend fun getDiskSpace(): List<DiskSpace>

    @GET("system/task")
    suspend fun getScheduledTasks(): List<ScheduledTask>

    @GET("log")
    suspend fun getLogs(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("level") level: String? = null
    ): LogResponse

    // ── Commands ──────────────────────────────────────────────────────────────
    @GET("command")
    suspend fun getCommands(): List<Command>

    @POST("command")
    suspend fun postCommand(@Body request: CommandRequest): Command
}
