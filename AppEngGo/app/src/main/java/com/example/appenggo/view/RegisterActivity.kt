package com.example.appenggo.view

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.SignupRequest
import com.example.appenggo.repository.AuthRepository
import com.example.appenggo.viewmodel.AuthResult
import com.example.appenggo.viewmodel.AuthViewModel
import com.example.appenggo.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    
    private lateinit var edUsername: EditText
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var edConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
        edUsername = findViewById(R.id.edtUsername)
        edEmail = findViewById(R.id.edtEmail)
        edPassword = findViewById(R.id.edtPassword)
        edConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        txtLogin = findViewById(R.id.txtLogin)

        btnRegister.setOnClickListener {
            handleRegister()
        }

        txtLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.signupResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    btnRegister.isEnabled = false
                }
                is AuthResult.Success -> {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is AuthResult.Error -> {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
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
        if (password != confirmPassword) {
            edConfirmPassword.error = "Mật khẩu không khớp!"
            return
        }

        val request = SignupRequest(username, email, password)
        viewModel.signup(request)
    }
}