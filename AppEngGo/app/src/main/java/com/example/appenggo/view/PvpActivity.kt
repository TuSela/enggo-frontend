package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.FriendPvpAdapter
import com.example.appenggo.model.RandomBlueprintRequest
import com.example.appenggo.websocket.WebSocketManager
import kotlinx.coroutines.launch
import com.example.appenggo.adapter.FriendAdapter

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: ScrollView
    private lateinit var layoutInviteContent: ScrollView

    private lateinit var cardTopic: LinearLayout
    private lateinit var tvTopicValue: TextView
    private lateinit var cardDifficulty: LinearLayout
    private lateinit var cardQuestionCount: LinearLayout
    private lateinit var tvQuestionCountValue: TextView
    private lateinit var tvDifficultyValue: TextView
    private lateinit var ivDifficultyIcon: ImageView
    private lateinit var rvFriendsPvp: RecyclerView
    private lateinit var friendPvpAdapter: FriendPvpAdapter

    private var currentTopicId: Int = 1
    private var currentTopicName: String = "Gia đình"
    private var currentDifficulty: DifficultyBottomSheet.Difficulty = DifficultyBottomSheet.Difficulty.MEDIUM
    private var currentQuestionCount: Int = 10
    private lateinit var token: String

    private val topicLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val themeId = result.data?.getIntExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, -1) ?: -1
            val themeName = result.data?.getStringExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_NAME)
            if (themeId != -1 && themeName != null) {
                currentTopicId = themeId
                currentTopicName = themeName
                tvTopicValue.text = themeName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp)

        token = "Bearer ${
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("TOKEN", "")
        }"

        initViews()
        setupClickListeners()
        setupFriendList()
        loadFriends()
        listenPvpInviteResponse()
        listenOnlineStatus()

        tvTopicValue.text = currentTopicName
        tvDifficultyValue.text = currentDifficulty.label
        updateSettingCardSelection(cardDifficulty)
    }

    private fun initViews() {
        tabRanking           = findViewById(R.id.tab_ranking)
        tabInvite            = findViewById(R.id.tab_invite)
        layoutRankingContent = findViewById(R.id.layout_ranking_content)
        layoutInviteContent  = findViewById(R.id.layout_invite_content)
        cardTopic            = findViewById(R.id.card_topic)
        tvTopicValue         = findViewById(R.id.tv_topic_value)
        cardDifficulty       = findViewById(R.id.card_difficulty)
        tvDifficultyValue    = findViewById(R.id.tv_difficulty_value)
        ivDifficultyIcon     = findViewById(R.id.iv_difficulty_icon)
        cardQuestionCount    = findViewById(R.id.card_question_count)
        tvQuestionCountValue = findViewById(R.id.tv_question_count_value)
        rvFriendsPvp         = findViewById(R.id.rv_friends_pvp)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupFriendList() {
        friendPvpAdapter = FriendPvpAdapter { friend ->
            // Bấm MỜI → gửi lời mời PVP
            inviteFriend(friend.userId, friend.username)
        }
        rvFriendsPvp.layoutManager = LinearLayoutManager(this)
        rvFriendsPvp.adapter = friendPvpAdapter
        rvFriendsPvp.isNestedScrollingEnabled = false
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                // Lấy tất cả bạn bè (ưu tiên online lên trên)
                val res = RetrofitClient.api.getAllFriends(token)
                if (res.code == 1000) {
                    val sorted = (res.result ?: emptyList())
                        .sortedByDescending { it.online } // Online trước
                    friendPvpAdapter.submitList(sorted)
                }
            } catch (e: Exception) {
                Toast.makeText(this@PvpActivity, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lắng nghe thay đổi trạng thái online/offline từ WebSocket và cập nhật UI
    private fun listenOnlineStatus() {
        WebSocketManager.onStatusChanged = { userId, status ->
            runOnUiThread {
                val isOnline = status == "ONLINE"
                friendPvpAdapter.updateOnlineStatus(userId, isOnline)
            }
        }
    }


    private fun inviteFriend(friendId: Int, friendUsername: String) {
        lifecycleScope.launch {
            try {
                val request = RandomBlueprintRequest(
                    difficulty = currentDifficulty.value,
                    themeIds = listOf(currentTopicId),
                    totalQuestions = currentQuestionCount
                )
                val res = RetrofitClient.api.inviteFriendPvp(token, friendId, request)
                if (res.code == 1000 && res.result != null) {
                    Toast.makeText(
                        this@PvpActivity,
                        "Đã gửi lời mời tới $friendUsername!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@PvpActivity, "Gửi lời mời thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PvpActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lắng nghe bạn chấp nhận lời mời → mở WaitingRoomActivity
    private fun listenPvpInviteResponse() {
        WebSocketManager.onPvpEventReceived = { event ->
            runOnUiThread {
                when (event["type"] as? String) {
                    "PVP_ACCEPTED" -> {
                        val matchId = (event["matchId"] as? Double)?.toInt() ?: return@runOnUiThread
                        val fromUsername = event["fromUsername"] as? String ?: ""
                        Toast.makeText(this, "$fromUsername đã chấp nhận!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, WaitingRoomActivity::class.java).apply {
                            putExtra(WaitingRoomActivity.EXTRA_MATCH_ID, matchId)
                            putExtra(WaitingRoomActivity.EXTRA_IS_PLAYER1, true)
                            putExtra(WaitingRoomActivity.EXTRA_OPPONENT_NAME, fromUsername)
                            putExtra(WaitingRoomActivity.EXTRA_EXAM_TOPIC, currentTopicName)
                            putExtra(WaitingRoomActivity.EXTRA_DIFFICULTY, currentDifficulty.label)
                            putExtra(WaitingRoomActivity.EXTRA_QUESTION_COUNT, "$currentQuestionCount Câu")
                        }
                        startActivity(intent)
                        // ← KHÔNG finish() PvpActivity, nhưng clear callback
                        WebSocketManager.onPvpEventReceived = null // ← thêm dòng này
                    }
                    "PVP_DECLINED" -> {
                        val msg = event["message"] as? String ?: "Lời mời bị từ chối"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        tabRanking.setOnClickListener { showRankingTab() }
        tabInvite.setOnClickListener  { showInviteTab() }
        cardTopic.setOnClickListener  { updateSettingCardSelection(cardTopic); openTopicSelection() }
        cardDifficulty.setOnClickListener { updateSettingCardSelection(cardDifficulty); openDifficultySelection() }
        cardQuestionCount.setOnClickListener { updateSettingCardSelection(cardQuestionCount); openQuestionCountSelection() }
    }

    private fun showRankingTab() {
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility  = View.GONE
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabInvite.background = null
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun showInviteTab() {
        layoutInviteContent.visibility  = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabRanking.background = null
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun openTopicSelection() {
        val intent = Intent(this, ThemeSelectionActivity::class.java).apply {
            putExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, currentTopicId)
        }
        topicLauncher.launch(intent)
    }

    private fun openQuestionCountSelection() {
        val sheet = QuestionCountBottomSheet.newInstance(currentQuestionCount)
        sheet.onCountSelected = { count ->
            currentQuestionCount = count
            tvQuestionCountValue.text = "$count Câu"
        }
        sheet.show(supportFragmentManager, QuestionCountBottomSheet.TAG)
    }

    private fun updateSettingCardSelection(selected: LinearLayout) {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card_selected)
        val normalBg   = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card)
        cardTopic.background         = if (selected == cardTopic)         selectedBg else normalBg
        cardDifficulty.background    = if (selected == cardDifficulty)    selectedBg else normalBg
        cardQuestionCount.background = if (selected == cardQuestionCount) selectedBg else normalBg
    }

    private fun openDifficultySelection() {
        val bottomSheet = DifficultyBottomSheet.newInstance(currentDifficulty)
        bottomSheet.onDifficultySelected = { difficulty ->
            currentDifficulty = difficulty
            tvDifficultyValue.text = difficulty.label
            val iconRes = when (difficulty) {
                DifficultyBottomSheet.Difficulty.EASY   -> R.drawable.ic_easy
                DifficultyBottomSheet.Difficulty.MEDIUM -> R.drawable.ic_medium
                DifficultyBottomSheet.Difficulty.HARD   -> R.drawable.ic_hard
            }
            ivDifficultyIcon.setImageResource(iconRes)
        }
        bottomSheet.show(supportFragmentManager, DifficultyBottomSheet.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.onPvpEventReceived = null
    }
}