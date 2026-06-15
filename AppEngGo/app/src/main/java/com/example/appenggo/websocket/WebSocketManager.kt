package com.example.appenggo.websocket

import android.content.Context
import android.util.Log
import com.example.appenggo.model.MessageResponse
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

object WebSocketManager {

    private const val TAG = "WebSocketManager"
    private const val WS_URL = "ws://10.0.2.2:8080/api/ws/websocket"

    private var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()

    // Callbacks
    var onNotificationReceived: ((NotificationPayload) -> Unit)? = null
    var onStatusChanged: ((Int, String) -> Unit)? = null
    var onChatMessageReceived: ((ChatMessageEvent) -> Unit)? = null

    fun connect(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null) ?: run {
            Log.e(TAG, "❌ Không tìm thấy TOKEN"); return
        }
        val username = prefs.getString("USERNAME", null) ?: run {
            Log.e(TAG, "❌ Không tìm thấy USERNAME"); return
        }

        Log.d(TAG, "Connecting với username: $username")

        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            WS_URL,
            mapOf("Authorization" to "Bearer $token")
        )

        disposables.add(
            stompClient!!.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    when (event.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d(TAG, "✅ WebSocket connected")
                            subscribeTopics()
                        }
                        LifecycleEvent.Type.CLOSED -> Log.d(TAG, "❌ WebSocket disconnected")
                        LifecycleEvent.Type.ERROR -> Log.e(TAG, "⚠️ Error: ${event.exception?.message}")
                        else -> {}
                    }
                }, { Log.e(TAG, "Lifecycle error: ${it.message}") })
        )

        stompClient!!.connect()
    }

    private fun subscribeTopics() {
        // Thông báo cá nhân (friend request...)
        disposables.add(
            stompClient!!.topic("/user/queue/notifications")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "🔔 Notification: ${msg.payload}")
                    try {
                        val payload = Gson().fromJson(msg.payload, NotificationPayload::class.java)
                        onNotificationReceived?.invoke(payload)
                    } catch (e: Exception) { Log.e(TAG, "Parse error: ${e.message}") }
                }, { Log.e(TAG, "Notification error: ${it.message}") })
        )

        // Tin nhắn chat realtime
        disposables.add(
            stompClient!!.topic("/user/queue/chat")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "💬 Chat message: ${msg.payload}")
                    try {
                        val event = Gson().fromJson(msg.payload, ChatMessageEvent::class.java)
                        onChatMessageReceived?.invoke(event)
                    } catch (e: Exception) { Log.e(TAG, "Parse chat error: ${e.message}") }
                }, { Log.e(TAG, "Chat subscribe error: ${it.message}") })
        )

        // Status online/offline
        disposables.add(
            stompClient!!.topic("/topic/status")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    try {
                        val payload = Gson().fromJson(msg.payload, StatusPayload::class.java)
                        onStatusChanged?.invoke(payload.userId, payload.status)
                    } catch (e: Exception) { Log.e(TAG, "Parse status error: ${e.message}") }
                }, { Log.e(TAG, "Status error: ${it.message}") })
        )
    }

    // Gửi tin nhắn qua WebSocket
    fun sendMessage(jsonPayload: String) {
        stompClient?.send("/app/chat.send", jsonPayload)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { Log.d(TAG, "✅ Message sent") },
                { Log.e(TAG, "❌ Send error: ${it.message}") }
            )
    }

    fun disconnect() {
        disposables.clear()
        stompClient?.disconnect()
        stompClient = null
        Log.d(TAG, "WebSocket disconnected manually")
    }

    fun isConnected(): Boolean = stompClient?.isConnected == true

    // Data classes
    data class NotificationPayload(
        val type: String,
        val fromUserId: Int,
        val fromUsername: String,
        val message: String,
        val requestId: Int?
    )

    data class StatusPayload(
        val userId: Int,
        val status: String
    )

    data class ChatMessageEvent(
        val conversationId: Int,
        val id: Int,
        val senderId: Int,
        val senderUsername: String,
        val content: String,
        val type: String,
        val createdAt: String?
    ) {
        fun toMessageResponse() = MessageResponse(
            id = id,
            senderId = senderId,
            senderUsername = senderUsername,
            content = content,
            type = type,
            createdAt = createdAt
        )
    }
}