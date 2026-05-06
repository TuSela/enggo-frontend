package com.example.appenggo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.model.LoginRequest
import com.example.appenggo.model.LoginResponse
import com.example.appenggo.model.SignupRequest
import com.example.appenggo.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T?) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<AuthResult<LoginResponse>>()
    val loginResult: LiveData<AuthResult<LoginResponse>> = _loginResult

    private val _signupResult = MutableLiveData<AuthResult<Void>>()
    val signupResult: LiveData<AuthResult<Void>> = _signupResult

    fun login(request: LoginRequest) {
        _loginResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                if (response.isSuccessful) {
                    _loginResult.value = AuthResult.Success(response.body())
                } else {
                    _loginResult.value = AuthResult.Error("Sai tài khoản hoặc mật khẩu!")
                }
            } catch (e: Exception) {
                _loginResult.value = AuthResult.Error("Lỗi kết nối!")
            }
        }
    }

    fun signup(request: SignupRequest) {
        _signupResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.signup(request)
                if (response.isSuccessful) {
                    _signupResult.value = AuthResult.Success(null)
                } else {
                    _signupResult.value = AuthResult.Error("Đăng ký thất bại!")
                }
            } catch (e: Exception) {
                _signupResult.value = AuthResult.Error("Lỗi kết nối!")
            }
        }
    }
}