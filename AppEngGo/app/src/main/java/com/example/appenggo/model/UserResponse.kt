package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?,
    @SerializedName("exp") val exp: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("elo") val elo: Int,
    @SerializedName("badgeRank") val badgeRank: BadgeRank?,
    @SerializedName("pvpWins") val pvpWins: Int,
    @SerializedName("winStreak") val winStreak: Int
)
