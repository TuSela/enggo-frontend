package com.example.appenggo.model.Request

/**
 * Khớp với nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest
 */
data class ExamSubmitRequest(
    val examAnswers: List<ExamAnswerRequest>
)

/**
 * Khớp với nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest
 */
data class ExamAnswerRequest(
    val questionId: Int,
    val selectedOptionId: Int? = null,
    val fillBlanks: List<FillBlankSubmitRequest>? = null,
    val matchings: List<MatchingSubmitRequest>? = null
)

/**
 * Khớp với nhom12.enggo_backend.dto.request.exam.FillBlankSubmitRequest
 */
data class FillBlankSubmitRequest(
    val blankId: Int,
    val position: Int,
    val userInput: String
)

/**
 * Khớp với nhom12.enggo_backend.dto.request.exam.MatchingSubmitRequest
 */
data class MatchingSubmitRequest(
    val leftId: Int,
    val rightId: Int
)

/**
 * Khớp với nhom12.enggo_backend.dto.response.gamification.QuizProgressResponse
 */
data class QuizProgressResponse(
    val userId: Int,
    val currentScore: Int,
    val isCorrect: Boolean,
    val questionId: Int
)
