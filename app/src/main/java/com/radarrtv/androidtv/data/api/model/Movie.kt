package com.radarrtv.androidtv.data.api.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int = 0,
    val title: String = "",
    val originalTitle: String? = null,
    val sortTitle: String = "",
    val status: String = "",
    val overview: String? = null,
    val inCinemas: String? = null,
    val physicalRelease: String? = null,
    val digitalRelease: String? = null,
    val year: Int = 0,
    val hasFile: Boolean = false,
    val monitored: Boolean = false,
    val minimumAvailability: String = "released",
    val isAvailable: Boolean = false,
    val folderName: String? = null,
    val runtime: Int = 0,
    val tmdbId: Int = 0,
    val imdbId: String? = null,
    val genres: List<String> = emptyList(),
    val tags: List<Int> = emptyList(),
    val added: String = "",
    val ratings: Ratings? = null,
    val movieFile: MovieFile? = null,
    val images: List<MediaCover> = emptyList(),
    val qualityProfileId: Int = 1,
    val path: String = "",
    val certification: String? = null,
    val website: String? = null,
    val youTubeTrailerId: String? = null,
    val studio: String? = null,
    val collection: MovieCollectionRef? = null,
    val popularity: Double? = null,
    val sizeOnDisk: Long? = null,
    val movieFileId: Int = 0
) {
    fun posterUrl(baseUrl: String, apiKey: String): String {
        val base = baseUrl.trimEnd('/')
        return "$base/MediaCover/$id/poster.jpg?apikey=$apiKey"
    }

    fun backdropUrl(baseUrl: String, apiKey: String): String {
        val base = baseUrl.trimEnd('/')
        return "$base/MediaCover/$id/fanart.jpg?apikey=$apiKey"
    }
}

data class Ratings(
    val tmdb: Rating? = null,
    val imdb: Rating? = null,
    val rottenTomatoes: Rating? = null,
    val trakt: Rating? = null
)

data class Rating(
    val votes: Int = 0,
    val value: Double = 0.0,
    val type: String = ""
)

data class MediaCover(
    val coverType: String = "",
    val url: String = "",
    val remoteUrl: String? = null
)

data class MovieCollectionRef(
    val tmdbId: Int = 0,
    val title: String = ""
)

data class AddMovieRequest(
    val title: String,
    val tmdbId: Int,
    val year: Int,
    val qualityProfileId: Int,
    val rootFolderPath: String,
    val monitored: Boolean = true,
    val minimumAvailability: String = "released",
    val addOptions: AddOptions = AddOptions(),
    val images: List<MediaCover> = emptyList()
)

data class AddOptions(
    val searchForMovie: Boolean = true,
    val monitor: String = "movieOnly"
)

data class EditMovieRequest(
    val id: Int,
    val title: String,
    val tmdbId: Int,
    val qualityProfileId: Int,
    val monitored: Boolean,
    val minimumAvailability: String,
    val path: String,
    val tags: List<Int> = emptyList()
)
