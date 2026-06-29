package com.example.appenggo.model

data class UserUpdateRequest(
    val email: String,
    val fullName: String,
    val bio: String
)