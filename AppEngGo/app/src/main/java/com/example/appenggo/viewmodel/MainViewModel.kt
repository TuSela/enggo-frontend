package com.example.appenggo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.UserResponse
import com.example.appenggo.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(RetrofitClient.api)

    private val _userInfo = MutableLiveData<UserResponse>()
    val userInfo: LiveData<UserResponse> = _userInfo

    private fun getToken(): String {
        return getApplication<Application>()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("TOKEN", "") ?: ""
    }

    fun loadMyInfo() {
        viewModelScope.launch {
            try {
                val response = repo.getMyInfo(getToken())
                if (response.code == 1000) {
                    _userInfo.value = response.result
                }
            } catch (e: Exception) {
                // xử lý lỗi nếu cần
            }
        }
    }
}