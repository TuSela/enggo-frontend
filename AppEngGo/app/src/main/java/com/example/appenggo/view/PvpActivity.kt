package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.adapter.FriendAdapter
import com.example.appenggo.viewmodel.PvpViewModel
import org.json.JSONObject

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: LinearLayout
    private lateinit var layoutInviteContent: LinearLayout

    private lateinit var btnEnter: Button
    private lateinit var pvpViewModel: PvpViewModel
    private var matchmakingDialog: AlertDialog? = null

    // Friend PvP Views
    private lateinit var rvFriends: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var tvMyUsernameRanking: TextView
    private lateinit var tvMyUsernameInvite: TextView
    private lateinit var tvNoFriends: TextView

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
        // Đánh dấu PvpActivity đang hoạt động để MainActivity không xử lý chồng chéo
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

        rvFriends = findViewById(R.id.rv_friends_online)
        tvMyUsernameRanking = findViewById(R.id.tv_username_ranking)
        tvMyUsernameInvite = findViewById(R.id.tv_my_username_invite)
        tvNoFriends = findViewById(R.id.tv_no_friends)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        friendAdapter = FriendAdapter(emptyList()) { friend ->
            pvpViewModel.sendInvite(friend.username)
            Toast.makeText(this, "Đã gửi lời mời tới ${friend.username}", Toast.LENGTH_SHORT).show()
        }
        rvFriends.adapter = friendAdapter
    }

    private fun initViewModel() {
        pvpViewModel = ViewModelProvider(this)[PvpViewModel::class.java]

        token = intent.getStringExtra("JWT_TOKEN") ?: ""
        currentUserId = intent.getIntExtra("USER_ID", -1)
        val username = intent.getStringExtra("USERNAME") ?: "User"

        tvMyUsernameRanking.text = username
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
    }

    private fun observeViewModelData() {
        pvpViewModel.connectionState.observe(this) { isConnected ->
            btnEnter.isEnabled = isConnected
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

        pvpViewModel.friendList.observe(this) { friends ->
            if (friends.isNullOrEmpty()) {
                rvFriends.visibility = View.GONE
                tvNoFriends.visibility = View.VISIBLE
            } else {
                rvFriends.visibility = View.VISIBLE
                tvNoFriends.visibility = View.GONE
                friendAdapter.updateData(friends)
            }
        }

        pvpViewModel.incomingInvite.observe(this) { invite ->
            // 🎯 KIỂM TRA NULL: Chỉ hiển thị dialog khi có lời mời thực sự (không phải dữ liệu cũ đã clear)
            invite?.let {
                showIncomingInviteDialog(it.inviterUsername, it.inviteId)
            }
        }

        pvpViewModel.matchResultJson.observe(this) { responseString ->
            if (responseString == null) return@observe
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
                Log.e("PVP_WS", "Lỗi phân tích JSON: ${e.message}")
            }
        }

        pvpViewModel.pvpQuizJson.observe(this) { quizJson ->
            if (quizJson == null || currentMatchId == -1) return@observe

            // 🎯 CHỐNG LẶP: Thử tiêu thụ trận đấu. Chỉ 1 nơi (PvpActivity hoặc MainActivity) được phép mở.
            if (PvpViewModel.tryConsumeMatch(currentMatchId)) {
                matchmakingDialog?.dismiss()
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

    private fun showIncomingInviteDialog(inviterName: String, inviteId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Lời mời thách đấu")
            .setMessage("$inviterName muốn thi đấu PvP với bạn!")
            .setPositiveButton("Đồng ý") { _, _ ->
                pvpViewModel.respondToInvite(inviteId, true)
                showMatchmakingLoadingDialog()
            }
            .setNegativeButton("Từ chối") { _, _ ->
                pvpViewModel.respondToInvite(inviteId, false)
            }
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
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.btn_blue_fancy)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.white))
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_card_light_blue)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    private fun showInviteTab() {
        layoutInviteContent.visibility = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.btn_blue_fancy)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.white))
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_card_light_blue)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.black))
        
        pvpViewModel.loadFriendList(token)
    }
}
