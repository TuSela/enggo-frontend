package com.example.appenggo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appenggo.R
import com.google.android.material.button.MaterialButton

class ProfileInfoFragment : Fragment() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtBio: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_info, container, false)
        initViews(view)
        setupListeners()
        return view
    }

    private fun initViews(view: View) {
        edtFullName = view.findViewById(R.id.edt_full_name)
        edtEmail = view.findViewById(R.id.edt_email)
        edtBio = view.findViewById(R.id.edt_bio)
        btnSave = view.findViewById(R.id.btn_save)
        btnBack = view.findViewById(R.id.btn_back)

        // Điền thông tin mẫu để hiển thị trực quan
        edtFullName.setText("Nguyễn Văn A")
        edtEmail.setText("nguyenvana@gmail.com")
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val bio = edtBio.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty()) {
                Toast.makeText(context, "Họ tên và Email không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Xử lý gọi API cập nhật thông tin cá nhân tại đây
            Toast.makeText(context, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
