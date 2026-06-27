package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

data class UpdatePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmNewPassword") val confirmNewPassword: String
)
