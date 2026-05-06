package com.example.appenggo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var edUsername: EditText
    private lateinit var edPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView
    private lateinit var progressBar: ProgressBar

    // Khởi tạo ViewModel bằng cách sử dụng "by viewModels()"
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupObservers()

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun initViews() {
        edUsername = findViewById(R.id.edtEmail)
        edPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)
        // Nếu bạn chưa có ProgressBar trong layout, bạn có thể thêm vào sau
        // progressBar = findViewById(R.id.progressBar) 
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Hiển thị loading nếu cần
                    btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    btnLogin.isEnabled = true
                    val token = resource.data?.result?.token
                    if (token != null) {
                        saveToken(token)
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
                is Resource.Error -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
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

        val request = LoginRequest(username, password)
        viewModel.login(request)
    }

    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("TOKEN", token).apply()
    }
}