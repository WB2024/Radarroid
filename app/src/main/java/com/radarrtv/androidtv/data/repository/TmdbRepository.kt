package com.radarrtv.androidtv.data.repository

import com.radarrtv.androidtv.data.api.TmdbApiClient
import com.radarrtv.androidtv.data.api.TmdbApiService
import com.radarrtv.androidtv.data.api.model.TmdbGenre
import com.radarrtv.androidtv.data.preferences.UserPreferences

class TmdbRepository(prefs: UserPreferences) {
    private val api: TmdbApiService = TmdbApiClient.create(prefs.tmdbApiKey)

    fun posterUrl(path: String?) = TmdbApiClient.posterUrl(path)
    fun backdropUrl(path: String?) = TmdbApiClient.backdropUrl(path)
    fun profileUrl(path: String?) = TmdbApiClient.profileUrl(path)

    // Search
    suspend fun searchMovies(query: String, page: Int = 1, year: Int? = null) = api.searchMovies(query, page, year)
    suspend fun searchPeople(query: String, page: Int = 1) = api.searchPeople(query, page)
    suspend fun searchCollections(query: String, page: Int = 1) = api.searchCollections(query, page)
    suspend fun searchCompanies(query: String, page: Int = 1) = api.searchCompanies(query, page)
    suspend fun searchKeywords(query: String, page: Int = 1) = api.searchKeywords(query, page)

    // Curated lists
    suspend fun getPopular(page: Int = 1) = api.getPopular(page)
    suspend fun getTopRated(page: Int = 1) = api.getTopRated(page)
    suspend fun getNowPlaying(page: Int = 1) = api.getNowPlaying(page)
    suspend fun getUpcoming(page: Int = 1) = api.getUpcoming(page)
    suspend fun getTrending(timeWindow: String = "week", page: Int = 1) = api.getTrending(timeWindow, page)

    // Genres
    suspend fun getGenres(): List<TmdbGenre> = api.getGenres().genres

    // Discover
    suspend fun discover(
        sortBy: String = "popularity.desc",
        genreIds: List<Int> = emptyList(),
        year: Int? = null,
        minRating: Float? = null,
        minVotes: Int? = null,
        language: String? = null,
        withCast: String? = null,
        withCrew: String? = null,
        withCompanies: String? = null,
        withKeywords: String? = null,
        originCountry: String? = null,
        page: Int = 1
    ) = api.discoverMovies(
        sortBy = sortBy,
        withGenres = genreIds.joinToString(",").ifBlank { null },
        year = year,
        minRating = minRating,
        minVotes = minVotes,
        language = language,
        withCast = withCast,
        withCrew = withCrew,
        withCompanies = withCompanies,
        withKeywords = withKeywords,
        originCountry = originCountry,
        page = page
    )

    // Movie detail
    suspend fun getMovieDetail(movieId: Int) = api.getMovieDetail(movieId)

    // Related
    suspend fun getSimilar(movieId: Int, page: Int = 1) = api.getSimilar(movieId, page)
    suspend fun getRecommendations(movieId: Int, page: Int = 1) = api.getRecommendations(movieId, page)

    // People
    suspend fun getPersonMovieCredits(personId: Int) = api.getPersonMovieCredits(personId)

    // Collections
    suspend fun getCollectionDetail(collectionId: Int) = api.getCollectionDetail(collectionId)
}
