package com.example.appenggo.view

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.UserUpdateRequest
import com.example.appenggo.repository.UserRepository
import com.example.appenggo.viewmodel.ProfileResult
import com.example.appenggo.viewmodel.ProfileViewModel
import com.example.appenggo.viewmodel.ProfileViewModelFactory
import com.google.android.material.button.MaterialButton

class ProfileInfoActivity : BaseActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtBio: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        setupViewModel()
        initViews()
        setupListeners()
        observeViewModel()
        
        loadCurrentUserInfo()
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.api)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun initViews() {
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email)
        edtBio = findViewById(R.id.edt_bio)
        btnSave = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun loadCurrentUserInfo() {
        val token = getToken()
        if (token != null) {
            viewModel.fetchProfileData(token)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            handleUpdateProfile()
        }
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(this) { result ->
            if (result is ProfileResult.Success) {
                val user = result.data
                user?.let {
                    edtFullName.setText(it.fullName)
                    edtEmail.setText(it.email)
                    edtBio.setText(it.bio ?: "")
                }
            }
        }

        viewModel.updateUserResult.observe(this) { result ->
            when (result) {
                is ProfileResult.Loading -> {
                    showLoading("Đang cập nhật...")
                }
                is ProfileResult.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is ProfileResult.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleUpdateProfile() {
        val fullName = edtFullName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val bio = edtBio.text.toString().trim()

        if (fullName.isEmpty()) {
            edtFullName.error = "Họ tên không được để trống"
            return
        }
        if (email.isEmpty()) {
            edtEmail.error = "Email không được để trống"
            return
        }

        val token = getToken()
        if (token != null) {
            val request = UserUpdateRequest(email, fullName, bio)
            viewModel.updateUser(token, request)
        } else {
            Toast.makeText(this, "Phiên làm việc hết hạn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getToken(): String? {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getString("TOKEN", null)
    }
}
