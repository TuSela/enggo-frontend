package com.example.appenggo.websocket

import android.content.Context
import android.util.Log
import com.example.appenggo.model.MessageResponse
import com.example.appenggo.model.MatchResultResponse
import com.example.appenggo.model.NotificationPayload
import com.example.appenggo.model.QuizProgressPayload
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

object WebSocketManager {

    private const val TAG = "WebSocketManager"
    private const val WS_URL = "ws://13.158.23.199:8080/api/ws/websocket"

    var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()
    private val gson = Gson()

    // ── Callbacks ────────────────────────────────────────────────────────────
    var onNotificationReceived: ((NotificationPayload) -> Unit)? = null
    var onStatusChanged: ((Int, String) -> Unit)? = null
    var onChatMessageReceived: ((ChatMessageEvent) -> Unit)? = null
    var onPvpEventReceived: ((Map<String, Any>) -> Unit)? = null
    var onPvpExamReceived: ((Any) -> Unit)? = null

    // PVP realtime callbacks
    var onPvpProgressReceived: ((QuizProgressPayload) -> Unit)? = null
    var onPvpResultReceived: ((MatchResultResponse) -> Unit)? = null
    var onQueueStatusReceived: ((String) -> Unit)? = null

    // ── Connect ──────────────────────────────────────────────────────────────
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

