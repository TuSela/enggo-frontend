package com.example.appenggo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<Unit>>()
    val registerResult: LiveData<Resource<Unit>> = _registerResult

    fun login(request: LoginRequest) {
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.value = Resource.Success(response.body()!!)
                } else {
                    _loginResult.value = Resource.Error("Sai tài khoản hoặc mật khẩu!")
                }
            } catch (e: Exception) {
                _loginResult.value = Resource.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun signup(request: SignupRequest) {
        _registerResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.signup(request)
                if (response.isSuccessful) {
                    _registerResult.value = Resource.Success(Unit)
                } else {
                    _registerResult.value = Resource.Error("Đăng ký thất bại!")
                }
            } catch (e: Exception) {
                _registerResult.value = Resource.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}