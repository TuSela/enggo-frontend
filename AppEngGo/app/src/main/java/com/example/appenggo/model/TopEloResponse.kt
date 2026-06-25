package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for the GET /users/top-elo API.
 * It contains a list of top users and the current user's rank.
 */

data class TopEloResponse(
    @SerializedName("topUsers") val topUsers: List<UserRank> = emptyList(),
    @SerializedName("myRank") val myRank: UserRank? = null
)

/**
 * Represents a user in the ranking list.
 * Only the fields required for the UI are included here.
 */

data class UserRank(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("elo") val elo: Int = 0,
    @SerializedName("badgeRank") val badgeRank: BadgeRank? = null,
    @SerializedName("level") val level: Int = 0
)

/**
 * Badge information for a user rank.
 */

data class BadgeRank(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("badgeName") val badgeName: String = "",
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("description") val description: String = ""
)