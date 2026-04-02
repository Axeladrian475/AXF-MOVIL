package com.axf.gymnet.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Emulador → 10.0.2.2 | Celular físico → IP de tu PC, ej: 192.168.1.100
    private const val BASE_URL = "http://10.0.2.2:3001/"
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}