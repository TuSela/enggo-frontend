package com.example.appenggo.model.Request

    data class PracticeSessionRequest(
        val themeId: Int,
        val difficulty: Int,
        val questionCount: Int
    )
