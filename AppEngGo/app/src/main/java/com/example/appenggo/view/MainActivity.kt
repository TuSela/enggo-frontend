package com.example.appenggo.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appenggo.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            
            // Load HomeFragment mặc định khi mở app
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        replaceFragment(HomeFragment())
                        true
                    }
                    R.id.nav_ranking -> {
                        // Tạm thời load Fragment trống hoặc thông báo
                        Toast.makeText(this, "Tính năng Xếp hạng đang phát triển", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_community -> {
                        Toast.makeText(this, "Tính năng Cộng đồng đang phát triển", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_profile -> {
                        Toast.makeText(this, "Tính năng Cá nhân đang phát triển", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khởi tạo: ${e.message}")
            Toast.makeText(this, "Lỗi khởi tạo màn hình chính!", Toast.LENGTH_LONG).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}