package com.example.appenggo.view

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var pbDailyMission: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            initViews()
            viewModel = ViewModelProvider(this)[MainViewModel::class.java]
            observeViewModel()
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khởi tạo: ${e.message}")
            Toast.makeText(this, "Lỗi khởi tạo màn hình chính!", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        tvStreak = findViewById(R.id.tv_streak)
        tvLevel = findViewById(R.id.tv_level)
        tvProgress = findViewById(R.id.tv_progress)
        pbDailyMission = findViewById(R.id.pb_daily_mission)
    }

    private fun observeViewModel() {
        viewModel.userStats.observe(this) { stats ->
            tvStreak?.text = "🔥 ${stats.streak}"
            tvLevel?.text = "LV. ${stats.level}"
            tvProgress?.text = "${stats.currentProgress}/${stats.totalProgress}"
            
            if (stats.totalProgress > 0) {
                val progressPercent = (stats.currentProgress.toFloat() / stats.totalProgress * 100).toInt()
                pbDailyMission?.progress = progressPercent
            }
        }
    }
}