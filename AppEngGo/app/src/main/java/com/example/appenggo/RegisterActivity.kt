package com.example.appenggo

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var edUsername: EditText
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText

    private lateinit var edConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edUsername = findViewById(R.id.edtUsername)
        edEmail = findViewById(R.id.edtEmail)
        edPassword = findViewById(R.id.edtPassword)
        edConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    private fun handleRegister() {

        val username = edUsername.text.toString().trim()
        val email = edEmail.text.toString().trim()
        val password = edPassword.text.toString().trim()
        val confirmPassword = edConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            edUsername.error = "Nhập username!"
            return
        }

        if (TextUtils.isEmpty(email)) {
            edEmail.error = "Nhập email!"
            return
        }

        if (TextUtils.isEmpty(password)) {
            edPassword.error = "Nhập password!"
            return
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            edConfirmPassword.error = "Nhập lại password!"
            return
        }

        if (password != confirmPassword) {
            edConfirmPassword.error = "Mật khẩu không khớp!"
            return
        }

        val request = SignupRequest(username, email, password)

        // 🔥 coroutine thay cho callback
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.signup(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Lỗi kết nối!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}