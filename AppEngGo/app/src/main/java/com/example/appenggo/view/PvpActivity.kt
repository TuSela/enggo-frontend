package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.Resource
import com.example.appenggo.adapter.FriendAdapter
import com.example.appenggo.adapter.SelectThemeAdapter
import com.example.appenggo.model.Request.RandomBlueprintRequest
import com.example.appenggo.model.Response.InviteResponse
import com.example.appenggo.model.Response.UserResponse
import com.example.appenggo.viewmodel.PvpViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: LinearLayout
    private lateinit var layoutInviteContent: View

    private lateinit var btnEnter: Button // Ranking Find Match
    private lateinit var btnStartPvpFriend: Button // Invite Friend Start
    private lateinit var etSearchFriends: EditText

    private lateinit var pvpViewModel: PvpViewModel
    private var matchmakingDialog: AlertDialog? = null
    private var waitingInviteDialog: AlertDialog? = null

    // Friend PvP Views
    private lateinit var rvFriends: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var tvMyUsernameInvite: TextView
    private lateinit var tvNoFriends: TextView
    private lateinit var viewOpponentPlaceholder: View
    
    // Config Display Views
    private lateinit var tvSelectedTheme: TextView
    private lateinit var tvSelectedDifficulty: TextView
    private lateinit var tvSelectedQuestions: TextView
    
    // Current Configuration
    private var selectedThemeIds = mutableListOf<Int>()
    private var selectedThemeName: String = "Từ vựng"
    private var selectedDifficulty: Int = 2 // Default Medium
    private var selectedQuestions: Int = 10
    private var selectedFriend: UserResponse? = null

    private var fullFriendList: List<UserResponse> = emptyList()
    private var currentUserId: Int = -1
    private var token: String = ""
    private var currentMatchId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pvp)

        initViews()
        initViewModel()
        setupClickListeners()
        observeViewModelData()
    }

    override fun onResume() {
        super.onResume()
        PvpViewModel.isPvpActivityActive = true
    }

    override fun onPause() {
        super.onPause()
        PvpViewModel.isPvpActivityActive = false
    }

    private fun initViews() {
        tabRanking = findViewById(R.id.tab_ranking)
        tabInvite = findViewById(R.id.tab_invite)
        layoutRankingContent = findViewById(R.id.layout_ranking_content)
        layoutInviteContent = findViewById(R.id.layout_invite_content)
        
        btnEnter = findViewById(R.id.btn_enter)
        btnStartPvpFriend = findViewById(R.id.btn_start_pvp_friend)
        etSearchFriends = findViewById(R.id.et_search_friends)

        rvFriends = findViewById(R.id.rv_friends_online)
        tvMyUsernameInvite = findViewById(R.id.tv_my_username_invite)
        tvNoFriends = findViewById(R.id.tv_no_friends)
        viewOpponentPlaceholder = findViewById(R.id.view_opponent_placeholder)
        
        tvSelectedTheme = findViewById(R.id.tv_selected_theme)
        tvSelectedDifficulty = findViewById(R.id.tv_selected_difficulty)
        tvSelectedQuestions = findViewById(R.id.tv_selected_questions)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        friendAdapter = FriendAdapter(emptyList()) { friend ->
            selectedFriend = friend
            Toast.makeText(this, "Đã chọn ${friend.username}", Toast.LENGTH_SHORT).show()
        }
        rvFriends.adapter = friendAdapter
        rvFriends.layoutManager = LinearLayoutManager(this)
        
        setupSearch()
        updateConfigDisplay()
    }

    private fun setupSearch() {
        etSearchFriends.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFriends(query: String) {
        val filtered = if (query.isEmpty()) {
            fullFriendList
        } else {
            fullFriendList.filter { it.username.contains(query, ignoreCase = true) }
        }
        friendAdapter.updateData(filtered)
        tvNoFriends.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun initViewModel() {
        pvpViewModel = ViewModelProvider(this)[PvpViewModel::class.java]

        token = intent.getStringExtra("JWT_TOKEN") ?: ""
        currentUserId = intent.getIntExtra("USER_ID", -1)
        val username = intent.getStringExtra("USERNAME") ?: "User"

        findViewById<TextView>(R.id.tv_username_ranking).text = username
        tvMyUsernameInvite.text = username

        if (token.isEmpty() || currentUserId == -1) {
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            token = sharedPref.getString("TOKEN", "") ?: ""
            currentUserId = sharedPref.getInt("USER_ID", -1)
        }

        if (token.isEmpty() || currentUserId == -1) {
            Toast.makeText(this, "Lỗi xác thực dữ liệu!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pvpViewModel.startPvpSession(token)
        pvpViewModel.subscribeToFriendInvites(currentUserId)
        pvpViewModel.loadAllThemes(token)
    }

    private fun setupClickListeners() {
        tabRanking.setOnClickListener { showRankingTab() }
        tabInvite.setOnClickListener { showInviteTab() }

        btnEnter.setOnClickListener {
            if (currentUserId != -1) {
                pvpViewModel.clickFindMatch(currentUserId)
                showMatchmakingLoadingDialog()
            }
        }
        
        btnStartPvpFriend.setOnClickListener {
            val friend = selectedFriend
            if (friend == null) {
                Toast.makeText(this, "Vui lòng chọn một người bạn để thách đấu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedThemeIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 chủ đề!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val blueprint = RandomBlueprintRequest(
                difficulty = selectedDifficulty.toByte(),
                themeIds = selectedThemeIds,
                totalQuestions = selectedQuestions
            )

            pvpViewModel.sendInvite(friend.username, blueprint)
            showWaitingInviteDialog(friend.username)
        }

        findViewById<View>(R.id.card_select_theme).setOnClickListener { showThemeSelectionDialog() }
        findViewById<View>(R.id.card_select_difficulty).setOnClickListener { showDifficultyDialog() }
        findViewById<View>(R.id.card_select_questions).setOnClickListener { showQuestionsDialog() }
    }

    private fun updateConfigDisplay() {
        tvSelectedTheme.text = if (selectedThemeIds.isEmpty()) "Từ vựng" else selectedThemeName
        tvSelectedDifficulty.text = when(selectedDifficulty) {
            1 -> "Dễ"
            2 -> "Vừa"
            3 -> "Khó"
            else -> "Vừa"
        }
        tvSelectedQuestions.text = "$selectedQuestions Câu"
    }

    private fun showDifficultyDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_select_difficulty, null)
        
        val optEasy = view.findViewById<View>(R.id.option_easy)
        val optMedium = view.findViewById<View>(R.id.option_medium)
        val optHard = view.findViewById<View>(R.id.option_hard)
        val rbEasy = view.findViewById<RadioButton>(R.id.rb_easy)
        val rbMedium = view.findViewById<RadioButton>(R.id.rb_medium)
        val rbHard = view.findViewById<RadioButton>(R.id.rb_hard)
        
        fun resetSelection() {
            optEasy.setBackgroundResource(R.drawable.bg_card_white)
            optMedium.setBackgroundResource(R.drawable.bg_card_white)
            optHard.setBackgroundResource(R.drawable.bg_card_white)
            rbEasy.isChecked = false
            rbMedium.isChecked = false
            rbHard.isChecked = false
        }

        when(selectedDifficulty) {
            1 -> { optEasy.setBackgroundResource(R.drawable.bg_option_selected); rbEasy.isChecked = true }
            2 -> { optMedium.setBackgroundResource(R.drawable.bg_option_selected); rbMedium.isChecked = true }
            3 -> { optHard.setBackgroundResource(R.drawable.bg_option_selected); rbHard.isChecked = true }
        }

        var tempDifficulty = selectedDifficulty

        optEasy.setOnClickListener { resetSelection(); optEasy.setBackgroundResource(R.drawable.bg_option_selected); rbEasy.isChecked = true; tempDifficulty = 1 }
        optMedium.setOnClickListener { resetSelection(); optMedium.setBackgroundResource(R.drawable.bg_option_selected); rbMedium.isChecked = true; tempDifficulty = 2 }
        optHard.setOnClickListener { resetSelection(); optHard.setBackgroundResource(R.drawable.bg_option_selected); rbHard.isChecked = true; tempDifficulty = 3 }

        view.findViewById<Button>(R.id.btn_confirm_difficulty).setOnClickListener {
            selectedDifficulty = tempDifficulty
            updateConfigDisplay()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showQuestionsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_select_questions, null)
        
        val tvCount = view.findViewById<TextView>(R.id.tv_question_count_dialog)
        var tempCount = selectedQuestions
        tvCount.text = tempCount.toString()

        view.findViewById<TextView>(R.id.btn_minus_questions).setOnClickListener {
            if (tempCount > 5) {
                tempCount -= 5
                tvCount.text = tempCount.toString()
            }
        }
        view.findViewById<TextView>(R.id.btn_plus_questions).setOnClickListener {
            if (tempCount < 50) {
                tempCount += 5
                tvCount.text = tempCount.toString()
            }
        }

        view.findViewById<Button>(R.id.btn_confirm_questions).setOnClickListener {
            selectedQuestions = tempCount
            updateConfigDisplay()
            dialog.dismiss()
        }
        
        view.findViewById<View>(R.id.btn_close_dialog).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showThemeSelectionDialog() {
        val dialog = BottomSheetDialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_select_theme, null)
        
        val rvThemes = view.findViewById<RecyclerView>(R.id.rv_themes_selection)
        val themesResource = pvpViewModel.themes.value
        
        if (themesResource is Resource.Success) {
            val themes = themesResource.data ?: emptyList()
            val adapter = SelectThemeAdapter(themes)
            rvThemes.adapter = adapter
            rvThemes.layoutManager = GridLayoutManager(this, 2)

            view.findViewById<Button>(R.id.btn_confirm_themes).setOnClickListener {
                val ids = adapter.getSelectedIds()
                if (ids.isNotEmpty()) {
                    selectedThemeIds.clear()
                    selectedThemeIds.addAll(ids)
                    selectedThemeName = themes.find { it.id == ids[0] }?.themeName ?: "Nhiều chủ đề"
                    if (ids.size > 1) selectedThemeName += " (+${ids.size - 1})"
                    updateConfigDisplay()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Vui lòng chọn ít nhất 1 chủ đề!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Đang chuẩn bị danh sách chủ đề...", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.btn_close_themes).setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun observeViewModelData() {
        pvpViewModel.connectionState.observe(this) { isConnected ->
            btnEnter.isEnabled = isConnected
            btnStartPvpFriend.isEnabled = isConnected
        }

        pvpViewModel.queueStatus.observe(this) { status ->
            when (status) {
                "WAITING" -> matchmakingDialog?.findViewById<TextView>(R.id.tv_status)?.text = "Đang tìm đối thủ..."
                "WAITING_FOR_ENEMY_READY" -> {
                    matchmakingDialog?.findViewById<TextView>(R.id.tv_status)?.text = "Đã sẵn sàng! Đang đợi đối thủ..."
                    matchmakingDialog?.findViewById<Button>(R.id.btn_ready_confirm)?.isEnabled = false
                }
            }
        }

        pvpViewModel.friendList.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    fullFriendList = resource.data ?: emptyList()
                    filterFriends(etSearchFriends.text.toString())
                }
                is Resource.Error -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> {}
            }
        }

        pvpViewModel.incomingInvite.observe(this) { invite ->
            invite?.let { showIncomingInviteDialog(it) }
        }

        pvpViewModel.inviteResult.observe(this) { result ->
            waitingInviteDialog?.dismiss()
            val message = when (result) {
                "INVITE_DECLINED" -> "Bạn của bạn đã từ chối lời mời."
                "INVITE_TIMEOUT" -> "Lời mời đã hết hạn do không có phản hồi."
                "INVITE_EXPIRED" -> "Lời mời đã hết hạn."
                else -> null
            }
            message?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }

        pvpViewModel.matchResultJson.observe(this) { responseString ->
            if (responseString == null) return@observe
            waitingInviteDialog?.dismiss()

            if (responseString == "MATCH_TIMEOUT") {
                matchmakingDialog?.dismiss()
                Toast.makeText(this, "Trận đấu bị hủy!", Toast.LENGTH_SHORT).show()
                return@observe
            }

            try {
                val matchObj = JSONObject(responseString)
                val mId = if (matchObj.has("matchId")) matchObj.getInt("matchId") else matchObj.getInt("id")
                if (mId != -1) currentMatchId = mId

                if (matchObj.has("player1Username") && matchObj.has("player2Username") && !matchObj.has("questions")) {
                    showReadyDialog(matchObj.getString("player1Username"), matchObj.getString("player2Username"))
                }
            } catch (e: Exception) {
                Log.e("PVP_WS", "Lỗi JSON: ${e.message}")
            }
        }

        pvpViewModel.pvpQuizJson.observe(this) { quizJson ->
            if (quizJson == null || currentMatchId == -1) return@observe

            if (PvpViewModel.tryConsumeMatch(currentMatchId)) {
                matchmakingDialog?.dismiss()
                waitingInviteDialog?.dismiss()
                val intent = Intent(this, PvpQuizActivity::class.java).apply {
                    putExtra("MATCH_JSON", quizJson)
                    putExtra("MATCH_ID", currentMatchId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                pvpViewModel.clearPvpQuiz()
                startActivity(intent)
                finish() 
            }
        }
    }

    private fun showIncomingInviteDialog(invite: InviteResponse) {
        val blueprint = invite.randomBlueprintRequest
        val details = if (blueprint != null) {
            val diff = when(blueprint.difficulty.toInt()) {
                1 -> "Dễ"
                2 -> "Vừa"
                3 -> "Khó"
                else -> "Vừa"
            }
            "\n(Độ khó: $diff, Số câu: ${blueprint.totalQuestions})"
        } else ""

        AlertDialog.Builder(this)
            .setTitle("Lời mời thách đấu")
            .setMessage("${invite.inviterUsername} muốn đấu với bạn!$details")
            .setPositiveButton("Đồng ý") { _, _ ->
                pvpViewModel.respondToInvite(invite.inviteId, true)
                showMatchmakingLoadingDialog()
            }
            .setNegativeButton("Từ chối") { _, _ ->
                pvpViewModel.respondToInvite(invite.inviteId, false)
            }
            .setCancelable(false)
            .show()
    }

    private fun showWaitingInviteDialog(friendName: String) {
        waitingInviteDialog = AlertDialog.Builder(this)
            .setTitle("Đang chờ phản hồi")
            .setMessage("Đã gửi lời mời tới $friendName. Đang đợi họ chấp nhận...")
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showMatchmakingLoadingDialog() {
        if (matchmakingDialog?.isShowing == true) return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_matchmaking, null)
        matchmakingDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()
        matchmakingDialog?.show()

        dialogView.findViewById<Button>(R.id.btn_cancel_queue).setOnClickListener {
            pvpViewModel.clickCancelMatch(currentUserId)
            matchmakingDialog?.dismiss()
        }
    }

    private fun showReadyDialog(player1Name: String, player2Name: String) {
        if (matchmakingDialog == null || !matchmakingDialog!!.isShowing) {
            showMatchmakingLoadingDialog()
        }
        matchmakingDialog?.let { dialog ->
            dialog.findViewById<Button>(R.id.btn_cancel_queue)?.visibility = View.GONE
            dialog.findViewById<View>(R.id.pb_loading)?.visibility = View.GONE
            dialog.findViewById<TextView>(R.id.tv_status)?.text = "ĐÃ TÌM THẤY ĐỐI THỦ!\n$player1Name VS $player2Name"

            val btnReadyConfirm = dialog.findViewById<Button>(R.id.btn_ready_confirm)
            btnReadyConfirm?.visibility = View.VISIBLE
            btnReadyConfirm?.setOnClickListener {
                if (currentMatchId != -1) {
                    pvpViewModel.sendReadyStatus(currentMatchId)
                    btnReadyConfirm.isEnabled = false
                    btnReadyConfirm.text = "ĐÃ SẴN SÀNG"
                }
            }
        }
    }

    private fun showRankingTab() {
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility = View.GONE
        btnStartPvpFriend.visibility = View.GONE
        tabRanking.setBackgroundResource(R.drawable.bg_card_white)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabInvite.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabInvite.setTextColor(android.graphics.Color.parseColor("#8E8E8E"))
    }

    private fun showInviteTab() {
        layoutInviteContent.visibility = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        btnStartPvpFriend.visibility = View.VISIBLE
        tabInvite.setBackgroundResource(R.drawable.bg_card_white)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabRanking.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabRanking.setTextColor(android.graphics.Color.parseColor("#8E8E8E"))
        pvpViewModel.loadFriendList(token)
    }
}
