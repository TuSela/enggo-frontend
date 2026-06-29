package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("exp") val exp: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("streakDays") val streakDays: Int,
    @SerializedName("completedTasks") val completedTasks: Int,
    @SerializedName("pvpWins") val pvpWins: Int,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("elo") val elo: Int,
    @SerializedName("winStreak") val winStreak: Int,
    @SerializedName("badgeRank") val badgeRank: BadgeRank?,
    @SerializedName("leaderboardRank") val leaderboardRank: Int
)
