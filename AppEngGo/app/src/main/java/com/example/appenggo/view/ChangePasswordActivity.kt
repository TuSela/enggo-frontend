package com.example.appenggo.view

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.UpdatePasswordRequest
import com.example.appenggo.repository.UserRepository
import com.example.appenggo.viewmodel.ProfileResult
import com.example.appenggo.viewmodel.ProfileViewModel
import com.example.appenggo.viewmodel.ProfileViewModelFactory

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var edtOldPass: EditText
    private lateinit var edtNewPass: EditText
    private lateinit var edtConfirmPass: EditText
    private lateinit var btnUpdate: View
    private lateinit var viewModel: ProfileViewModel

    private var isOldPassVisible = false
    private var isNewPassVisible = false
    private var isConfirmPassVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        setupViewModel()
        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.api)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun initViews() {
        edtOldPass = findViewById(R.id.edt_old_password)
        edtNewPass = findViewById(R.id.edt_new_password)
        edtConfirmPass = findViewById(R.id.edt_confirm_password)
        btnUpdate = findViewById(R.id.btn_update)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btn_toggle_old_password).setOnClickListener {
            isOldPassVisible = !isOldPassVisible
            togglePasswordVisibility(edtOldPass, it as ImageView, isOldPassVisible)
        }

        findViewById<ImageView>(R.id.btn_toggle_new_password).setOnClickListener {
            isNewPassVisible = !isNewPassVisible
            togglePasswordVisibility(edtNewPass, it as ImageView, isNewPassVisible)
        }

        findViewById<ImageView>(R.id.btn_toggle_confirm_password).setOnClickListener {
            isConfirmPassVisible = !isConfirmPassVisible
            togglePasswordVisibility(edtConfirmPass, it as ImageView, isConfirmPassVisible)
        }

        btnUpdate.setOnClickListener {
            handleUpdatePassword()
        }
    }

    private fun observeViewModel() {
        viewModel.updatePasswordResult.observe(this) { result ->
            when (result) {
                is ProfileResult.Loading -> {
                    btnUpdate.isEnabled = false
                }
                is ProfileResult.Success -> {
                    btnUpdate.isEnabled = true
                    // result.data chứa chuỗi "Password has been changed" từ BE trả về
                    val message = result.data ?: "Đổi mật khẩu thành công"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ProfileResult.Error -> {
                    btnUpdate.isEnabled = true
                    val message = result.message ?: "Mật khẩu cũ không chính xác"
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_uneye)
        }
        editText.setSelection(editText.text.length)
    }

    private fun handleUpdatePassword() {
        val oldPass = edtOldPass.text.toString().trim()
        val newPass = edtNewPass.text.toString().trim()
        val confirmPass = edtConfirmPass.text.toString().trim()

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass.length < 8) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("TOKEN", "") ?: ""
        if (token.isNotEmpty()) {
            val request = UpdatePasswordRequest(oldPass, newPass, confirmPass)
            viewModel.updatePassword(token, request)
        } else {
            Toast.makeText(this, "Phiên làm việc hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
        }
    }
}
