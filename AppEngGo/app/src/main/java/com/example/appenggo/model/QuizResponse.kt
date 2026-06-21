package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

// --- Response khi bắt đầu thi ---
data class StartExamResponse(
    @SerializedName("examId")
    val examId: Int,
    @SerializedName("attemptId")
    val attemptId: Int,
    val title: String,
    val difficulty: Int,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val examType: String,
    val questions: List<ExamQuestionWrapper>
)

data class ExamQuestionWrapper(
    val orderPriority: Int,
    val question: QuestionDetail
)

data class QuestionDetail(
    val id: Int,
    val content: String,
    val questionType: String, // MULTIPLE_CHOICE, FILL_BLANK, MATCHING
    val multipleOptions: List<MultipleChoiceOption>?,
    val fillBlankOptions: List<FillBlankOption>?,
    val leftOptions: List<MatchingOption>?,
    val rightOptions: List<MatchingOption>?
)

data class MultipleChoiceOption(
    val id: Int,
    val optionText: String
)

data class FillBlankOption(
    val blankId: Int,
    val position: Int,
    val maxLength: Int,
    val placeholder: String?
)

data class MatchingOption(
    val id: Int,
    val optionText: String
)

// --- Request nộp bài ---
data class SubmitExamRequest(
    val examAnswers: List<ExamAnswerRequest>
)

data class ExamAnswerRequest(
    val questionId: Int,
    val selectedOptionId: Int?,
    val fillBlanks: List<FillBlankAnswer>?,
    val matchings: List<MatchingAnswer>?
)

data class FillBlankAnswer(
    val blankId: Int,
    val position: Int,
    val userInput: String
)

data class MatchingAnswer(
    val leftId: Int,
    val rightId: Int
)

// --- Response sau khi nộp bài ---
data class SubmitExamResponse(
    val attemptId: Int,
    val completedAt: String,
    val correctAnswersCount: Int,
    val examId: Int,
    val startedAt: String,
    val timeTakenSeconds: Int?,
    val totalQuestions: Int,
    val totalScore: Double,
    val expGained: Int
)


// Response từ server khi cả 2 join-queue (nhận qua WebSocket /topic/match/{matchId})
data class ExamPvpDisplayResponse(
    val matchId: Int,
    val examId: Int,
    val attemptId1: Int,
    val attemptId2: Int,
    val title: String,
    val examType: String?,
    val difficulty: Int?,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val questions: List<ExamQuestionWrapper>
)

// Progress realtime: mỗi lần trả lời 1 câu, server broadcast điểm
data class QuizProgressPayload(
    val userId: Int,
    val currentScore: Int,
    @com.google.gson.annotations.SerializedName("correct")
    val isCorrect: Boolean,
    val questionId: Int
)

// Kết quả trận đấu cuối cùng
data class MatchResultResponse(
    val matchId: Int,
    val winnerId: Int?,      // null = hòa
    val player1Id: Int?,     // thêm để client biết ai là player1
    val player2Id: Int?,
    val player1: PlayerResult,
    val player2: PlayerResult,
    val status: String
)

data class PlayerResult(
    val avatarUrl: String?,
    val playerScore: Int,
    val eloChange: Int?,
    val correctAnswersCount: Int?,
    val elo: Int?
)