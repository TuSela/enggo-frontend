package com.example.appenggo.model

// Request gửi tin nhắn
data class SendMessageRequest(
    val content: String,
    val type: String = "TEXT"
)

// Response conversation
data class ConversationResponse(
    val id: Int,
    val name: String?,
    val type: String,
    val lastMessageContent: String?,
    val updatedAt: String?
)

// Response tin nhắn
data class MessageResponse(
    val id: Int,
    val senderId: Int,
    val senderUsername: String,
    val content: String,
    val type: String,
    val createdAt: String?
)