    // ── Subscribe topics mặc định ────────────────────────────────────────────
    private fun subscribeTopics() {
        // Thông báo cá nhân
        disposables.add(
            stompClient!!.topic("/user/queue/notifications")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "🔔 Notification: ${msg.payload}")
                    try {
                        val payload = gson.fromJson(msg.payload, NotificationPayload::class.java)
                        onNotificationReceived?.invoke(payload)
                    } catch (e: Exception) { Log.e(TAG, "Parse error: ${e.message}") }
                }, { Log.e(TAG, "Notification error: ${it.message}") })
        )

        // Tin nhắn chat
        disposables.add(
            stompClient!!.topic("/user/queue/chat")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    try {
                        val event = gson.fromJson(msg.payload, ChatMessageEvent::class.java)
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
                        val payload = gson.fromJson(msg.payload, StatusPayload::class.java)
                        onStatusChanged?.invoke(payload.userId, payload.status)
                    } catch (e: Exception) { Log.e(TAG, "Parse status error: ${e.message}") }
                }, { Log.e(TAG, "Status error: ${it.message}") })
        )

        // PVP events cá nhân (invite, accepted, declined, ready, start)
        disposables.add(
            stompClient!!.topic("/user/queue/pvp")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "⚔️ PVP event: ${msg.payload}")
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val event = gson.fromJson(msg.payload, Any::class.java) as Map<String, Any>
                        onPvpEventReceived?.invoke(event)

                        if (event["type"] == "PVP_INVITE") {
                            val payload = NotificationPayload(
                                type = "PVP_INVITE",
                                fromUserId = (event["fromUserId"] as? Double)?.toInt() ?: 0,
                                fromUsername = event["fromUsername"] as? String ?: "",
                                message = event["message"] as? String ?: "",
                                requestId = (event["matchId"] as? Double)?.toInt()
                            )
                            onNotificationReceived?.invoke(payload)
                        }
                    } catch (e: Exception) { Log.e(TAG, "PVP parse error: ${e.message}") }
                }, { Log.e(TAG, "PVP subscribe error: ${it.message}") })
        )
    }

    // ── PVP matching ─────────────────────────────────────────────────────────
    // Lắng nghe trạng thái hàng đợi cá nhân (WAITING, WAITING_FOR_ENEMY_READY...)
    // Server gửi qua convertAndSendToUser(username, "/queue/queue-status", ...)
    // -> client subscribe đúng "/user/queue/queue-status".
    private var queueStatusSubscription: Disposable? = null

    fun subscribeToMyQueueStatus() {
        // Hủy đăng ký cũ nếu có để tránh trùng lặp luồng dữ liệu
        queueStatusSubscription?.dispose()

        queueStatusSubscription = stompClient!!.topic("/user/queue/queue-status")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msg ->
                // Loại bỏ dấu ngoặc kép dư thừa nếu server trả về chuỗi thuần túy dạng JSON
                val status = msg.payload.replace("\"", "")
                Log.d(TAG, "🎁 Nhận được trạng thái hàng đợi: $status")
                onQueueStatusReceived?.invoke(status)
            }, { error ->
                Log.e(TAG, "❌ Lỗi lắng nghe Queue status: ${error.message}")
            })

        disposables.add(queueStatusSubscription!!)
    }
    private var myMatchSubscription: Disposable? = null

    fun subscribeToMatchFound(myUserId: Int) {
        myMatchSubscription?.dispose() // Xóa sub cũ nếu có

        myMatchSubscription = stompClient!!.topic("/topic/match/$myUserId")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msg ->
                Log.d(TAG, "⚔️ Match found info received: ${msg.payload}")
                val payload = msg.payload.trim()

                // Server có lúc gửi JSON object (thông tin trận đấu),
                // có lúc gửi chuỗi thuần như MATCH_TIMEOUT/CANCELLED (không phải JSON hợp lệ).
                // Phải tách 2 trường hợp này, nếu không Gson sẽ throw và callback sẽ không
                // bao giờ được gọi khi trận bị huỷ.
                if (payload.startsWith("{")) {
                    try {
                        val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
                        val eventMap: Map<String, Any> = gson.fromJson(payload, type)
                        onPvpEventReceived?.invoke(eventMap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing match found payload: ${e.message}")
                    }
                } else {
                    // Chuỗi trạng thái thuần, ví dụ "MATCH_TIMEOUT" hoặc "CANCELLED"
                    val status = payload.replace("\"", "")
                    onPvpEventReceived?.invoke(mapOf("status" to status))
                }
            }, { Log.e(TAG, "Match found subscribe error: ${it.message}") })

        disposables.add(myMatchSubscription!!)
    }
    fun sendFindMatch() {
        stompClient?.send("/app/find-match", "{}")
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d(TAG, "✅ Sent find-match") }, { Log.e(TAG, "❌ Find-match error: ${it.message}") })
    }

    fun sendLeaveQueue(userId: Int) {
        stompClient?.send("/app/leave-queue", userId.toString())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d(TAG, "✅ Sent leave-queue") }, { Log.e(TAG, "❌ Leave-queue error: ${it.message}") })
    }

    fun sendJoinQueue(matchId: Int) {
        stompClient?.send("/app/join-queue", matchId.toString())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d(TAG, "✅ Sent join-queue") }, { Log.e(TAG, "❌ Join-queue error: ${it.message}") })
    }

    fun subscribeToMatchSession(matchId: Int) {
        disposables.add(
            stompClient!!.topic("/topic/match/$matchId")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "📝 Match $matchId session data: ${msg.payload}")
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val event = gson.fromJson(msg.payload, Any::class.java) as Map<String, Any>
                        onPvpEventReceived?.invoke(event)
                    } catch (e: Exception) { Log.e(TAG, "Match session parse error: ${e.message}") }
                }, { Log.e(TAG, "Match session subscribe error: ${it.message}") })
        )
    }

    fun clearPvpCallbacks() {
        onQueueStatusReceived = null
        onPvpEventReceived = null
        onPvpProgressReceived = null
        onPvpResultReceived = null
        onPvpExamReceived = null
    }

    // ── PVP match: join queue + subscribe đề + progress + result ────────────
    fun joinPvpQueue(matchId: Int) {
        stompClient?.send("/app/join-queue", matchId.toString())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { Log.d(TAG, "✅ Joined PVP queue for match $matchId") },
                { Log.e(TAG, "❌ Join queue error: ${it.message}") }
            )

        // Nhận đề thi khi cả 2 đã join
        disposables.add(
            stompClient!!.topic("/topic/match/$matchId")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "📝 Match $matchId data: ${msg.payload}")
                    try {
                        val data = gson.fromJson(msg.payload, Any::class.java)
                        onPvpExamReceived?.invoke(data)
                    } catch (e: Exception) { Log.e(TAG, "Match data parse error: ${e.message}") }
                }, { Log.e(TAG, "Match subscribe error: ${it.message}") })
        )

        // Nhận progress realtime của đối thủ
        disposables.add(
            stompClient!!.topic("/topic/match/$matchId/progress")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "📊 PVP Progress: ${msg.payload}")
                    try {
                        val progress = gson.fromJson(msg.payload, QuizProgressPayload::class.java)
                        onPvpProgressReceived?.invoke(progress)
                    } catch (e: Exception) { Log.e(TAG, "Progress parse error: ${e.message}") }
                }, { Log.e(TAG, "Progress subscribe error: ${it.message}") })
        )

        // Nhận kết quả khi cả 2 đã nộp bài
        disposables.add(
            stompClient!!.topic("/topic/match/$matchId/result")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.d(TAG, "🏆 PVP Result: ${msg.payload}")
                    try {
                        val result = gson.fromJson(msg.payload, MatchResultResponse::class.java)
                        onPvpResultReceived?.invoke(result)
                    } catch (e: Exception) { Log.e(TAG, "Result parse error: ${e.message}") }
                }, { Log.e(TAG, "Result subscribe error: ${it.message}") })
        )
    }

    // ── Gửi progress khi trả lời 1 câu ─────────────────────────────────────
    fun sendPvpProgress(matchId: Int, questionId: Int, selectedOptionId: Int?,
                        fillBlanks: Any?, matchings: Any?) {
        val payload = mapOf(
            "questionId" to questionId,
            "selectedOptionId" to selectedOptionId,
            "fillBlanks" to fillBlanks,
            "matchings" to matchings
        )
        stompClient?.send("/app/match/$matchId/progress", gson.toJson(payload))
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { Log.d(TAG, "✅ Progress sent q=$questionId") },
                { Log.e(TAG, "❌ Progress send error: ${it.message}") }
            )
    }

    // ── Submit toàn bộ bài PVP qua WebSocket ────────────────────────────────
    fun submitPvpExam(matchId: Int, answersJson: String) {
        stompClient?.send("/app/match/$matchId/submit", answersJson)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { Log.d(TAG, "✅ PVP exam submitted for match $matchId") },
                { Log.e(TAG, "❌ PVP submit error: ${it.message}") }
            )
    }

    // ── Gửi chat ─────────────────────────────────────────────────────────────
    fun sendMessage(jsonPayload: String) {
        stompClient?.send("/app/chat.send", jsonPayload)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { Log.d(TAG, "✅ Message sent") },
                { Log.e(TAG, "❌ Send error: ${it.message}") }
            )
    }

    // ── Disconnect ───────────────────────────────────────────────────────────
    fun disconnect() {
        disposables.clear()
        stompClient?.disconnect()
        stompClient = null
        Log.d(TAG, "WebSocket disconnected manually")
    }

    fun isConnected(): Boolean = stompClient?.isConnected == true

    // ── Data classes ─────────────────────────────────────────────────────────
    data class StatusPayload(val userId: Int, val status: String)

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