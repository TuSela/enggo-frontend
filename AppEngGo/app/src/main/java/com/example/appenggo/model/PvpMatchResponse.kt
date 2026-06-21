package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Data class mirroring the backend DTO `PvpMatchResponse`.
 * Used by the Android client to deserialize the JSON payload returned by the
 * PVP direct match APIs.
 */
data class PvpMatchResponse(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("player1Id")
    val player1Id: Int?,
    @SerializedName("avatarUrlP1")
    val avatarUrlP1: String?,
    @SerializedName("player1Username")
    val player1Username: String?,
    @SerializedName("eloP1")
    val eloP1: Int?,
    @SerializedName("player1AttemptId")
    val player1AttemptId: Int?,

    @SerializedName("player2Id")
    val player2Id: Int?,
    @SerializedName("avatarUrlP2")
    val avatarUrlP2: String?,
    @SerializedName("player2Username")
    val player2Username: String?,
    @SerializedName("eloP2")
    val eloP2: Int?,
    @SerializedName("player2AttemptId")
    val player2AttemptId: Int?,

    @SerializedName("examId")
    val examId: Int?,
    @SerializedName("examTitle")
    val examTitle: String?,

    @SerializedName("player1Score")
    val player1Score: Int?,
    @SerializedName("player2Score")
    val player2Score: Int?,

    @SerializedName("winnerId")
    val winnerId: Int?,
    @SerializedName("winnerUsername")
    val winnerUsername: String?,

    @SerializedName("status")
    val status: String?,

    // Dates are received as ISO‑8601 strings; keep them as String for now.
    @SerializedName("startTime")
    val startTime: String?,
    @SerializedName("endTime")
    val endTime: String?,
    @SerializedName("createdAt")
    val createdAt: String?
)
