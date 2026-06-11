package com.example.appenggo

import com.example.appenggo.model.Request.LoginRequest
import com.example.appenggo.model.Request.SignupRequest
import com.example.appenggo.model.Response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/users/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<Void>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/themes/all")
    suspend fun getAllThemesGroupedByCategory(
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, List<ThemeResponse>>>

    @GET("api/exams/all")
    suspend fun getExams(
        @Header("Authorization") token: String,
        @Query("themeIds") themeIds: Int,
        @Query("diffs") diffs: Int
    ): ApiResponse<PageResponse<ExamItemResponse>>

    @GET("api/exams/{id}/start")
    suspend fun startExam(
        @Header("Authorization") token: String,
        @Path("id") examId: Int
    ): ApiResponse<StartExamResponse>

    @POST("api/exams/{examId}/attempt/{attemptId}/submit")
    suspend fun submitExam(
        @Header("Authorization") token: String,
        @Path("examId") examId: Int,
        @Path("attemptId") attemptId: Int,
        @Body request: SubmitExamRequest
    ): ApiResponse<SubmitExamResponse>

    // Friend API
    @POST("api/friends/request/{receiverId}")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Path("receiverId") receiverId: Int
    ): ApiResponse<Boolean>
    
    @GET("api/social/friends")
    suspend fun getFriends(
        @Header("Authorization") token: String
    ): ApiResponse<List<UserResponse>>
}
