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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_matching)

        // 1. Lấy thông tin tài khoản của bạn
        myId = intent.getIntExtra("MY_ID", 0)

        // 2. Khởi tạo view TRƯỚC TIÊN — nếu không, mọi lateinit var ở dưới
        //    sẽ chưa được gán và app sẽ crash ngay khi WebSocket trả dữ liệu về.
        initViews()
        loadMyProfileData()

        // 3. Đăng ký toàn bộ callback xử lý sự kiện WebSocket TRƯỚC khi gửi
        //    lệnh tìm trận, để không bỏ lỡ sự kiện trả về sớm.
        setupWebSocketListener()
        WebSocketManager.subscribeToMatchFound(myId)

        // 4. SAU KHI ĐÃ ĐĂNG KÝ HẾT CÁC KÊNH, TIẾN HÀNH BẤM NÚT TÌM TRẬN
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
                WebSocketManager.sendJoinQueue(matchId)

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

        Glide.with(this).load(intent.getStringExtra("MY_AVATAR_URL")).circleCrop().placeholder(R.drawable.ic_person).into(ivMyAvatar)
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

        WebSocketManager.onPvpEventReceived = { eventMap ->
            runOnUiThread {
                try {
                    val statusInMap = eventMap["status"] as? String
                    if (statusInMap == "MATCH_TIMEOUT" || statusInMap == "CANCELLED") {
                        Toast.makeText(this, "Trận đấu đã bị hủy hoặc hết thời gian!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else if (eventMap.containsKey("examTitle") || eventMap.containsKey("questions")) {
                        val gson = Gson()
                        val rawJson = gson.toJson(eventMap)
                        val p1Id = (eventMap["player1Id"] as? Number)?.toInt() ?: 0
                        val isPlayer1 = (myId == p1Id)
                        val opponentName = if (isPlayer1) {
                            eventMap["player2Username"] as? String
                        } else {
                            eventMap["player1Username"] as? String
                        }

                        val intent = Intent(this, PvpQuizActivity::class.java).apply {
                            putExtra(PvpQuizActivity.EXTRA_EXAM_DATA, rawJson)
                            putExtra(PvpQuizActivity.EXTRA_MATCH_ID, currentMatchId ?: 0)
                            putExtra(PvpQuizActivity.EXTRA_IS_PLAYER1, isPlayer1)
                            putExtra(PvpQuizActivity.EXTRA_OPPONENT_NAME, opponentName)
                        }
                        startActivity(intent)
                        finish()
                    }
                    else if (eventMap.containsKey("id")) {
                        val matchId = (eventMap["id"] as? Number)?.toInt()
                        currentMatchId = matchId

                        matchId?.let { WebSocketManager.subscribeToMatchSession(it) }

                        val p1Id = (eventMap["player1Id"] as? Number)?.toInt() ?: 0

                        if (p1Id == myId) {
                            displayOpponentInfo(
                                name = eventMap["player2Username"] as? String,
                                elo = (eventMap["eloP2"] as? Number)?.toInt() ?: 0,
                                avatarUrl = eventMap["avatarUrlP2"] as? String,
                                level = (eventMap["levelP2"] as? Number)?.toInt() ?: 0
                            )
                        } else {
                            displayOpponentInfo(
                                name = eventMap["player1Username"] as? String,
                                elo = (eventMap["eloP1"] as? Number)?.toInt() ?: 0,
                                avatarUrl = eventMap["avatarUrlP1"] as? String,
                                level = (eventMap["levelP1"] as? Number)?.toInt() ?: 0
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PvpMatching", "Error parsing event: ${e.message}")
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
        Glide.with(this).load(avatarUrl).circleCrop().placeholder(R.drawable.ic_person).error(R.drawable.ic_person).into(ivEnemyAvatar)
        ivEnemyBadge.setImageResource(R.drawable.rank4)

        btnCancelMatch.visibility = View.GONE
        btnReadyMatch.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.clearPvpCallbacks()
    }
}