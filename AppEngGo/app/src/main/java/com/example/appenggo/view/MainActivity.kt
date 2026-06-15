package com.example.appenggo.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appenggo.R
import com.example.appenggo.websocket.WebSocketManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Kết nối WebSocket ngay khi vào MainActivity → server sẽ set status = ONLINE
        WebSocketManager.connect(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_learn -> {
                    // loadFragment(LearnFragment())
                    true
                }
                R.id.nav_pvp -> {
                    // loadFragment(PvpFragment())
                    true
                }
                R.id.nav_friend -> {
                     loadFragment(FriendFragment())
                    true
                }
                R.id.nav_profile -> {
                    // loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ngắt kết nối WebSocket khi đóng app → server sẽ set status = OFFLINE
        WebSocketManager.disconnect()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}