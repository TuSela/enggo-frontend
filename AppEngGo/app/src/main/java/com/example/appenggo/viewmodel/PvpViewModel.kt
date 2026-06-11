package com.example.appenggo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.Request.InviteRequest
import com.example.appenggo.model.Request.InviteRespondRequest
import com.example.appenggo.model.Response.InviteResponse
import com.example.appenggo.model.Response.UserResponse
import com.example.appenggo.repository.PvpRepository
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch
import org.json.JSONObject

class PvpViewModel : ViewModel() {

    companion object {
        @Volatile
        var lastStartedMatchId: Int = -1

        @Volatile
        var isPvpActivityActive: Boolean = false // Đánh dấu PvpActivity có đang hiển thị không

        // Dữ liệu PvP dùng chung toàn ứng dụng (Static)
        private val _matchResultJson = MutableLiveData<String?>()
        val matchResultJson: LiveData<String?> = _matchResultJson

        private val _pvpQuizJson = MutableLiveData<String?>()
        val pvpQuizJson: LiveData<String?> = _pvpQuizJson

        private val _incomingInvite = MutableLiveData<InviteResponse?>()
        val incomingInvite: LiveData<InviteResponse?> = _incomingInvite

        private val _inviteResult = MutableLiveData<String>()
        val inviteResult: LiveData<String> = _inviteResult

        // Quản lý Disposable tĩnh
        private var matchFoundDisposable: Disposable? = null
        private var inviteDisposable: Disposable? = null
        private var inviteResultDisposable: Disposable? = null
        private var matchRoomDisposable: Disposable? = null
        
        private val globalCompositeDisposable = CompositeDisposable()

        // Hàm kiểm tra và "tiêu thụ" trận đấu, đảm bảo chỉ mở Activity 1 lần
        @Synchronized
        fun tryConsumeMatch(matchId: Int): Boolean {
            if (matchId == -1 || matchId == lastStartedMatchId) return false
            lastStartedMatchId = matchId
            return true
        }
    }

    private val pvpRepository = PvpRepository()
    private val gson = Gson()
    private val apiService = RetrofitClient.api

    // Expose static LiveData through instance for easier access in Activities
    val matchResultJson: LiveData<String?> get() = Companion.matchResultJson
    val pvpQuizJson: LiveData<String?> get() = Companion.pvpQuizJson
    val incomingInvite: LiveData<InviteResponse?> get() = Companion.incomingInvite
    val inviteResult: LiveData<String> get() = Companion.inviteResult
    
    private val instanceCompositeDisposable = CompositeDisposable()
    private var queueStatusDisposable: Disposable? = null

    private val _connectionState = MutableLiveData<Boolean>()
    val connectionState: LiveData<Boolean> = _connectionState

    private val _queueStatus = MutableLiveData<String>()
    val queueStatus: LiveData<String> = _queueStatus

    private val _friendList = MutableLiveData<List<UserResponse>>()
    val friendList: LiveData<List<UserResponse>> = _friendList

    fun startPvpSession(token: String) {
        pvpRepository.connectWebSocket(
            token = token,
            onConnected = { _connectionState.postValue(true) },
            onError = { throwable ->
                _connectionState.postValue(false)
                Log.e("PVP_WS", "Lỗi WebSocket: ${throwable.message}")
            }
        )?.let { instanceCompositeDisposable.add(it) }
    }

    private fun subscribeMatchFoundInternal(userId: Int) {
        // Chỉ subscribe 1 lần duy nhất cho 1 Topic
        if (matchFoundDisposable != null && !matchFoundDisposable!!.isDisposed) return
        
        matchFoundDisposable = pvpRepository.subscribeMatchFound(userId) { matchJson ->
            Log.d("PVP_VM", "MatchFound Received (Global Topic)")
            _matchResultJson.postValue(matchJson)
            try {
                val matchObj = JSONObject(matchJson)
                val matchId = if (matchObj.has("matchId")) matchObj.getInt("matchId") else matchObj.getInt("id")
                subscribeMatchRoom(matchId)
            } catch (e: Exception) {
                Log.e("PVP_VM", "Error parsing MatchFound: ${e.message}")
            }
        }
        matchFoundDisposable?.let { globalCompositeDisposable.add(it) }
    }

    fun clickFindMatch(userId: Int) {
        queueStatusDisposable?.dispose()
        queueStatusDisposable = pvpRepository.subscribeQueueStatus(userId) { status ->
            _queueStatus.postValue(status)
        }
        queueStatusDisposable?.let { instanceCompositeDisposable.add(it) }

        subscribeMatchFoundInternal(userId)
        pvpRepository.sendJoinQueue(userId)
    }

    fun subscribeToFriendInvites(userId: Int) {
        if (inviteDisposable == null || inviteDisposable!!.isDisposed) {
            inviteDisposable = pvpRepository.subscribeIncomingInvite(userId) { payload ->
                try {
                    val invite = gson.fromJson(payload, InviteResponse::class.java)
                    _incomingInvite.postValue(invite)
                } catch (e: Exception) {
                    Log.e("PVP_VM", "Invite Error: ${e.message}")
                }
            }
            inviteDisposable?.let { globalCompositeDisposable.add(it) }
        }

        if (inviteResultDisposable == null || inviteResultDisposable!!.isDisposed) {
            inviteResultDisposable = pvpRepository.subscribeInviteResult(userId) { result ->
                _inviteResult.postValue(result)
            }
            inviteResultDisposable?.let { globalCompositeDisposable.add(it) }
        }

        subscribeMatchFoundInternal(userId)
    }

    fun sendInvite(inviteeUsername: String) {
        val request = InviteRequest(inviteeUsername)
        pvpRepository.sendFriendInvite(gson.toJson(request))
    }

    fun respondToInvite(inviteId: Int, accepted: Boolean) {
        val request = InviteRespondRequest(inviteId, accepted)
        pvpRepository.respondToInvite(gson.toJson(request))
        // Xóa lời mời sau khi đã phản hồi để không bị hiện lại khi quay lại màn hình
        _incomingInvite.postValue(null)
    }

    fun loadFriendList(token: String) {
        viewModelScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.getFriends(formattedToken)
                if (response.code == 1000) {
                    _friendList.postValue(response.result ?: emptyList())
                } else {
                    _friendList.postValue(emptyList())
                }
            } catch (e: Exception) {
                _friendList.postValue(emptyList())
            }
        }
    }

    private fun subscribeMatchRoom(matchId: Int) {
        matchRoomDisposable?.dispose()
        matchRoomDisposable = pvpRepository.subscribeMatchRoom(matchId) { roomPayload ->
            Log.d("PVP_VM", "Quiz Data Received for Match: $matchId")
            _pvpQuizJson.postValue(roomPayload)
        }
        matchRoomDisposable?.let { globalCompositeDisposable.add(it) }
    }

    fun clearPvpQuiz() {
        Log.d("PVP_VM", "Clearing Global PvP Data")
        _pvpQuizJson.postValue(null)
        _matchResultJson.postValue(null)
        _incomingInvite.postValue(null) // Xóa cả lời mời đang chờ
    }

    fun sendReadyStatus(matchId: Int) {
        _queueStatus.postValue("WAITING_FOR_ENEMY_READY")
        pvpRepository.sendReadyConfirm(matchId)
    }

    fun clickCancelMatch(userId: Int) {
        pvpRepository.sendLeaveQueue(userId)
    }

    override fun onCleared() {
        super.onCleared()
        instanceCompositeDisposable.clear()
        Log.d("PVP_VM", "Instance Disposed.")
    }
}
