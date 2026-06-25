package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.example.appenggo.adapter.RankingAdapter
import com.bumptech.glide.Glide
import com.example.appenggo.model.UserRank

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

    private lateinit var rvRanking: RecyclerView
    private lateinit var rankingAdapter: RankingAdapter

    // Views for Ranking Tab
    private lateinit var tvMyRankName: TextView
    private lateinit var tvMyBadgeName: TextView
    private lateinit var ivMyProfileAvatar: ImageView
    private lateinit var tvMyProfileLevel: TextView
    private lateinit var ivMyRankBadgeIcon: ImageView
    private lateinit var tvMyEloValue: TextView
    private lateinit var pbMyRankProgress: ProgressBar

    // Views for Invite Tab
    private lateinit var ivInviteMyAvatar: ImageView
    private lateinit var tvInviteMyLevel: TextView
    private lateinit var tvInviteMyName: TextView

    private var isRankingLoaded = false

    private lateinit var btnStartPvp: Button
    private lateinit var btnStartContainer: View

    private var currentMyRank: UserRank? = null

    private val rankNamesByPosition by lazy {
        arrayOf<TextView>(
            findViewById(R.id.rank_name_1),
            findViewById(R.id.rank_name_2),
            findViewById(R.id.rank_name_3),
            findViewById(R.id.rank_name_4)
        )
    }

    private val rankDescsByPosition by lazy {
        arrayOf<TextView>(
            findViewById(R.id.rank_desc_1),
            findViewById(R.id.rank_desc_2),
            findViewById(R.id.rank_desc_3),
            findViewById(R.id.rank_desc_4)
        )
    }

    private val rankPointsByPosition by lazy {
        arrayOf<TextView>(
            findViewById(R.id.rank_points_1),
            findViewById(R.id.rank_points_2),
            findViewById(R.id.rank_points_3),
            findViewById(R.id.rank_points_4)
        )
    }

    private val rankAvatarsByPosition by lazy {
        arrayOf<ImageView>(
            findViewById(R.id.rank_avt_1),
            findViewById(R.id.rank_avt_2),
            findViewById(R.id.rank_avt_3),
            findViewById(R.id.rank_avt_4)
        )
    }

    private val topicLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

        token = "Bearer ${getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("TOKEN", "")}"

        initViews()
        setupClickListeners()
        setupFriendList()
        setupRankingList()
        loadFriends()
        loadRankingData()
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
        rvRanking            = findViewById(R.id.rv_ranking)

        // Ranking Tab Views
        ivMyProfileAvatar   = findViewById(R.id.iv_my_profile_avatar)
        tvMyProfileLevel    = findViewById(R.id.tv_my_profile_level)
        tvMyRankName        = findViewById(R.id.tv_my_rank_name)
        tvMyBadgeName       = findViewById(R.id.tv_my_badge_name)
        ivMyRankBadgeIcon   = findViewById(R.id.iv_my_rank_badge_icon)
        tvMyEloValue        = findViewById(R.id.tv_my_elo_value)
        pbMyRankProgress    = findViewById(R.id.pb_my_rank_progress)

        // Invite Tab Views
        ivInviteMyAvatar    = findViewById(R.id.iv_invite_my_avatar)
        tvInviteMyLevel     = findViewById(R.id.tv_invite_my_level)
        tvInviteMyName      = findViewById(R.id.tv_invite_my_name)

        btnStartPvp = findViewById(R.id.btn_start_pvp)
        btnStartContainer = findViewById(R.id.btn_start_container)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupRankingList() {
        rankingAdapter = RankingAdapter()
        rvRanking.layoutManager = LinearLayoutManager(this)
        rvRanking.adapter = rankingAdapter
        rvRanking.isNestedScrollingEnabled = false
    }

    private fun loadRankingData() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getTopElo(token)
                if (res.code == 1000 && res.result != null) {
                    val rankingResult = res.result
                    val topUsers = rankingResult.topUsers ?: emptyList()

                    rankingResult.myRank?.let { myRank ->
                        currentMyRank = myRank
                        
                        // Update UI Ranking Tab
                        tvMyRankName.text = myRank.username
                        tvMyProfileLevel.text = "LV. ${myRank.level}"
                        tvMyBadgeName.text = myRank.badgeRank?.description ?: "Chưa xếp hạng"

                        val currentElo = myRank.elo
                        if (currentElo > 1000) {
                            tvMyEloValue.text = "$currentElo"
                            pbMyRankProgress.progress = 100
                        } else {
                            tvMyEloValue.text = "${currentElo % 100}/100"
                            pbMyRankProgress.progress = currentElo % 100
                        }

                        Glide.with(this@PvpActivity)
                            .load(myRank.avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(ivMyProfileAvatar)

                        Glide.with(this@PvpActivity).load(myRank.badgeRank?.iconUrl).placeholder(R.drawable.rank4).into(ivMyRankBadgeIcon)

                        // Update UI Invite Tab (Fix for the issue)
                        tvInviteMyName.text = myRank.username
                        tvInviteMyLevel.text = "LV ${myRank.level}"
                        Glide.with(this@PvpActivity)
                            .load(myRank.avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(ivInviteMyAvatar)
                    }

                    for (i in 0 until 4) {
                        if (i < topUsers.size) {
                            val user = topUsers[i]
                            rankNamesByPosition[i].text = user.username
                            rankDescsByPosition[i].text = user.badgeRank?.description ?: "Chưa xếp hạng"
                            rankPointsByPosition[i].text = String.format("%,d", user.elo)
                            
                            Glide.with(this@PvpActivity)
                                .load(user.avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(rankAvatarsByPosition[i])
                        }
                    }
                    isRankingLoaded = true
                }
            } catch (e: Exception) {}
        }
    }

    private fun setupFriendList() {
        friendPvpAdapter = FriendPvpAdapter { friend ->
            inviteFriend(friend.userId, friend.username)
        }
        rvFriendsPvp.layoutManager = LinearLayoutManager(this)
        rvFriendsPvp.adapter = friendPvpAdapter
        rvFriendsPvp.isNestedScrollingEnabled = false
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getAllFriends(token)
                if (res.code == 1000) {
                    val sorted = (res.result ?: emptyList())
                        .sortedByDescending { it.online }
                    friendPvpAdapter.submitList(sorted)
                }
            } catch (e: Exception) {
                Toast.makeText(this@PvpActivity, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                        WebSocketManager.onPvpEventReceived = null
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

        btnStartPvp.setOnClickListener {
            currentMyRank?.let { myRank ->
                val intent = Intent(this@PvpActivity, PvpMatchingActivity::class.java).apply {
                    putExtra("MY_ID", myRank.id)
                    putExtra("MY_NAME", myRank.username)
                    putExtra("MY_LEVEL", myRank.level)
                    putExtra("MY_AVATAR_URL", myRank.avatarUrl)
                    putExtra("MY_ELO", myRank.elo)
                    putExtra("MY_BADGE_DESC", myRank.badgeRank?.description)
                    putExtra("MY_BADGE_ICON_URL", myRank.badgeRank?.iconUrl)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Vui lòng đợi tải dữ liệu...", Toast.LENGTH_SHORT).show()
                loadRankingData()
            }
        }
    }

    private fun showRankingTab() {
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility  = View.GONE
        btnStartContainer.visibility = View.VISIBLE
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabInvite.background = null
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
        if (!isRankingLoaded) loadRankingData()
    }

    private fun showInviteTab() {
        layoutInviteContent.visibility  = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        btnStartContainer.visibility = View.GONE
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabRanking.background = null
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun openTopicSelection() {
        val intent = Intent(this, ThemeSelectionActivity::class.java).apply { putExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, currentTopicId) }
        topicLauncher.launch(intent)
    }

    private fun openQuestionCountSelection() {
        QuestionCountBottomSheet.newInstance(currentQuestionCount).apply {
            onCountSelected = { count -> currentQuestionCount = count; tvQuestionCountValue.text = "$count Câu" }
        }.show(supportFragmentManager, QuestionCountBottomSheet.TAG)
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

    private fun updateSettingCardSelection(selected: LinearLayout) {
        val sel = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card_selected)
        val nor = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card)
        cardTopic.background = if (selected == cardTopic) sel else nor
        cardDifficulty.background = if (selected == cardDifficulty) sel else nor
        cardQuestionCount.background = if (selected == cardQuestionCount) sel else nor
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.onPvpEventReceived = null
    }
}