package com.radarrtv.androidtv.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TmdbApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    const val IMAGE_BASE = "https://image.tmdb.org/t/p/"

    fun create(accessToken: String): TmdbApiService {
        // v4 Read Access Tokens are long JWTs starting with "eyJ"; v3 API keys are ~32 hex chars
        val isBearer = accessToken.startsWith("eyJ") || accessToken.length > 100

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val reqBuilder = original.newBuilder().addHeader("accept", "application/json")
                if (isBearer) {
                    reqBuilder.addHeader("Authorization", "Bearer $accessToken")
                    chain.proceed(reqBuilder.build())
                } else {
                    val url = original.url.newBuilder().addQueryParameter("api_key", accessToken).build()
                    chain.proceed(reqBuilder.url(url).build())
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }

    fun posterUrl(path: String?, size: String = "w342") =
        if (path.isNullOrBlank()) "" else "$IMAGE_BASE$size$path"

    fun backdropUrl(path: String?, size: String = "w780") =
        if (path.isNullOrBlank()) "" else "$IMAGE_BASE$size$path"

    fun profileUrl(path: String?, size: String = "w185") =
        if (path.isNullOrBlank()) "" else "$IMAGE_BASE$size$path"
}
