package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class MissionResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("targetValue") val targetValue: Int,
    @SerializedName("expReward") val expReward: Int,
    @SerializedName("type") val type: String  // e.g. "QUIZ", "VOCABULARY", "PVP"
)

data class MissionProgressResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("missionResponse") val missionResponse: MissionResponse,
    @SerializedName("currentValue") val currentValue: Int,
    @SerializedName("status") val status: String,   // "IN_PROGRESS" | "COMPLETED" | "CLAIMED"
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class BadgeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("iconUrl") val iconUrl: String?
)

data class ClaimRewardResponse(
    @SerializedName("expAwarded") val expAwarded: Int,
    @SerializedName("newTotalExp") val newTotalExp: Int,
    @SerializedName("status") val status: String,
    @SerializedName("badgeResponse") val badgeResponse: List<BadgeResponse>?
)