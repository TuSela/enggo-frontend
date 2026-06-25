package com.example.appenggo.view

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appenggo.R

class ChangePasswordFragment : Fragment() {

    private lateinit var edtOldPass: EditText
    private lateinit var edtNewPass: EditText
    private lateinit var edtConfirmPass: EditText
    
    private var isOldPassVisible = false
    private var isNewPassVisible = false
    private var isConfirmPassVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        
        initViews(view)
        setupListeners(view)
        
        return view
    }

    private fun initViews(view: View) {
        edtOldPass = view.findViewById(R.id.edt_old_password)
        edtNewPass = view.findViewById(R.id.edt_new_password)
        edtConfirmPass = view.findViewById(R.id.edt_confirm_password)
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<ImageView>(R.id.btn_toggle_old_password).setOnClickListener {
            isOldPassVisible = !isOldPassVisible
            togglePasswordVisibility(edtOldPass, it as ImageView, isOldPassVisible)
        }

        view.findViewById<ImageView>(R.id.btn_toggle_new_password).setOnClickListener {
            isNewPassVisible = !isNewPassVisible
            togglePasswordVisibility(edtNewPass, it as ImageView, isNewPassVisible)
        }

        view.findViewById<ImageView>(R.id.btn_toggle_confirm_password).setOnClickListener {
            isConfirmPassVisible = !isConfirmPassVisible
            togglePasswordVisibility(edtConfirmPass, it as ImageView, isConfirmPassVisible)
        }

        view.findViewById<View>(R.id.btn_update).setOnClickListener {
            handleUpdatePassword()
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
        val oldPass = edtOldPass.text.toString()
        val newPass = edtNewPass.text.toString()
        val confirmPass = edtConfirmPass.text.toString()

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass.length < 8) {
            Toast.makeText(context, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Gọi API đổi mật khẩu ở đây
        Toast.makeText(context, "Cập nhật mật khẩu thành công (Demo)", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}