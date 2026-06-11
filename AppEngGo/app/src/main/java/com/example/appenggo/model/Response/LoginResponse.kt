package com.example.appenggo.model.Response

data class LoginResponse(
    val code: Int,
    val result: ResultData
)

data class ResultData(
    val token: String,
    val authenticated: Boolean,
    val userId: Int // Thêm trường này để nhận ID từ Server
)