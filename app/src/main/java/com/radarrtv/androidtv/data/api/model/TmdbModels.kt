package com.radarrtv.androidtv.data.api.model

import com.google.gson.annotations.SerializedName

data class TmdbMovie(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("original_title") val originalTitle: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    val video: Boolean = false,
    @SerializedName("original_language") val originalLanguage: String? = null
) {
    val year: String get() = releaseDate?.take(4) ?: ""
}

data class TmdbPagedResult<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0
)

data class TmdbGenre(val id: Int = 0, val name: String = "")
data class TmdbGenreListResponse(val genres: List<TmdbGenre> = emptyList())

data class TmdbPerson(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("profile_path") val profilePath: String? = null,
    @SerializedName("known_for_department") val knownForDepartment: String? = null,
    @SerializedName("known_for") val knownFor: List<TmdbMovie> = emptyList()
)

data class TmdbCollection(
    val id: Int = 0,
    val name: String = "",
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null
)

data class TmdbCollectionDetail(
    val id: Int = 0,
    val name: String = "",
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    val parts: List<TmdbMovie> = emptyList()
)

data class TmdbCompany(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("logo_path") val logoPath: String? = null,
    @SerializedName("origin_country") val originCountry: String? = null
)

data class TmdbKeyword(val id: Int = 0, val name: String = "")

data class TmdbCastMember(
    val id: Int = 0,
    val name: String = "",
    val character: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null,
    val order: Int = 0
)

data class TmdbCrewMember(
    val id: Int = 0,
    val name: String = "",
    val job: String? = null,
    val department: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbCredits(
    val id: Int = 0,
    val cast: List<TmdbCastMember> = emptyList(),
    val crew: List<TmdbCrewMember> = emptyList()
)

data class TmdbBelongsToCollection(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null
)

data class TmdbMovieDetail(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("original_title") val originalTitle: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val runtime: Int? = null,
    val genres: List<TmdbGenre> = emptyList(),
    val adult: Boolean = false,
    val status: String? = null,
    val tagline: String? = null,
    val budget: Long = 0,
    val revenue: Long = 0,
    @SerializedName("imdb_id") val imdbId: String? = null,
    val homepage: String? = null,
    @SerializedName("original_language") val originalLanguage: String? = null,
    @SerializedName("production_companies") val productionCompanies: List<TmdbCompany> = emptyList(),
    val credits: TmdbCredits? = null,
    @SerializedName("belongs_to_collection") val belongsToCollection: TmdbBelongsToCollection? = null,
    val keywords: TmdbKeywordsWrapper? = null,
    @SerializedName("release_dates") val releaseDates: TmdbReleaseDatesWrapper? = null
) {
    val year: String get() = releaseDate?.take(4) ?: ""
    val certification: String get() {
        val us = releaseDates?.results?.find { it.country == "US" }
        return us?.releaseDates
            ?.filter { it.certification.isNotBlank() }
            ?.minByOrNull { it.type }
            ?.certification ?: ""
    }
}

data class TmdbKeywordsWrapper(val keywords: List<TmdbKeyword> = emptyList())

data class TmdbReleaseDateEntry(val certification: String = "", val type: Int = 0)
data class TmdbCountryReleaseDates(
    @SerializedName("iso_3166_1") val country: String = "",
    @SerializedName("release_dates") val releaseDates: List<TmdbReleaseDateEntry> = emptyList()
)
data class TmdbReleaseDatesWrapper(val results: List<TmdbCountryReleaseDates> = emptyList())

data class TmdbPersonMovieCredits(
    val id: Int = 0,
    val cast: List<TmdbMovie> = emptyList(),
    val crew: List<TmdbMovie> = emptyList()
)

data class TmdbListDetail(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    @SerializedName("item_count") val itemCount: Int = 0,
    @SerializedName("poster_path") val posterPath: String? = null,
    val items: List<TmdbMovie> = emptyList()
)

data class TmdbCountry(
    @SerializedName("iso_3166_1") val code: String = "",
    @SerializedName("english_name") val name: String = ""
)

data class TmdbVideo(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val type: String = "",
    val site: String = "",
    val official: Boolean = false
)
data class TmdbVideoResult(val results: List<TmdbVideo> = emptyList())

data class TmdbWatchProvider(
    @SerializedName("provider_id") val providerId: Int = 0,
    @SerializedName("provider_name") val providerName: String = "",
    @SerializedName("logo_path") val logoPath: String? = null,
    @SerializedName("display_priority") val displayPriority: Int = 0
)
data class TmdbWatchProvidersResponse(val results: List<TmdbWatchProvider> = emptyList())

data class TmdbMovieWatchProviderEntry(
    val flatrate: List<TmdbWatchProvider> = emptyList(),
    val free: List<TmdbWatchProvider> = emptyList(),
    val ads: List<TmdbWatchProvider> = emptyList(),
    val rent: List<TmdbWatchProvider> = emptyList(),
    val buy: List<TmdbWatchProvider> = emptyList()
)
data class TmdbMovieWatchProvidersResponse(val results: Map<String, TmdbMovieWatchProviderEntry> = emptyMap())

data class TmdbReview(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    @SerializedName("author_details") val authorDetails: TmdbReviewAuthor = TmdbReviewAuthor(),
    @SerializedName("created_at") val createdAt: String = ""
)
data class TmdbReviewAuthor(val name: String = "", val rating: Double? = null)
data class TmdbReviewResult(@SerializedName("total_results") val totalResults: Int = 0, val results: List<TmdbReview> = emptyList())

data class TmdbPersonDetail(
    val id: Int = 0,
    val name: String = "",
    val biography: String = "",
    val birthday: String? = null,
    val deathday: String? = null,
    @SerializedName("place_of_birth") val placeOfBirth: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null,
    @SerializedName("known_for_department") val knownForDepartment: String? = null,
    val popularity: Double = 0.0
)
