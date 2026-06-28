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
    val credits: TmdbCredits? = null
) {
    val year: String get() = releaseDate?.take(4) ?: ""
}

data class TmdbPersonMovieCredits(
    val id: Int = 0,
    val cast: List<TmdbMovie> = emptyList(),
    val crew: List<TmdbMovie> = emptyList()
)
