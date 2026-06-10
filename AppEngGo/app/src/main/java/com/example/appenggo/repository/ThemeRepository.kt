package com.example.appenggo.repository

import com.example.appenggo.ApiService
import com.example.appenggo.model.SubmitExamRequest
import com.example.appenggo.model.RandomExamRequest

class ThemeRepository(private val apiService: ApiService) {

    // Public endpoint - không cần Bearer
    suspend fun getAllThemes(token: String) =
        apiService.getAllThemesGroupedByCategory(token)

    suspend fun getExams(token: String, themeId: Int, difficulty: Int) =
        apiService.getExams("Bearer $token", themeId, difficulty)

    suspend fun startExam(token: String, examId: Int) =
        apiService.startExam("Bearer $token", examId)

    suspend fun getRandomExam(token: String, request: RandomExamRequest) =
        apiService.getRandomExam("Bearer $token", request)

    suspend fun submitExam(token: String, examId: Int, attemptId: Int, request: SubmitExamRequest) =
        apiService.submitExam("Bearer $token", examId, attemptId, request)
}