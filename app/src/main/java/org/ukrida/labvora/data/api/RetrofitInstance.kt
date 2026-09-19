// Singleton untuk konfigurasi Retrofit, menentukan Base URL dan Converter
package org.ukrida.labvora.data.api

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    const val BASE_URL = "https://labvora.ifukrida.net/Labvora-API/routes/"

    // Token JWT untuk autentikasi API
    @Volatile
    var authToken: String? = null

    // Base foto profil server — diturunkan dari BASE_URL agar ikut
    // pindah saat ganti ke lokal (Laragon) atau produksi.
    val uploadsBaseUrl: String
        get() = BASE_URL.substringBefore("/routes/") + "/uploads/"

    // PERF:
    // - connect 10s (dulu 30s): konek >10s = jaringan/server bermasalah,
    //   gagal cepat lebih baik daripada UI macet 30 detik.
    // - read/write tetap 30s: upload foto & respons hosting lambat butuh ruang.
    // - retryOnConnectionFailure = false (dulu true): retry otomatis request POST
    //   yang gagal di tengah jalan bisa bikin BOOKING GANDA di server.
    // - ConnectionPool keep-alive: request berurutan pakai ulang koneksi TLS,
    //   hemat handshake tiap request.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val token = authToken
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
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
