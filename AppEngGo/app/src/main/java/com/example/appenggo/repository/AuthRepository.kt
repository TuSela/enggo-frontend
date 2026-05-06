package com.example.appenggo.repository

import com.example.appenggo.ApiService
import com.example.appenggo.model.LoginRequest
import com.example.appenggo.model.LoginResponse
import com.example.appenggo.model.SignupRequest
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return apiService.login(request)
    }

    suspend fun signup(request: SignupRequest): Response<Void> {
        return apiService.signup(request)
    }
}