package com.axf.gymnet.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Para emulador Android Studio usa 10.0.2.2
    // Para dispositivo físico usa la IP de tu PC en la red local, ej: 192.168.1.X
    private const val BASE_URL = "http://10.0.2.2:3001/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}