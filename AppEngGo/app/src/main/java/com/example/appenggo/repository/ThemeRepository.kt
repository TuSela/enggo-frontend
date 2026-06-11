package com.example.appenggo.repository

import com.example.appenggo.ApiService
import com.example.appenggo.model.Response.SubmitExamRequest

class ThemeRepository(private val apiService: ApiService) {
    suspend fun getAllThemes(token: String) = apiService.getAllThemesGroupedByCategory(token)

    suspend fun getExams(token: String, themeId: Int, difficulty: Int) =
        apiService.getExams(token, themeId, difficulty)

    suspend fun startExam(token: String, examId: Int) =
        apiService.startExam(token, examId)

    suspend fun submitExam(token: String, examId: Int, attemptId: Int, request: SubmitExamRequest) =
        apiService.submitExam(token, examId, attemptId, request)
}