package com.axf.gymnet.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * URL base del backend.
     *
     * ─── PRODUCCIÓN ───────────────────────────────────────────────────────────
     *   Cambia esta URL por la dirección real de tu servidor:
     *   const val BASE_URL = "https://tudominio.com/"
     *
     * ─── DESARROLLO (emulador Android) ───────────────────────────────────────
     *   El emulador usa 10.0.2.2 para acceder al localhost del PC:
     *   const val BASE_URL = "http://10.0.2.2:3001/"
     *
     * ─── DESARROLLO (dispositivo físico) ──────────────────────────────────────
     *   Usa la IP local de tu PC (ej: 192.168.1.100):
     *   const val BASE_URL = "http://192.168.1.100:3001/"
     *
     * ─────────────────────────────────────────────────────────────────────────
     * IMPORTANTE: Si usas HTTP (no HTTPS), asegúrate de tener
     *   android:usesCleartextTraffic="true"  en el AndroidManifest.xml
     * ─────────────────────────────────────────────────────────────────────────
     */
    const val BASE_URL = "http://10.0.2.2:3001/"   // ← CAMBIA ESTO

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}