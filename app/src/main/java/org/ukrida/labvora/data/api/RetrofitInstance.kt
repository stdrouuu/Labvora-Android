// Singleton untuk konfigurasi Retrofit, menentukan Base URL dan Converter
package org.ukrida.labvora.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    const val BASE_URL = "https://labvora.ifukrida.net/Labvora-API/routes/"

    // Base foto profil server — diturunkan dari BASE_URL agar ikut
    // pindah saat ganti ke lokal (Laragon) atau produksi.
    val uploadsBaseUrl: String
        get() = BASE_URL.substringBefore("/routes/") + "/uploads/"

    // Timeout 30s agar tahan hosting shared (cPanel) yang kadang slow,
    // tanpa ini default 10s -> ANR/crash saat closed testing.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
