package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.NotificationAdapter
import com.example.appenggo.model.NotificationPayload
import com.example.appenggo.model.NotificationRepository
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        token = "Bearer ${
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("TOKEN", "")
        }"

        rvNotifications = findViewById(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        val adapter = NotificationAdapter(
            NotificationRepository.list.toMutableList(),
            onAccept = { notification ->
                // Xóa khỏi Repo để khi quay lại không bị hiện lại
                NotificationRepository.remove(notification)
                when (notification.type) {
                    "FRIEND_REQUEST" -> acceptFriendRequest(notification.requestId)
                    "PVP_INVITE"     -> acceptPvpInvite(notification)
                }
            },
            onDecline = { notification ->
                NotificationRepository.remove(notification)
                when (notification.type) {
                    "FRIEND_REQUEST" -> declineFriendRequest(notification.requestId)
                    "PVP_INVITE"     -> declinePvpInvite(notification.requestId)
                }
            }
        )
        rvNotifications.adapter = adapter
    }

    private fun acceptFriendRequest(requestId: Int?) {
        requestId ?: return
        lifecycleScope.launch {
            try {
                RetrofitClient.api.acceptFriendRequest(token, requestId)
                Toast.makeText(this@NotificationActivity, "Đã chấp nhận kết bạn", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@NotificationActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun declineFriendRequest(requestId: Int?) {
        requestId ?: return
        lifecycleScope.launch {
            try {
                RetrofitClient.api.rejectFriendRequest(token, requestId)
                Toast.makeText(this@NotificationActivity, "Đã từ chối", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@NotificationActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptPvpInvite(notification: NotificationPayload) {
        val matchId = notification.requestId ?: return
        lifecycleScope.launch {
            try {
                RetrofitClient.api.acceptDirectMatch(token, matchId)
                val intent = Intent(this@NotificationActivity, WaitingRoomActivity::class.java).apply {
                    putExtra(WaitingRoomActivity.EXTRA_MATCH_ID, matchId)
                    putExtra(WaitingRoomActivity.EXTRA_IS_PLAYER1, false)
                    putExtra(WaitingRoomActivity.EXTRA_OPPONENT_NAME, notification.fromUsername)
                    putExtra(WaitingRoomActivity.EXTRA_EXAM_TITLE, notification.examTitle ?: "PVP Quiz")
                    putExtra(WaitingRoomActivity.EXTRA_EXAM_TOPIC, notification.examTopic ?: "")
                    putExtra(WaitingRoomActivity.EXTRA_DIFFICULTY, notification.difficulty ?: "")
                    val qCountStr = notification.questionCount?.let { "${it} Câu" } ?: ""
                    putExtra(WaitingRoomActivity.EXTRA_QUESTION_COUNT, qCountStr)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@NotificationActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun declinePvpInvite(matchId: Int?) {
        matchId ?: return
        lifecycleScope.launch {
            try {
                RetrofitClient.api.declineDirectMatch(token, matchId)
                Toast.makeText(this@NotificationActivity, "Đã từ chối lời mời PvP", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@NotificationActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}