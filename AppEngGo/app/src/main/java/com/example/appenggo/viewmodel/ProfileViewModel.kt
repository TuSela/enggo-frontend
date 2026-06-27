package com.example.appenggo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.model.ApiResponse
import com.example.appenggo.model.UpdatePasswordRequest
import com.example.appenggo.model.UserBadge
import com.example.appenggo.model.UserResponse
import com.example.appenggo.repository.UserRepository
import kotlinx.coroutines.launch

sealed class ProfileResult<out T> {
    data class Success<out T>(val data: T?) : ProfileResult<T>()
    data class Error(val message: String) : ProfileResult<Nothing>()
    object Loading : ProfileResult<Nothing>()
}

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userInfo = MutableLiveData<ProfileResult<UserResponse>>()
    val userInfo: LiveData<ProfileResult<UserResponse>> = _userInfo

    private val _userBadges = MutableLiveData<ProfileResult<List<UserBadge>>>()
    val userBadges: LiveData<ProfileResult<List<UserBadge>>> = _userBadges

    private val _updatePasswordResult = MutableLiveData<ProfileResult<String>>()
    val updatePasswordResult: LiveData<ProfileResult<String>> = _updatePasswordResult

    fun fetchProfileData(token: String) {
        _userInfo.value = ProfileResult.Loading
        _userBadges.value = ProfileResult.Loading

        viewModelScope.launch {
            try {
                val userResponse = repository.getMyInfo(token)
                if (userResponse.code == 1000) {
                    _userInfo.value = ProfileResult.Success(userResponse.result)
                } else {
                    _userInfo.value = ProfileResult.Error("Failed to load user info")
                }

                val badgesResponse = repository.getMyBadges(token)
                if (badgesResponse.code == 1000) {
                    _userBadges.value = ProfileResult.Success(badgesResponse.result)
                } else {
                    _userBadges.value = ProfileResult.Error("Failed to load badges")
                }
            } catch (e: Exception) {
                _userInfo.value = ProfileResult.Error(e.message ?: "Unknown error")
                _userBadges.value = ProfileResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updatePassword(token: String, request: UpdatePasswordRequest) {
        _updatePasswordResult.value = ProfileResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.updatePassword(token, request)
                if (response.code == 1000) {
                    _updatePasswordResult.value = ProfileResult.Success(response.result)
                } else {
                    _updatePasswordResult.value = ProfileResult.Error(response.message ?: "Cập nhật mật khẩu thất bại")
                }
            } catch (e: Exception) {
                _updatePasswordResult.value = ProfileResult.Error("Lỗi kết nối mạng")
            }
        }
    }
}
