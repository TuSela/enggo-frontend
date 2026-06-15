package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel
import com.example.appenggo.websocket.WebSocketManager

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var btnLearnVocabulary: View? = null
    private var btn_battle: View? = null

    private var btnBell: View? = null
    private var tvNotificationBadge: TextView? = null
    private var unreadCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupClickListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        observeViewModel()
        listenNotifications()
    }

    private fun initViews(view: View) {
        tvStreak = view.findViewById(R.id.tv_streak)
        tvLevel = view.findViewById(R.id.tv_level)
        tvProgress = view.findViewById(R.id.tv_progress)
        btnLearnVocabulary = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle = view.findViewById(R.id.btn_battle)
        btnBell = view.findViewById(R.id.btn_bell)
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge)
    }

    private fun setupClickListeners() {
        btnLearnVocabulary?.setOnClickListener {
            startActivity(Intent(requireContext(), VocabularyActivity::class.java))
        }
        btn_battle?.setOnClickListener {
            startActivity(Intent(requireContext(), PvpActivity::class.java))
        }

        // Click chuông → xem danh sách thông báo (reset badge)
        btnBell?.setOnClickListener {
            unreadCount = 0
            updateBadge()
            // TODO: mở màn hình danh sách thông báo nếu có
            Toast.makeText(requireContext(), "Danh sách thông báo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenNotifications() {
        // Đăng ký callback nhận thông báo từ WebSocket
        WebSocketManager.onNotificationReceived = { notification ->
            activity?.runOnUiThread {
                unreadCount++
                updateBadge()

                // Hiện toast khi có thông báo mới
                val msg = when (notification.type) {
                    "FRIEND_REQUEST" -> "🔔 ${notification.fromUsername} gửi lời mời kết bạn"
                    "FRIEND_ACCEPTED" -> "✅ ${notification.fromUsername} đã chấp nhận kết bạn"
                    else -> notification.message
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBadge() {
        if (unreadCount > 0) {
            tvNotificationBadge?.visibility = View.VISIBLE
            tvNotificationBadge?.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            tvNotificationBadge?.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            tvStreak?.text = " ${stats.streak}"
            tvLevel?.text = "LV. ${stats.level}"
            tvProgress?.text = "${stats.currentProgress}/${stats.totalProgress}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy callback khi fragment bị destroy tránh memory leak
        WebSocketManager.onNotificationReceived = null
    }
}