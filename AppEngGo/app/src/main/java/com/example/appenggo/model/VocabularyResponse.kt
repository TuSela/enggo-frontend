package com.example.appenggo.model

data class ApiResponse<T>(
    val code: Int,
    val message: String? = null,
    val result: T?
)

data class ThemeResponse(
    val id: Int,
    val themeName: String,
    val category: String? = null, // JSON của bạn không có trường này
    val active: Boolean? = null   // JSON của bạn không có trường này
)
