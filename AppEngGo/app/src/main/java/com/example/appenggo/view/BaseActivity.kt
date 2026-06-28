package com.example.appenggo.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appenggo.LoadingDialog

abstract class BaseActivity : AppCompatActivity() {
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(this)
    }

    fun showLoading(message: String? = null) {
        loadingDialog?.show(message)
    }

    fun hideLoading() {
        loadingDialog?.dismiss()
    }
}