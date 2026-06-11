package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.model.Response.InviteResponse
import com.example.appenggo.viewmodel.PvpViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var pvpViewModel: PvpViewModel
    private var currentMatchId: Int = -1
    private var readyDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            setupPvpGlobalListener()

            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { replaceFragment(HomeFragment()); true }
                    R.id.nav_ranking -> { Toast.makeText(this, "Tính năng Xếp hạng đang phát triển", Toast.LENGTH_SHORT).show(); true }
                    R.id.nav_community -> { Toast.makeText(this, "Tính năng Cộng đồng đang phát triển", Toast.LENGTH_SHORT).show(); true }
                    R.id.nav_profile -> { Toast.makeText(this, "Tính năng Cá nhân đang phát triển", Toast.LENGTH_SHORT).show(); true }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khởi tạo: ${e.message}")
        }
    }

    private fun setupPvpGlobalListener() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", null)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (token != null && userId != -1) {
            pvpViewModel = ViewModelProvider(this)[PvpViewModel::class.java]
            pvpViewModel.startPvpSession(token)
            pvpViewModel.subscribeToFriendInvites(userId)

            pvpViewModel.incomingInvite.observe(this) { invite ->
                invite?.let { showInviteDialog(it) }
            }

            pvpViewModel.matchResultJson.observe(this) { json ->
                if (json == null) return@observe 

                try {
                    val jsonObj = JSONObject(json)
                    val matchId = if (jsonObj.has("matchId")) jsonObj.getInt("matchId") else jsonObj.getInt("id")
                    
                    // Kiểm tra ID trận đấu để không hiện dialog cũ
                    if (matchId != PvpViewModel.lastStartedMatchId) {
                        currentMatchId = matchId
                        showReadyDialog(json)
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error parsing matchResultJson: ${e.message}")
                }
            }

            pvpViewModel.pvpQuizJson.observe(this) { quizJson ->
                if (quizJson != null && currentMatchId != -1 && currentMatchId != PvpViewModel.lastStartedMatchId) {
                    
                    val matchToStart = currentMatchId
                    PvpViewModel.lastStartedMatchId = matchToStart
                    
                    // Đóng dialog sẵn sàng trước khi vào trận
                    readyDialog?.dismiss()
                    readyDialog = null

                    val intent = Intent(this, PvpQuizActivity::class.java).apply {
                        putExtra("MATCH_JSON", quizJson)
                        putExtra("MATCH_ID", matchToStart)
                        // Quan trọng: Sử dụng FLAG_ACTIVITY_CLEAR_TOP để không chồng Activity
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    
                    // Reset ngay lập tức để không lặp lại
                    pvpViewModel.clearPvpQuiz() 
                    currentMatchId = -1
                    startActivity(intent)
                }
            }
        }
    }

    private fun showInviteDialog(invite: InviteResponse) {
        AlertDialog.Builder(this)
            .setTitle("Lời mời thách đấu!")
            .setMessage("${invite.inviterUsername} muốn thách đấu PvP với bạn. Bạn có đồng ý không?")
            .setPositiveButton("Đồng ý") { _, _ ->
                pvpViewModel.respondToInvite(invite.inviteId, true)
            }
            .setNegativeButton("Từ chối") { _, _ ->
                pvpViewModel.respondToInvite(invite.inviteId, false)
            }
            .setCancelable(false)
            .show()
    }

    private fun showReadyDialog(matchJson: String) {
        if (readyDialog != null) readyDialog?.dismiss()

        readyDialog = AlertDialog.Builder(this)
            .setTitle("Trận đấu đã sẵn sàng")
            .setMessage("Đối thủ đã chấp nhận! Bấm Sẵn sàng để bắt đầu thi đấu.")
            .setPositiveButton("SẮN SÀNG") { _, _ ->
                if (currentMatchId != -1) {
                    pvpViewModel.sendReadyStatus(currentMatchId)
                }
            }
            .setNegativeButton("HỦY") { _, _ -> 
                currentMatchId = -1
                pvpViewModel.clearPvpQuiz()
            }
            .setCancelable(false)
            .create()
        
        readyDialog?.show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
