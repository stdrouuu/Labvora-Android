// Singleton untuk konfigurasi Retrofit, menentukan Base URL dan Converter
package org.ukrida.labvora.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // private const val BASE_URL = "http://192.168.2"
    const val BASE_URL = "http://10.0.2.2/Diagnos-API/routes/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
