package com.radionula.radionula.data

import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface PlaylistApiService {

    /**
     * Fetches a "recently played" RSS feed, e.g. "recently_played_ch4.xml".
     * [cacheBuster] mirrors what radionula.com itself sends - the feeds are
     * static files and get cached aggressively otherwise.
     */
    @Headers("Cache-Control: no-store")
    @GET
    suspend fun getPlaylist(@Url xmlPath: String, @Query("t") cacheBuster: Long): ResponseBody

    companion object {
        operator fun invoke(
                connectivityInterceptor: com.radionula.radionula.data.network.ConnectivityInterceptorImpl
        ): PlaylistApiService {
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(connectivityInterceptor)
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("https://radionula.com/")
                    .build()
                    .create(PlaylistApiService::class.java)
        }
    }
}
