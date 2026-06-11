package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.Request.LoginRequest
import com.example.appenggo.repository.AuthRepository
import com.example.appenggo.viewmodel.AuthResult
import com.example.appenggo.viewmodel.AuthViewModel
import com.example.appenggo.viewmodel.AuthViewModelFactory
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    private lateinit var edUsername: EditText
    private lateinit var edPassword: EditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var txtRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupViewModel()
        initViews()
        observeViewModel()
    }

    private fun setupViewModel() {
        // Khởi tạo Repository và Factory cho MVVM
        val repository = AuthRepository(RetrofitClient.api)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun initViews() {
        edUsername = findViewById(R.id.edtEmail)
        edPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun observeViewModel() {
        // Quan sát kết quả từ ViewModel (đặc trưng của MVVM)
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    btnLogin.isEnabled = false
                    Log.d("LoginActivity", "Đang xử lý đăng nhập...")
                }
                is AuthResult.Success -> {
                    btnLogin.isEnabled = true
                    val loginData = result.data
                    if (loginData != null && loginData.result.authenticated) {
                        val rawToken = loginData.result.token

                        // 🎯 TỰ ĐỘNG BÓC SỐ USER_ID TỪ TRONG JWT TOKEN MỚI
                        val realUserId = getUserIdFromJwt(rawToken)

                        // Lưu token thực và ID vừa bóc tách tự động vào SharedPreferences
                        saveLoginData(rawToken, realUserId)

                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                is AuthResult.Error -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLogin() {
        val username = edUsername.text.toString().trim()
        val password = edPassword.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            edUsername.error = "Nhập username!"
            return
        }

        if (TextUtils.isEmpty(password)) {
            edPassword.error = "Nhập password!"
            return
        }

        // Gửi yêu cầu cho ViewModel xử lý (không xử lý logic tại đây)
        viewModel.login(LoginRequest(username, password))
    }

    private fun saveLoginData(token: String, userId: Int) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putString("TOKEN", token)
            .putInt("USER_ID", userId)
            .apply()
    }

    // 🎯 HÀM PHỤ GIAO CHỨC NĂNG GIẢI MÃ PAYLOAD CỦA JWT ĐỂ TRÍCH XUẤT USERID
    private fun getUserIdFromJwt(token: String): Int {
        try {
            // Chuỗi JWT có cấu trúc chuẩn ngăn cách bởi dấu chấm: Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size < 2) return -1

            // Lấy phần giữa (Index 1) giải mã Base64 sang chuỗi văn bản JSON
            val payloadEncoded = parts[1]
            val bytes = android.util.Base64.decode(payloadEncoded, android.util.Base64.DEFAULT)
            val payloadJson = String(bytes, Charsets.UTF_8)

            // Đọc từ khóa "userId" từ đối tượng JSON
            val jsonObject = JSONObject(payloadJson)
            val extractedId = jsonObject.optInt("userId", -1)

            Log.d("PVP_WS", "Đã giải mã JWT thành công -> Tìm thấy userId: $extractedId")
            return extractedId
        } catch (e: Exception) {
            Log.e("PVP_WS", "Lỗi trong quá trình tự động bóc tách JWT: ${e.message}")
            return -1
        }
    }
}