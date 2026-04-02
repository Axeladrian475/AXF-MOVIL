package com.axf.gymnet.network

import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/suscriptores/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}