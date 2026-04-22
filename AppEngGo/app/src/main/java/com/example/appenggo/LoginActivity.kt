package com.example.appenggo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var edUsername: EditText
    private lateinit var edPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edUsername = findViewById(R.id.edtEmail) // nếu bạn dùng chung ô email/username
        edPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)

        // 👉 sang màn đăng ký
        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 👉 xử lý login
        btnLogin.setOnClickListener {
            handleLogin()
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

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful && response.body() != null) {

                    val token = response.body()!!.result.token

                    // 🔥 LƯU TOKEN
                    saveToken(token)

                    Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Lỗi kết nối!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("TOKEN", token).apply()
    }
}