package com.example.appenggo.repository

import com.example.appenggo.ApiService
import com.example.appenggo.model.ApiResponse
import com.example.appenggo.model.UpdatePasswordRequest
import com.example.appenggo.model.UserBadge
import com.example.appenggo.model.UserResponse
import com.example.appenggo.model.UserUpdateRequest

class UserRepository(private val apiService: ApiService) {
    suspend fun getMyInfo(token: String): ApiResponse<UserResponse> {
        return apiService.getMyInfo("Bearer $token")
    }

    suspend fun getMyBadges(token: String): ApiResponse<List<UserBadge>> {
        return apiService.getMyBadges("Bearer $token")
    }

    suspend fun updatePassword(token: String, request: UpdatePasswordRequest): ApiResponse<String> {
        return apiService.updateUserPassword("Bearer $token", request)
    }

    suspend fun updateUser(token: String, request: UserUpdateRequest): ApiResponse<UserResponse> {
        return apiService.updateUser("Bearer $token", request)
    }
}
