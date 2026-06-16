package com.example.appenggo.model

data class UserSearchResponse(
    val id: Int,
    val username: String,
    val avatarUrl: String?,
    val level: Int?,
    val status: String?
)