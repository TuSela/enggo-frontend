package com.example.appenggo

import com.example.appenggo.model.*
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

    @POST("api/exams/random")
    suspend fun getRandomExam(
        @Header("Authorization") token: String,
        @Body request: RandomExamRequest
    ): ApiResponse<RandomExamResponse>

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

    // ===== FRIEND APIs =====

    @GET("api/social/friends")
    suspend fun getAllFriends(
        @Header("Authorization") token: String
    ): ApiResponse<List<FriendResponse>>

    @GET("api/social/friends/online")
    suspend fun getOnlineFriends(
        @Header("Authorization") token: String
    ): ApiResponse<List<FriendResponse>>

    @GET("api/social/friends/search")
    suspend fun searchFriends(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String
    ): ApiResponse<List<FriendResponse>>

    @POST("api/social/friends/request/{receiverId}")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Path("receiverId") receiverId: Int
    ): ApiResponse<Boolean>

    @POST("api/social/friends/accept/{requestId}")
    suspend fun acceptFriendRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: Int
    ): ApiResponse<Boolean>

    @DELETE("api/social/friends/reject/{requestId}")
    suspend fun rejectFriendRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: Int
    ): ApiResponse<Boolean>

    @DELETE("api/social/friends/unfriend/{targetUserId}")
    suspend fun unfriend(
        @Header("Authorization") token: String,
        @Path("targetUserId") targetUserId: Int
    ): ApiResponse<Boolean>

    // ===== CONVERSATION (CHAT) APIs =====

    @POST("api/social/conversations/private/{targetUserId}")
    suspend fun openPrivateChat(
        @Header("Authorization") token: String,
        @Path("targetUserId") targetUserId: Int
    ): ApiResponse<ConversationResponse>

    @GET("api/social/conversations")
    suspend fun getMyConversations(
        @Header("Authorization") token: String
    ): ApiResponse<List<ConversationResponse>>

    @POST("api/social/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: Int,
        @Body request: SendMessageRequest
    ): ApiResponse<MessageResponse>

    @GET("api/social/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<MessageResponse>>
}