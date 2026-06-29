package com.example.appenggo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.ClaimRewardResponse
import com.example.appenggo.model.MissionProgressResponse
import com.example.appenggo.repository.MissionRepository
import kotlinx.coroutines.launch

class MissionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = MissionRepository(RetrofitClient.api)

    private val _missions = MutableLiveData<List<MissionProgressResponse>>()
    val missions: LiveData<List<MissionProgressResponse>> = _missions

    private val _claimResult = MutableLiveData<ClaimRewardResponse?>()
    val claimResult: LiveData<ClaimRewardResponse?> = _claimResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoadingMissions = MutableLiveData<Boolean>()
    val isLoadingMissions: LiveData<Boolean> = _isLoadingMissions

    private val _isLoadingClaim = MutableLiveData<Boolean>()
    val isLoadingClaim: LiveData<Boolean> = _isLoadingClaim

    private fun getToken(): String {
        val prefs = getApplication<Application>()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("TOKEN", "") ?: ""
    }

    fun loadTodayMissions() {
        _isLoadingMissions.value = true
        viewModelScope.launch {
            try {
                val result = repo.getTodayMissions(getToken())
                _missions.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoadingMissions.value = false
            }
        }
    }

    fun claimReward(missionId: Int) {
        _isLoadingClaim.value = true
        viewModelScope.launch {
            try {
                val result = repo.claimReward(getToken(), missionId)
                _claimResult.value = result
                // Reload missions to reflect new status
                loadTodayMissions()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoadingClaim.value = false
            }
        }
    }

    fun clearClaimResult() {
        _claimResult.value = null
    }
}