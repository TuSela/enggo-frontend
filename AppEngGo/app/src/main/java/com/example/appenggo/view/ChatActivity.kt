package com.example.appenggo.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.MessageAdapter
import com.example.appenggo.model.SendMessageRequest
import com.example.appenggo.websocket.WebSocketManager
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONVERSATION_ID = "conversation_id"
        const val EXTRA_FRIEND_NAME = "friend_name"
        const val EXTRA_FRIEND_AVATAR = "friend_avatar"
        const val EXTRA_FRIEND_ONLINE = "friend_online"
    }

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var tvFriendName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var dotOnline: View

    private lateinit var messageAdapter: MessageAdapter
    private var conversationId: Int = -1
    private lateinit var token: String
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Lấy dữ liệu từ Intent
        conversationId = intent.getIntExtra(EXTRA_CONVERSATION_ID, -1)
        val friendName = intent.getStringExtra(EXTRA_FRIEND_NAME) ?: ""
        val friendAvatar = intent.getStringExtra(EXTRA_FRIEND_AVATAR)
        val friendOnline = intent.getBooleanExtra(EXTRA_FRIEND_ONLINE, false)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        token = "Bearer ${prefs.getString("TOKEN", "")}"
        currentUsername = prefs.getString("USERNAME", "") ?: ""

        initViews()
        setupHeader(friendName, friendAvatar, friendOnline)
        setupRecyclerView()
        setupSendButton()
        loadMessages()
        subscribeChatMessages()
    }

    private fun initViews() {
        rvMessages = findViewById(R.id.rv_messages)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
        btnBack = findViewById(R.id.btn_back)
        tvFriendName = findViewById(R.id.tv_friend_name)
        tvStatus = findViewById(R.id.tv_status)
        ivAvatar = findViewById(R.id.iv_avatar)
        dotOnline = findViewById(R.id.dot_online)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupHeader(name: String, avatarUrl: String?, online: Boolean) {
        tvFriendName.text = name
        tvStatus.text = if (online) "Đang hoạt động" else "Offline"
        tvStatus.setTextColor(
            if (online) getColor(R.color.green) else getColor(R.color.gray)
        )
        dotOnline.visibility = if (online) View.VISIBLE else View.GONE

        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop()
                .placeholder(R.drawable.ic_default_avatar).into(ivAvatar)
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(currentUsername)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true  // Tin nhắn mới nhất ở dưới
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = messageAdapter
    }

    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isEmpty()) return@setOnClickListener

            // Gửi qua WebSocket (realtime)
            if (WebSocketManager.isConnected()) {
                val payload = mapOf(
                    "conversationId" to conversationId,
                    "content" to content,
                    "type" to "TEXT"
                )
                WebSocketManager.sendMessage(Gson().toJson(payload))
                etMessage.setText("")
            } else {
                // Fallback: gửi qua REST API nếu WebSocket mất kết nối
                sendMessageViaApi(content)
            }
        }
    }

    private fun sendMessageViaApi(content: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.sendMessage(
                    token, conversationId, SendMessageRequest(content)
                )
                if (res.code == 1000 && res.result != null) {
                    messageAdapter.addMessage(res.result)
                    etMessage.setText("")
                    scrollToBottom()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "Gửi thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getMessages(token, conversationId, page = 0, size = 50)
                if (res.code == 1000 && res.result != null) {
                    // API trả về mới nhất trước → đảo ngược để hiện đúng thứ tự
                    val messages = res.result.content.reversed()
                    messageAdapter.submitList(messages)
                    scrollToBottom()
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Load messages error: ${e.message}")
            }
        }
    }

    private fun subscribeChatMessages() {
        // Lắng nghe tin nhắn realtime trong conversation này
        WebSocketManager.onChatMessageReceived = { message ->
            if (message.conversationId == conversationId) {
                runOnUiThread {
                    messageAdapter.addMessage(message.toMessageResponse())
                    scrollToBottom()
                }
            }
        }
    }

    private fun scrollToBottom() {
        val count = messageAdapter.itemCount
        if (count > 0) rvMessages.scrollToPosition(count - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.onChatMessageReceived = null
    }
}