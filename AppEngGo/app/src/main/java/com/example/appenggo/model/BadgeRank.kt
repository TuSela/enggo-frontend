package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class BadgeRank(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("badgeName") val badgeName: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("iconUrl") val iconUrl: String? = null
)
