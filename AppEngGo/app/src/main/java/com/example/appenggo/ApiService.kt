package com.example.appenggo

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/users/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<Void>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}