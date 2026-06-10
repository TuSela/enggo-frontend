package com.example.appenggo.model

data class RandomExamRequest(
    val themeIds: List<Int>,
    val difficulty: Int,
    val totalQuestions: Int,
    val questionTypes: List<String> = listOf("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING")
)

// Response từ POST /api/exams/random
data class RandomExamResponse(
    val id: Int,
    val title: String,
    val difficulty: Int,
    val durationMinutes: Int,
    val examType: String,
    val totalQuestions: Int,
    val active: Boolean
)