package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment // <-- thêm
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel // <-- thêm
import com.example.appenggo.websocket.WebSocketManager
import com.example.appenggo.model.NotificationRepository
import com.example.appenggo.model.NotificationPayload          // <-- đã có
import com.example.appenggo.view.NotificationActivity          // <-- thêm

class HomeFragment : Fragment() {

    // ------------------------------------------------------------------------
    // ViewModel & data
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // UI references
    // ------------------------------------------------------------------------
    private lateinit var viewModel: MainViewModel
    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var btnLearnVocabulary: View? = null
    private var btn_battle: View? = null


    private var btnBell: View? = null
    private var tvNotificationBadge: TextView? = null
    private var unreadCount = 0

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

    // ------------------------------------------------------------------------
    // UI init
    // ------------------------------------------------------------------------
    private fun initViews(view: View) {
        tvStreak = view.findViewById(R.id.tv_streak)
        tvLevel = view.findViewById(R.id.tv_level)
        tvProgress = view.findViewById(R.id.tv_progress)
        btnLearnVocabulary = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle = view.findViewById(R.id.btn_battle)
        btnBell = view.findViewById(R.id.btn_bell)
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge)
    }

    // ------------------------------------------------------------------------
    // Click listeners
    // ------------------------------------------------------------------------
    private fun setupClickListeners() {
        btnLearnVocabulary?.setOnClickListener {
            startActivity(Intent(requireContext(), VocabularyActivity::class.java))
        }
        btn_battle?.setOnClickListener {
            startActivity(Intent(requireContext(), PvpActivity::class.java))
        }


        // ------------------- Chuông (notification) -------------------
        btnBell?.setOnClickListener {
            // Reset badge
            unreadCount = 0
            updateBadge()
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }

    // ------------------------------------------------------------------------
    // WebSocket listening
    // ------------------------------------------------------------------------
    private fun listenNotifications() {
        WebSocketManager.onNotificationReceived = { payload ->
            activity?.runOnUiThread {
                NotificationRepository.add(payload) // ✅ thay notifVm.add + notifications.add

                unreadCount++
                updateBadge()

                val msg = when (payload.type) {
                    "FRIEND_REQUEST" -> "🔔 ${payload.fromUsername} gửi lời mời kết bạn"
                    "FRIEND_ACCEPTED" -> "✅ ${payload.fromUsername} đã chấp nhận kết bạn"
                    else -> payload.message
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Badge UI
    // ------------------------------------------------------------------------
    private fun updateBadge() {
        if (unreadCount > 0) {
            tvNotificationBadge?.visibility = View.VISIBLE
            tvNotificationBadge?.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            tvNotificationBadge?.visibility = View.GONE
        }
    }

    // ------------------------------------------------------------------------
    // ViewModel dữ liệu người dùng (streak, level,…)
    // ------------------------------------------------------------------------
    private fun observeViewModel() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            tvStreak?.text = " ${stats.streak}"
            tvLevel?.text = "LV. ${stats.level}"
            tvProgress?.text = "${stats.currentProgress}/${stats.totalProgress}"
        }
    }

    // ------------------------------------------------------------------------
    // Cleanup
    // ------------------------------------------------------------------------
    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy callback khi fragment bị destroy để tránh memory leak
        WebSocketManager.onNotificationReceived = null
    }
}