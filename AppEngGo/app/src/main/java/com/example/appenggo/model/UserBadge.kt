package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class UserBadge(
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("badgeId") val badgeId: Int,
    @SerializedName("badgeName") val badgeName: String,
    @SerializedName("description") val description: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("earnedAt") val earnedAt: String
)
