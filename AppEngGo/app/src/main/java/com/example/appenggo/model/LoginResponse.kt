package com.example.appenggo.model

data class LoginResponse(
    val code: Int,
    val result: ResultData
)

data class ResultData(
    val token: String,
    val authenticated: Boolean
)