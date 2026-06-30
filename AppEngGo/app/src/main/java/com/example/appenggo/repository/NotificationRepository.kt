package com.example.appenggo.model

object NotificationRepository {
    private val _list = mutableListOf<NotificationPayload>()
    val list: List<NotificationPayload> get() = _list.toList()

    fun add(payload: NotificationPayload) {
        // Tránh trùng lặp nếu cần, hoặc cứ add vào đầu
        _list.add(0, payload)
    }

    // Dùng khi nạp dữ liệu pending từ REST API lúc mở app/login,
    // tránh add trùng nếu cùng request đã được nhận trước đó qua WebSocket.
    fun addIfAbsent(payload: NotificationPayload) {
        val exists = _list.any { it.type == payload.type && it.requestId == payload.requestId }
        if (!exists) _list.add(0, payload)
    }

    fun remove(payload: NotificationPayload) {
        _list.remove(payload)
    }

    fun removeByRequestId(requestId: Int?) {
        _list.removeAll { it.requestId == requestId }
    }

    fun clearAll() {
        _list.clear()
    }
}