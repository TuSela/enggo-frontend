package com.example.appenggo.model.Request

data class InviteRequest(
    val inviteeUsername: String,
    val randomBlueprintRequest: RandomBlueprintRequest
)

data class RandomBlueprintRequest(
    val difficulty: Byte,
    val questionTypes: List<String> = listOf("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING"),
    val themeIds: List<Int>,
    val totalQuestions: Int
)

data class InviteRespondRequest(
    val inviteId: Int,
    val accepted: Boolean
)
