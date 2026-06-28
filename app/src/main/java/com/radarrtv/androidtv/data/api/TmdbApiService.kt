package com.radarrtv.androidtv.data.api

import com.radarrtv.androidtv.data.api.model.*
import retrofit2.http.*

interface TmdbApiService {

    // ── Search ───────────────────────────────────────────────────────────────
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("year") year: Int? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResult<TmdbMovie>

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbPerson>

    @GET("search/collection")
    suspend fun searchCollections(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbCollection>

    @GET("search/company")
    suspend fun searchCompanies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbCompany>

    @GET("search/keyword")
    suspend fun searchKeywords(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbKeyword>

    // ── Curated Lists ────────────────────────────────────────────────────────
    @GET("movie/popular")
    suspend fun getPopular(@Query("page") page: Int = 1): TmdbPagedResult<TmdbMovie>

    @GET("movie/top_rated")
    suspend fun getTopRated(@Query("page") page: Int = 1): TmdbPagedResult<TmdbMovie>

    @GET("movie/now_playing")
    suspend fun getNowPlaying(@Query("page") page: Int = 1): TmdbPagedResult<TmdbMovie>

    @GET("movie/upcoming")
    suspend fun getUpcoming(@Query("page") page: Int = 1): TmdbPagedResult<TmdbMovie>

    @GET("trending/movie/{time_window}")
    suspend fun getTrending(
        @Path("time_window") timeWindow: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbMovie>

    // ── Genres ───────────────────────────────────────────────────────────────
    @GET("genre/movie/list")
    suspend fun getGenres(): TmdbGenreListResponse

    // ── Discover ─────────────────────────────────────────────────────────────
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("without_genres") withoutGenres: String? = null,
        @Query("primary_release_year") year: Int? = null,
        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null,
        @Query("vote_average.gte") minRating: Float? = null,
        @Query("vote_count.gte") minVotes: Int? = null,
        @Query("with_runtime.gte") minRuntime: Int? = null,
        @Query("with_runtime.lte") maxRuntime: Int? = null,
        @Query("with_original_language") language: String? = null,
        @Query("with_cast") withCast: String? = null,
        @Query("with_crew") withCrew: String? = null,
        @Query("with_companies") withCompanies: String? = null,
        @Query("with_keywords") withKeywords: String? = null,
        @Query("without_keywords") withoutKeywords: String? = null,
        @Query("with_origin_country") originCountry: String? = null,
        @Query("with_watch_providers") withWatchProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("with_watch_monetization_types") monetizationTypes: String? = null,
        @Query("certification") certification: String? = null,
        @Query("certification_country") certificationCountry: String? = null,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResult<TmdbMovie>

    // ── Movie Detail ─────────────────────────────────────────────────────────
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,keywords,release_dates"
    ): TmdbMovieDetail

    // ── Similar & Recommendations ─────────────────────────────────────────────
    @GET("movie/{movie_id}/similar")
    suspend fun getSimilar(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbMovie>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): TmdbPagedResult<TmdbMovie>

    // ── People ────────────────────────────────────────────────────────────────
    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(@Path("person_id") personId: Int): TmdbPersonMovieCredits

    // ── Collections ───────────────────────────────────────────────────────────
    @GET("collection/{collection_id}")
    suspend fun getCollectionDetail(@Path("collection_id") collectionId: Int): TmdbCollectionDetail

    // ── Lists ──────────────────────────────────────────────────────────────────
    @GET("list/{list_id}")
    suspend fun getList(@Path("list_id") listId: Int): TmdbListDetail

    // ── Countries ─────────────────────────────────────────────────────────────
    @GET("configuration/countries")
    suspend fun getCountries(): List<TmdbCountry>

    // ── Videos (trailers, teasers, featurettes) ───────────────────────────────
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Int): TmdbVideoResult

    // ── Watch providers ───────────────────────────────────────────────────────
    @GET("watch/providers/movie")
    suspend fun getWatchProviders(
        @Query("watch_region") region: String = "US"
    ): TmdbWatchProvidersResponse

    // ── Movie watch providers (where to stream/rent/buy) ─────────────────────
    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(@Path("movie_id") movieId: Int): TmdbMovieWatchProvidersResponse

    // ── Movie reviews ─────────────────────────────────────────────────────────
    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): TmdbReviewResult

    // ── Person detail (biography, birthday, etc.) ─────────────────────────────
    @GET("person/{person_id}")
    suspend fun getPersonDetail(@Path("person_id") personId: Int): TmdbPersonDetail
}
