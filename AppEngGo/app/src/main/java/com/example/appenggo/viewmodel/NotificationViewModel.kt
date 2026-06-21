package com.example.appenggo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appenggo.model.NotificationPayload

/**
 * ViewModel chia sẻ giữa HomeFragment và NotificationActivity.
 * Giữ danh sách thông báo ngay cả khi Activity/Fragment bị destroy‑recreate.
 */
class NotificationViewModel : ViewModel() {

    private val _list = MutableLiveData<List<NotificationPayload>>(emptyList())
    val list: LiveData<List<NotificationPayload>> = _list

    /** Thêm một thông báo mới ở đầu danh sách */
    fun add(payload: NotificationPayload) {
        val current = _list.value?.toMutableList() ?: mutableListOf()
        current.add(0, payload)          // newest lên đầu
        _list.value = current
    }

    /** Xóa toàn bộ danh sách (thường dùng khi người dùng mở danh sách và đã đọc) */
    fun clearAll() {
        _list.value = emptyList()
    }
    // NotificationViewModel.kt
    fun setAll(payloads: List<NotificationPayload>) {
        _list.value = payloads.toMutableList()
    }
}