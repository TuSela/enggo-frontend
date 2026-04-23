package com.example.appenggo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appenggo.model.UserStats

class MainViewModel : ViewModel() {

    private val _userStats = MutableLiveData<UserStats>()
    val userStats: LiveData<UserStats> = _userStats

    init {
        // Đảm bảo dữ liệu được khởi tạo ngay lập tức
        _userStats.value = UserStats(
            streak = 0,
            level = 1,
            currentProgress = 0,
            totalProgress = 1
        )
        loadUserStats()
    }

    private fun loadUserStats() {
        // Dữ liệu mẫu thực tế
        val stats = UserStats(
            streak = 59,
            level = 15,
            currentProgress = 1,
            totalProgress = 3
        )
        _userStats.value = stats
    }
}