package com.radarrtv.androidtv.data.api.model

data class Release(
    val guid: String = "",
    val quality: QualityModel? = null,
    val qualityWeight: Int = 0,
    val age: Int = 0,
    val ageHours: Double = 0.0,
    val ageMinutes: Double = 0.0,
    val size: Long = 0,
    val indexerId: Int = 0,
    val indexer: String = "",
    val releaseGroup: String? = null,
    val subGroup: String? = null,
    val releaseHash: String? = null,
    val title: String = "",
    val sceneSource: Boolean = false,
    val mappedMovieId: Int? = null,
    val tvdbId: Int = 0,
    val imdbId: String? = null,
    val tmdbId: Int = 0,
    val approved: Boolean = false,
    val temporarilyRejected: Boolean = false,
    val rejected: Boolean = false,
    val rejections: List<String> = emptyList(),
    val publishDate: String = "",
    val commentUrl: String? = null,
    val downloadUrl: String? = null,
    val infoUrl: String? = null,
    val downloadAllowed: Boolean = true,
    val releaseWeight: Int = 0,
    val preferredWordScore: Int = 0,
    val customFormats: List<CustomFormatRef> = emptyList(),
    val customFormatScore: Int = 0,
    val seeders: Int? = null,
    val leechers: Int? = null,
    val protocol: String = "",
    val languages: List<Language> = emptyList(),
    val movieId: Int? = null
) {
    val qualityName: String
        get() = quality?.quality?.name ?: "Unknown"

    val formattedAge: String
        get() {
            if (ageHours < 24) return "${ageHours.toInt()}h"
            val days = (ageHours / 24).toInt()
            if (days < 7) return "${days}d"
            return "${(days / 7)}w"
        }
}


data class GrabReleaseRequest(
    val guid: String,
    val indexerId: Int,
    val movieId: Int? = null
)
