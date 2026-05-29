package com.example.appenggo.model

    data class PracticeSessionRequest(
        val themeId: Int,
        val difficulty: Int,
        val questionCount: Int
    )
