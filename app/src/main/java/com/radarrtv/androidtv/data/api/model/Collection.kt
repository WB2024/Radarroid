package com.radarrtv.androidtv.data.api.model

data class MovieCollection(
    val id: Int = 0,
    val tmdbId: Int = 0,
    val title: String = "",
    val cleanTitle: String = "",
    val sortTitle: String = "",
    val overview: String? = null,
    val monitored: Boolean = false,
    val minimumAvailability: String = "released",
    val qualityProfileId: Int = 1,
    val searchOnAdd: Boolean = false,
    val images: List<MediaCover> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val added: String = "",
    val rootFolderPath: String? = null,
    val tags: List<Int> = emptyList()
) {
    val posterUrl: String?
        get() = images.firstOrNull { it.coverType == "poster" }?.remoteUrl
            ?: images.firstOrNull { it.coverType == "poster" }?.url

    val movieCount: Int get() = movies.size
    val downloadedCount: Int get() = movies.count { it.hasFile }
}
