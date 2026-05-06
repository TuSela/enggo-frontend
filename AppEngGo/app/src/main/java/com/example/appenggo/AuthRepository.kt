package com.example.appenggo

import retrofit2.Response

class AuthRepository {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return RetrofitClient.api.login(request)
    }

    suspend fun signup(request: SignupRequest): Response<Void> {
        return RetrofitClient.api.signup(request)
    }
}