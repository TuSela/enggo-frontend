package com.example.appenggo.model.Response

import com.example.appenggo.model.Request.RandomBlueprintRequest

data class InviteResponse(
    val inviteId: Int,
    val inviterPlayerId: Int,
    val inviteePlayerId: Int,
    val inviterUsername: String,
    val inviteeUsername: String,
    val randomBlueprintRequest: RandomBlueprintRequest? = null,
    val status: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val exp: Int?,
    val level: Int?,
    val streakDays: Int?,
    val completedTasks: Int?,
    val pvpWins: Int?,
    val avatarUrl: String?,
    val status: String?,
    val bio: String?,
    val createdAt: String?,
    val roles: Set<RoleResponse>?
)

data class RoleResponse(
    val id: Int?,
    val name: String,
    val description: String?,
    val permissions: List<String>?
)
