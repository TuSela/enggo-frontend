package com.example.appenggo.view

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    fun showLoading(message: String? = null) {
        (activity as? BaseActivity)?.showLoading(message)
    }

    fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }
}