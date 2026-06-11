package com.example.appenggo.model.Response

import com.google.gson.annotations.SerializedName

data class MatchResultResponse(
    val matchId: Int,
    val winnerId: Int,
    val player1: PlayerResult,
    val player2: PlayerResult,
    val status: String
) {
    data class PlayerResult(
        val avatarUrl: String?,
        val playerScore: Int,
        val eloChange: Int,
        val correctAnswersCount: Int,
        val elo: Int
    )
}
