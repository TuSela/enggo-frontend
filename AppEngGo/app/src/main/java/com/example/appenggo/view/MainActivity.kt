package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel
import com.example.appenggo.viewmodel.MissionViewModel
import com.example.appenggo.websocket.WebSocketManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var missionViewModel: MissionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        missionViewModel = ViewModelProvider(this)[MissionViewModel::class.java]

        WebSocketManager.connect(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_rank -> {
                    loadFragment(LeaderboardFragment())
                    true
                }
                R.id.nav_friend -> {
                     loadFragment(FriendFragment())
                    true
                }
                R.id.nav_profile -> {
                     loadFragment(ProfileFragment())
                    true
                }
                R.id.nav_setting -> {
                    loadFragment(SettingFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
            // Tải dữ liệu lần đầu khi app mở
            mainViewModel.loadMyInfo()
            missionViewModel.loadTodayMissions()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Chỉ reload khi có cờ RELOAD_HOME từ màn hình kết quả Quiz/PVP
        if (intent?.getBooleanExtra("RELOAD_HOME", false) == true) {
            mainViewModel.loadMyInfo()
            missionViewModel.loadTodayMissions()
            
            // Xóa cờ sau khi đã xử lý để tránh reload lặp lại nếu onNewIntent gọi lại
            intent.putExtra("RELOAD_HOME", false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.disconnect()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}