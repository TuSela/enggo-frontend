package com.example.appenggo.repository

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

/**
 * Repository quản lý kết nối WebSocket PvP dùng chung cho toàn bộ ứng dụng.
 */
class PvpRepository {

    companion object {
        @Volatile
        private var mStompClient: StompClient? = null

        private const val SERVER_IP = "192.168.2.6"
        private const val WS_URL = "ws://$SERVER_IP:8080/api/ws/websocket"

        @Synchronized
        fun getStompClient(): StompClient {
            if (mStompClient == null) {
                Log.d("PVP_WS", "Khởi tạo StompClient tại: $WS_URL")
                mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL)
            }
            return mStompClient!!
        }
    }

    fun connectWebSocket(token: String, onConnected: () -> Unit, onError: (Throwable) -> Unit): Disposable? {
        val client = getStompClient()

        if (client.isConnected) {
            Log.d("PVP_WS", "WebSocket đã kết nối sẵn.")
            onConnected()
            return null
        }

        // Đảm bảo token có tiền tố Bearer
        val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

        return client.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        Log.d("PVP_WS", "WebSocket kết nối thành công!")
                        onConnected()
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        Log.e("PVP_WS", "Lỗi kết nối WebSocket!", lifecycleEvent.exception)
                        onError(lifecycleEvent.exception ?: Exception("Unknown error"))
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        Log.d("PVP_WS", "WebSocket đã đóng.")
                    }
                    else -> {
                        Log.d("PVP_WS", "Sự kiện WebSocket: ${lifecycleEvent.type}")
                    }
                }
            }.also {
                val headers = listOf(StompHeader("Authorization", formattedToken))
                Log.d("PVP_WS", "Bắt đầu kết nối WebSocket...")
                client.connect(headers)
            }
    }

    fun subscribeMatchProgress(matchId: Int, onProgressReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/match/$matchId/progress")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ message -> onProgressReceived(message.payload) }, { it.printStackTrace() })
    }

    fun unsubscribe(disposable: Disposable?) {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
                Log.d("PVP_WS", "Đã hủy đăng ký Topic (Disposable disposed)")
            }
        }
    }

    fun subscribeMatchResult(matchId: Int, onResultReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/match/$matchId/result")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ message -> onResultReceived(message.payload) }, { it.printStackTrace() })
    }

    fun sendMatchProgress(matchId: Int, jsonRequest: String) {
        val client = getStompClient()
        if (client.isConnected) {
            client.send("/app/match/$matchId/progress", jsonRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("PVP_WS", "Gửi Progress thành công")
                }, {
                    Log.e("PVP_WS", "Gửi Progress thất bại", it)
                })
        } else {
            Log.e("PVP_WS", "Không thể gửi progress: WebSocket chưa kết nối!")
        }
    }

    fun sendQuizSubmit(matchId: Int, jsonRequest: String) {
        val client = getStompClient()
        if (client.isConnected) {
            Log.d("PVP_WS", "Submit tới /app/match/$matchId/submit: $jsonRequest")
            client.send("/app/match/$matchId/submit", jsonRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("PVP_WS", "Gửi Submit thành công")
                }, {
                    Log.e("PVP_WS", "Gửi Submit thất bại", it)
                })
        } else {
            Log.e("PVP_WS", "Không thể gửi Submit: WebSocket chưa kết nối!")
        }
    }

    // --- Các hàm cho PvP Thường (Matchmaking) ---
    fun subscribeQueueStatus(userId: Int, onStatusReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/queue-status/$userId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onStatusReceived(it.payload) }, { it.printStackTrace() })
    }

    fun subscribeMatchFound(userId: Int, onMatchFound: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/match/$userId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onMatchFound(it.payload) }, { it.printStackTrace() })
    }

    fun subscribeMatchRoom(matchId: Int, onRoomPayloadReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/match/$matchId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onRoomPayloadReceived(it.payload) }, { it.printStackTrace() })
    }

    fun sendJoinQueue(userId: Int) { 
        if (getStompClient().isConnected) {
            getStompClient().send("/app/find_match").subscribe() 
        } else {
            Log.e("PVP_WS", "Không thể Join Queue: WebSocket chưa kết nối!")
        }
    }
    
    fun sendReadyConfirm(matchId: Int) { 
        if (getStompClient().isConnected) {
            getStompClient().send("/app/join-queue", matchId.toString()).subscribe() 
        } else {
            Log.e("PVP_WS", "Không thể Sẵn sàng: WebSocket chưa kết nối!")
        }
    }
    
    fun sendLeaveQueue(userId: Int) { 
        if (getStompClient().isConnected) {
            getStompClient().send("/app/leave-queue", userId.toString()).subscribe() 
        } else {
            Log.e("PVP_WS", "Không thể rời Queue: WebSocket chưa kết nối!")
        }
    }

    // --- Các hàm cho PvP Friend (Invitation) ---
    fun subscribeIncomingInvite(userId: Int, onInviteReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/invite/$userId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onInviteReceived(it.payload) }, { it.printStackTrace() })
    }

    fun subscribeInviteResult(userId: Int, onResultReceived: (String) -> Unit): Disposable {
        return getStompClient().topic("/topic/invite-result/$userId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onResultReceived(it.payload) }, { it.printStackTrace() })
    }

    fun sendFriendInvite(jsonRequest: String): Disposable? {
        return if (getStompClient().isConnected) {
            getStompClient().send("/app/invite/send", jsonRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        } else {
            Log.e("PVP_WS", "Không thể gửi lời mời: WebSocket chưa kết nối!")
            null
        }
    }

    fun respondToInvite(jsonRequest: String): Disposable? {
        return if (getStompClient().isConnected) {
            getStompClient().send("/app/invite/respond", jsonRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        } else {
            Log.e("PVP_WS", "Không thể phản hồi lời mời: WebSocket chưa kết nối!")
            null
        }
    }

    fun forceDisconnect() {
        mStompClient?.disconnect()
        mStompClient = null
    }
}
