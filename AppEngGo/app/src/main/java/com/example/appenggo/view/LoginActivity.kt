package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.LoginRequest
import com.example.appenggo.repository.AuthRepository
import com.example.appenggo.viewmodel.AuthResult
import com.example.appenggo.viewmodel.AuthViewModel
import com.example.appenggo.viewmodel.AuthViewModelFactory
import org.json.JSONObject

class LoginActivity : BaseActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var edUsername: EditText
    private lateinit var edPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView
    private lateinit var imgEye: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupViewModel()
        initViews()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(RetrofitClient.api)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun initViews() {
        edUsername = findViewById(R.id.edtEmail)
        edPassword = findViewById(R.id.edtPassword)
        btnLogin   = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)
        imgEye = findViewById(R.id.imgEye)

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnLogin.setOnClickListener { handleLogin() }

        imgEye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                edPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgEye.setImageResource(R.drawable.ic_eye)
            } else {
                edPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgEye.setImageResource(R.drawable.ic_uneye)
            }
            edPassword.setSelection(edPassword.text.length)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    showLoading("Đang đăng nhập...")
                }
                is AuthResult.Success -> {
                    hideLoading()
                    val loginData = result.data
                    if (loginData != null && loginData.result.authenticated) {
                        val username = edUsername.text.toString().trim()
                        saveToken(loginData.result.token, username)
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                is AuthResult.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLogin() {
        val username = edUsername.text.toString().trim()
        val password = edPassword.text.toString().trim()
        if (TextUtils.isEmpty(username)) { edUsername.error = "Nhập username!"; return }
        if (TextUtils.isEmpty(password)) { edPassword.error = "Nhập password!"; return }
        viewModel.login(LoginRequest(username, password))
    }

    private fun saveToken(token: String, username: String) {
        val userId = decodeUserIdFromJwt(token)
        Log.d("LoginActivity", "Decoded userId=$userId from JWT")

        getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
            .putString("TOKEN", token)
            .putString("USERNAME", username)
            .putInt("USER_ID", userId)
            .apply()
    }

    private fun decodeUserIdFromJwt(token: String): Int {
        return try {
            val parts   = token.split(".")
            if (parts.size < 2) return -1
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING))
            val json    = JSONObject(payload)
            json.getInt("userId")
        } catch (e: Exception) {
            Log.e("LoginActivity", "JWT decode error: ${e.message}")
            -1
        }
    }
}