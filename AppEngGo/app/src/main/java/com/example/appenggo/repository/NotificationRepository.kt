package com.example.appenggo.model

object NotificationRepository {
    private val _list = mutableListOf<NotificationPayload>()
    val list: List<NotificationPayload> get() = _list.toList()

    fun add(payload: NotificationPayload) {
        _list.add(0, payload)
    }

    fun clearAll() {
        _list.clear()
    }
}