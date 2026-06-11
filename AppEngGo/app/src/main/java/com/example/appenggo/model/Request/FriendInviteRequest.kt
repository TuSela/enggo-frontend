package com.example.appenggo.model.Request

data class InviteRequest(val inviteeUsername: String)

data class InviteRespondRequest(
    val inviteId: Int,
    val accepted: Boolean
)
