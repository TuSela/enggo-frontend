package com.example.appenggo.view

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appenggo.R
import com.google.android.material.button.MaterialButton

class ProfileInfoActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtBio: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email)
        edtBio = findViewById(R.id.edt_bio)
        btnSave = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btn_back)

        // TODO: Load real user data here, for now using placeholders
        edtFullName.setText("Nguyễn Văn A")
        edtEmail.setText("nguyenvana@gmail.com")
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val bio = edtBio.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Họ tên và Email không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Xử lý gọi API cập nhật thông tin cá nhân tại đây
            Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
