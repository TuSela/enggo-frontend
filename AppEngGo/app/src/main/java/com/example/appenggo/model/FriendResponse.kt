package com.example.appenggo.model

data class FriendResponse(
    val userId: Int,
    val username: String,
    val avatarUrl: String?,
    val bio: String?,
    val level: Int?,
    val online: Boolean
)