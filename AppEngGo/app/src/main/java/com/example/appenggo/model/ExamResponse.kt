package com.example.appenggo.model

data class PageResponse<T>(
    val content: List<T>,
    val last: Boolean,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class ExamItemResponse(
    val id: Int,
    val title: String,
    val difficulty: Int,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val examType: String,
    val active: Boolean,
    val createdBy: CreatorResponse,
    val themes: List<ThemeResponse>,
    val skills: List<SkillResponse>
)

data class CreatorResponse(
    val id: Int,
    val username: String,
    val avatarUrl: String?,
    val level: Int,
    val status: String
)

data class SkillResponse(
    val id: Int,
    val skillName: String
)
