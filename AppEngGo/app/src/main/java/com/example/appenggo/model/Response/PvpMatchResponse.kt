package com.example.appenggo.model.Response

import com.google.gson.annotations.SerializedName

/**
 * Model dùng chung để nhận thông tin ghép trận (MatchFound) 
 * và thông tin đề thi (MatchStart/ExamDisplay).
 */
data class PvpMatchResponse(
    @SerializedName(value = "matchId", alternate = ["id"])
    val matchId: Int,    // Khớp cả "id" (khi tìm thấy trận) và "matchId" (khi nhận đề thi)
    val examId: Int,
    val title: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val difficulty: Int,
    val examType: String,
    val attemptId1: Int,
    val attemptId2: Int,
    val player1Username: String?,
    val player2Username: String?,
    val questions: List<PvpQuestionMapping>? // Nullable vì lúc tìm thấy trận chưa có câu hỏi
)

data class PvpQuestionMapping(
    val orderPriority: Int,
    val question: PvpQuestion
)

data class PvpQuestion(
    val id: Int,
    val content: String,
    val questionType: String, // MULTIPLE_CHOICE, FILL_BLANK, MATCHING
    val multipleOptions: List<PvpMultipleOption>?,
    val fillBlankOptions: List<PvpFillBlankOption>?,
    val leftOptions: List<PvpMatchingOption>?,
    val rightOptions: List<PvpMatchingOption>?
)

data class PvpMultipleOption(val id: Int, val optionText: String)
data class PvpFillBlankOption(val blankId: Int, val maxLength: Int, val position: Int)
data class PvpMatchingOption(val id: Int, val optionText: String)

/**
 * Model dùng để gửi và nhận tiến độ realtime qua WebSocket
 */
data class MatchProgress(
    val matchId: Int,
    val userId: Int,
    val currentScore: Int
)
