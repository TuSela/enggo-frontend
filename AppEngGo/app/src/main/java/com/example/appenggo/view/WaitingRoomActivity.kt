package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.ExamPvpDisplayResponse
import com.example.appenggo.model.ExamQuestionWrapper
import com.example.appenggo.model.StartExamResponse
import com.example.appenggo.websocket.WebSocketManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class WaitingRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MATCH_ID       = "match_id"
        const val EXTRA_IS_PLAYER1     = "is_player1"
        const val EXTRA_EXAM_TITLE     = "exam_title"
        const val EXTRA_OPPONENT_NAME  = "opponent_name"
        const val EXTRA_OPPONENT_AVATAR= "opponent_avatar"
        const val EXTRA_EXAM_TOPIC     = "exam_topic"
        const val EXTRA_DIFFICULTY     = "exam_difficulty"
        const val EXTRA_QUESTION_COUNT = "exam_question_count"
    }

    private lateinit var ivAvatarP1: ImageView
    private lateinit var ivAvatarP2: ImageView
    private lateinit var tvNameP1: TextView
    private lateinit var tvNameP2: TextView
    private lateinit var tvReadyP1: TextView
    private lateinit var tvReadyP2: TextView
    private lateinit var tvExamTitle: TextView
    private lateinit var btnReadyOrStart: Button
    private lateinit var btnCancel: TextView
    private lateinit var tvExamTopic: TextView
    private lateinit var tvExamDifficulty: TextView
    private lateinit var tvExamQuestionCount: TextView

    private var matchId: Int = -1
    private var isPlayer1: Boolean = false
    private var opponentReady = false
    private lateinit var token: String
    private lateinit var currentUsername: String

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        matchId    = intent.getIntExtra(EXTRA_MATCH_ID, -1)
        isPlayer1  = intent.getBooleanExtra(EXTRA_IS_PLAYER1, false)
        val opponentName   = intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: ""
        val opponentAvatar = intent.getStringExtra(EXTRA_OPPONENT_AVATAR)
        val examTopic      = intent.getStringExtra(EXTRA_EXAM_TOPIC) ?: ""
        val difficulty     = intent.getStringExtra(EXTRA_DIFFICULTY) ?: ""
        val questionCount  = intent.getStringExtra(EXTRA_QUESTION_COUNT) ?: ""

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        token           = "Bearer ${prefs.getString("TOKEN", "")}"
        currentUsername = prefs.getString("USERNAME", "") ?: ""

        initViews()
        setupUI(opponentName, opponentAvatar, examTopic, difficulty, questionCount)
        setupButtons()
        listenPvpEvents()
    }

    private fun initViews() {
        ivAvatarP1           = findViewById(R.id.iv_avatar_p1)
        ivAvatarP2           = findViewById(R.id.iv_avatar_p2)
        tvNameP1             = findViewById(R.id.tv_name_p1)
        tvNameP2             = findViewById(R.id.tv_name_p2)
        tvReadyP1            = findViewById(R.id.tv_ready_p1)
        tvReadyP2            = findViewById(R.id.tv_ready_p2)
        tvExamTitle          = findViewById(R.id.tv_exam_title)
        tvExamTopic          = findViewById(R.id.tv_exam_topic)
        tvExamDifficulty     = findViewById(R.id.tv_exam_difficulty)
        tvExamQuestionCount  = findViewById(R.id.tv_exam_question_count)
        btnReadyOrStart      = findViewById(R.id.btn_ready_or_start)
        btnCancel            = findViewById(R.id.btn_cancel)
    }

    private fun setupUI(
        opponentName: String,
        opponentAvatar: String?,
        examTopic: String,
        difficulty: String,
        questionCount: String
    ) {
        tvExamTitle.visibility = android.view.View.GONE
        tvExamTopic.text          = "Chủ đề: $examTopic"
        tvExamDifficulty.text     = "Độ khó: $difficulty"
        tvExamQuestionCount.text  = "Số câu: $questionCount"

        if (isPlayer1) {
            tvNameP1.text = currentUsername
            tvNameP2.text = opponentName
            btnReadyOrStart.text = "BẮT ĐẦU"
            btnReadyOrStart.isEnabled = false
            btnReadyOrStart.alpha = 0.5f
        } else {
            tvNameP1.text = opponentName
            tvNameP2.text = currentUsername
            btnReadyOrStart.text = "SẴN SÀNG"
        }

        if (!opponentAvatar.isNullOrEmpty()) {
            val avatarView = if (isPlayer1) ivAvatarP2 else ivAvatarP1
            Glide.with(this).load(opponentAvatar).circleCrop().into(avatarView)
        }
    }

    private fun setupButtons() {
        btnReadyOrStart.setOnClickListener {
            if (isPlayer1) {
                startMatch()
            } else {
                playerReady()
                btnReadyOrStart.isEnabled = false
                btnReadyOrStart.text = "Đã sẵn sàng ✓"
                tvReadyP2.text = "✅ Sẵn sàng"
                tvReadyP2.setTextColor(getColor(R.color.green))
            }
        }

        btnCancel.setOnClickListener {
            lifecycleScope.launch {
                try { RetrofitClient.api.declineDirectMatch(token, matchId) } catch (e: Exception) {}
                finish()
            }
        }
    }

    private fun playerReady() {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.playerReady(token, matchId)
            } catch (e: Exception) {
                Toast.makeText(this@WaitingRoomActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMatch() {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.startDirectMatch(token, matchId)
            } catch (e: Exception) {
                Toast.makeText(this@WaitingRoomActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenPvpEvents() {
        WebSocketManager.onPvpEventReceived = { event ->
            runOnUiThread {
                when (event["type"] as? String) {

                    "PVP_PLAYER_READY" -> {
                        if (isPlayer1) {
                            opponentReady = true
                            tvReadyP2.text = "✅ Sẵn sàng"
                            tvReadyP2.setTextColor(getColor(R.color.green))
                            btnReadyOrStart.isEnabled = true
                            btnReadyOrStart.alpha = 1f
                            Toast.makeText(this, "Đối thủ đã sẵn sàng!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    "PVP_START" -> {
                        val mid = (event["matchId"] as? Double)?.toInt() ?: matchId
                        openQuiz(mid)
                    }

                    "PVP_DECLINED" -> {
                        Toast.makeText(this, "Đối thủ đã huỷ trận đấu", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun openQuiz(matchId: Int) {
        // Join queue → server gửi ExamPvpDisplayResponse qua /topic/match/{matchId}
        WebSocketManager.joinPvpQueue(matchId)

        WebSocketManager.onPvpExamReceived = { rawData ->
            runOnUiThread {
                try {
                    // Parse ExamPvpDisplayResponse
                    val json = gson.toJson(rawData)
                    val pvpExam = gson.fromJson(json, ExamPvpDisplayResponse::class.java)

                    // Xác định attemptId của mình: player1 dùng attemptId1, player2 dùng attemptId2
                    val myAttemptId = if (isPlayer1) pvpExam.attemptId1 else pvpExam.attemptId2

                    // Convert sang StartExamResponse để QuizActivity dùng lại
                    val startExamResponse = StartExamResponse(
                        examId          = pvpExam.examId,
                        attemptId       = myAttemptId,
                        title           = pvpExam.title,
                        difficulty      = pvpExam.difficulty ?: 2,
                        durationMinutes = pvpExam.durationMinutes,
                        totalQuestions  = pvpExam.totalQuestions,
                        examType        = pvpExam.examType ?: "PVP",
                        questions       = pvpExam.questions
                    )

                    val intent = Intent(this, PvpQuizActivity::class.java).apply {
                        putExtra(PvpQuizActivity.EXTRA_EXAM_DATA,     gson.toJson(startExamResponse))
                        putExtra(PvpQuizActivity.EXTRA_MATCH_ID,      matchId)
                        putExtra(PvpQuizActivity.EXTRA_IS_PLAYER1,    isPlayer1)
                        putExtra(PvpQuizActivity.EXTRA_OPPONENT_NAME, intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: "")
                    }
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi tải đề thi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.onPvpEventReceived = null
        WebSocketManager.onPvpExamReceived  = null
    }
}