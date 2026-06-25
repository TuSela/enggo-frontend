package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.ExamPvpDisplayResponse
import com.example.appenggo.model.StartExamResponse
import com.example.appenggo.websocket.WebSocketManager
import com.google.gson.Gson

class PvpMatchingActivity : AppCompatActivity() {

    private lateinit var tvMatchStatusTop: TextView
    private lateinit var ivEnemyAvatar: ImageView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvEnemyLevel: TextView
    private lateinit var tvEnemyName: TextView
    private lateinit var lnEnemyBadgeContainer: LinearLayout
    private lateinit var ivEnemyBadge: ImageView
    private lateinit var tvEnemyElo: TextView

    private lateinit var btnCancelMatch: Button
    private lateinit var btnReadyMatch: Button

    private var myId: Int = 0
    private var currentMatchId: Int? = null
    private var isPlayer1: Boolean = false
    private var opponentName: String? = null

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_matching)

        myId = intent.getIntExtra("MY_ID", 0)

        initViews()
        loadMyProfileData()

        setupWebSocketListener()
        WebSocketManager.subscribeToMyQueueStatus()
        WebSocketManager.subscribeToMatchFound(myId)

        Log.d("PvpMatching", "🚀 Bắt đầu gửi lệnh tìm trận lên Server...")
        tvMatchStatusTop.text = "ĐANG TÌM ĐỐI THỦ..."
        pbLoading.visibility = View.VISIBLE
        WebSocketManager.sendFindMatch()
    }

    private fun initViews() {
        tvMatchStatusTop = findViewById(R.id.tv_match_status_top)
        ivEnemyAvatar = findViewById(R.id.iv_match_enemy_avatar)
        pbLoading = findViewById(R.id.pb_matching_loading)
        tvEnemyLevel = findViewById(R.id.tv_match_enemy_level)
        tvEnemyName = findViewById(R.id.tv_match_enemy_name)
        lnEnemyBadgeContainer = findViewById(R.id.ln_enemy_badge_container)
        ivEnemyBadge = findViewById(R.id.iv_match_enemy_badge)
        tvEnemyElo = findViewById(R.id.tv_match_enemy_elo)

        btnCancelMatch = findViewById(R.id.btn_cancel_match)
        btnReadyMatch = findViewById(R.id.btn_ready_match)

        btnReadyMatch.visibility = View.GONE

        btnCancelMatch.setOnClickListener {
            WebSocketManager.sendLeaveQueue(myId)
            finish()
        }

        btnReadyMatch.setOnClickListener {
            currentMatchId?.let { matchId ->
                // Áp dụng logic từ WaitingRoom: Join queue để sub các topic trận đấu
                WebSocketManager.joinPvpQueue(matchId)

                btnReadyMatch.isEnabled = false
                btnReadyMatch.text = "ĐANG CHỜ ĐỐI THỦ..."
                btnReadyMatch.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#9CA3AF")
                )
            }
        }
    }

    private fun loadMyProfileData() {
        val ivMyAvatar: ImageView = findViewById(R.id.iv_match_my_avatar)
        val tvMyLevel: TextView = findViewById(R.id.tv_match_my_level)
        val tvMyName: TextView = findViewById(R.id.tv_match_my_name)
        val ivMyBadge: ImageView = findViewById(R.id.iv_match_my_badge)
        val tvMyElo: TextView = findViewById(R.id.tv_match_my_elo)

        tvMyName.text = intent.getStringExtra("MY_NAME") ?: "Bạn"
        tvMyLevel.text = "LV. ${intent.getIntExtra("MY_LEVEL", 0)}"
        val myElo = intent.getIntExtra("MY_ELO", 0)
        val myBadgeDesc = intent.getStringExtra("MY_BADGE_DESC") ?: "Chưa xếp hạng"
        tvMyElo.text = "$myBadgeDesc - $myElo ELO"

        Glide.with(this).load(intent.getStringExtra("MY_AVATAR_URL")).circleCrop().placeholder(R.drawable.ic_default_avatar).into(ivMyAvatar)
        Glide.with(this).load(intent.getStringExtra("MY_BADGE_ICON_URL")).placeholder(R.drawable.rank4).into(ivMyBadge)
    }

    private fun setupWebSocketListener() {
        WebSocketManager.onQueueStatusReceived = { status ->
            runOnUiThread {
                when (status) {
                    "WAITING" -> {
                        tvMatchStatusTop.text = "ĐANG TÌM ĐỐI THỦ..."
                        pbLoading.visibility = View.VISIBLE
                    }
                    "WAITING_FOR_ENEMY_READY" -> {
                        tvMatchStatusTop.text = "ĐANG CHỜ ĐỐI THỦ SẴN SÀNG..."
                    }
                }
            }
        }

        // Lắng nghe thông tin trận đấu (Match Found)
        WebSocketManager.onPvpEventReceived = { eventMap ->
            runOnUiThread {
                try {
                    val statusInMap = eventMap["status"] as? String
                    if (statusInMap == "MATCH_TIMEOUT" || statusInMap == "CANCELLED") {
                        Toast.makeText(this, "Trận đấu đã bị hủy hoặc hết thời gian!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else {
                        // FIX: Kiểm tra cả "id" và "matchId" để lấy đúng ID trận đấu
                        val matchId = (eventMap["id"] as? Number)?.toInt() ?: (eventMap["matchId"] as? Number)?.toInt()

                        if (matchId != null) {
                            currentMatchId = matchId
                            WebSocketManager.subscribeToMatchSession(matchId)

                            val p1Id = (eventMap["player1Id"] as? Number)?.toInt() ?: 0
                            isPlayer1 = (myId == p1Id)

                            if (isPlayer1) {
                                opponentName = eventMap["player2Username"] as? String
                                displayOpponentInfo(
                                    name = opponentName,
                                    elo = (eventMap["eloP2"] as? Number)?.toInt() ?: 0,
                                    avatarUrl = eventMap["avatarUrlP2"] as? String,
                                    level = (eventMap["levelP2"] as? Number)?.toInt() ?: 0
                                )
                            } else {
                                opponentName = eventMap["player1Username"] as? String
                                displayOpponentInfo(
                                    name = opponentName,
                                    elo = (eventMap["eloP1"] as? Number)?.toInt() ?: 0,
                                    avatarUrl = eventMap["avatarUrlP1"] as? String,
                                    level = (eventMap["levelP1"] as? Number)?.toInt() ?: 0
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PvpMatching", "Error parsing event: ${e.message}")
                }
            }
        }

        // Lắng nghe đề thi (Chỉ nhận được sau khi cả 2 đã bấm Sẵn sàng/Join Queue)
        WebSocketManager.onPvpExamReceived = { rawData ->
            runOnUiThread {
                try {
                    val json = gson.toJson(rawData)
                    val pvpExam = gson.fromJson(json, ExamPvpDisplayResponse::class.java)

                    // Logic chuyển đổi sang StartExamResponse giống WaitingRoomActivity
                    val myAttemptId = if (isPlayer1) pvpExam.attemptId1 else pvpExam.attemptId2

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
                        putExtra(PvpQuizActivity.EXTRA_MATCH_ID,      currentMatchId ?: 0)
                        putExtra(PvpQuizActivity.EXTRA_IS_PLAYER1,    isPlayer1)
                        putExtra(PvpQuizActivity.EXTRA_OPPONENT_NAME, opponentName ?: "Đối thủ")
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("PvpMatching", "Lỗi xử lý đề thi: ${e.message}")
                    Toast.makeText(this, "Không thể tải đề thi PVP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayOpponentInfo(name: String?, elo: Int, avatarUrl: String?, level: Int) {
        tvMatchStatusTop.text = "ĐÃ TÌM THẤY ĐỐI THỦ!"
        tvMatchStatusTop.setTextColor(android.graphics.Color.parseColor("#10B981"))

        pbLoading.visibility = View.GONE
        tvEnemyLevel.visibility = View.VISIBLE
        lnEnemyBadgeContainer.visibility = View.VISIBLE

        tvEnemyName.text = name ?: "Đối thủ ẩn danh"
        tvEnemyName.setTypeface(null, android.graphics.Typeface.BOLD)
        tvEnemyLevel.text = "LV. $level"

        val computedRank = if (elo > 1000) "Cao Thủ" else "Hạng ${elo / 100}"
        tvEnemyElo.text = "$computedRank - $elo ELO"

        ivEnemyAvatar.setPadding(0, 0, 0, 0)
        Glide.with(this).load(avatarUrl).circleCrop().placeholder(R.drawable.ic_default_avatar).error(R.drawable.ic_default_avatar).into(ivEnemyAvatar)
        ivEnemyBadge.setImageResource(R.drawable.rank4)

        btnCancelMatch.visibility = View.GONE
        btnReadyMatch.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        // FIX: Chỉ xóa các callback cục bộ, không dùng clearPvpCallbacks() vì sẽ làm mất listener của QuizActivity
        WebSocketManager.onQueueStatusReceived = null
        WebSocketManager.onPvpEventReceived = null
        WebSocketManager.onPvpExamReceived = null
    }
}