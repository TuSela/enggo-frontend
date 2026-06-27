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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.adapter.MissionAdapter
import com.example.appenggo.viewmodel.MainViewModel
import com.example.appenggo.viewmodel.MissionViewModel
import com.example.appenggo.websocket.WebSocketManager
import com.example.appenggo.model.NotificationRepository

class HomeFragment : Fragment() {

    // ViewModels
    private lateinit var viewModel: MainViewModel
    private lateinit var missionViewModel: MissionViewModel

    // UI references
    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var btnLearnVocabulary: View? = null
    private var btn_battle: View? = null
    private var btnBell: View? = null
    private var tvNotificationBadge: TextView? = null
    private var rvMissions: RecyclerView? = null

    private var unreadCount = 0

    // Adapter
    private lateinit var missionAdapter: MissionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupMissionRecyclerView()
        setupClickListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        missionViewModel = ViewModelProvider(requireActivity())[MissionViewModel::class.java]

        observeViewModel()
        observeMissions()
        listenNotifications()
        viewModel.loadMyInfo()

        // Load missions khi fragment được tạo
        missionViewModel.loadTodayMissions()
    }

    private fun initViews(view: View) {
        tvStreak = view.findViewById(R.id.tv_streak)
        tvLevel = view.findViewById(R.id.tv_level)
        tvProgress = view.findViewById(R.id.tv_progress)
        btnLearnVocabulary = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle = view.findViewById(R.id.btn_battle)
        btnBell = view.findViewById(R.id.btn_bell)
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge)
        rvMissions = view.findViewById(R.id.rv_missions)
    }

    private fun setupMissionRecyclerView() {
        missionAdapter = MissionAdapter { missionId ->
            missionViewModel.claimReward(missionId)
        }
        rvMissions?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = missionAdapter
            isNestedScrollingEnabled = false  // Quan trọng: nằm trong ScrollView
        }
    }

    private fun setupClickListeners() {
        btnLearnVocabulary?.setOnClickListener {
            startActivity(Intent(requireContext(), VocabularyActivity::class.java))
        }
        btn_battle?.setOnClickListener {
            startActivity(Intent(requireContext(), PvpActivity::class.java))
        }
        btnBell?.setOnClickListener {
            unreadCount = 0
            updateBadge()
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            tvStreak?.text = " ${user.streakDays}"
            tvLevel?.text = "LV. ${user.level}"
        }
    }

    private fun observeMissions() {
        missionViewModel.missions.observe(viewLifecycleOwner) { missions ->
            missionAdapter.submitList(missions)

            // Cập nhật progress header: số mission COMPLETED hoặc CLAIMED / tổng
            val completed = missions.count { it.status == "COMPLETED" || it.status == "CLAIMED" }
            val total = missions.size
            tvProgress?.text = "$completed/$total"

            // Cập nhật progress bar tổng (nếu có)
            val pbDaily = view?.findViewById<android.widget.ProgressBar>(R.id.pb_daily_mission)
            if (total > 0) {
                pbDaily?.max = total
                pbDaily?.progress = completed
            }
        }

        missionViewModel.claimResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            val msg = buildString {
                append("🎉 Nhận thưởng thành công! +${result.expAwarded} XP")
                if (!result.badgeResponse.isNullOrEmpty()) {
                    append("\n🏅 Badge mới: ${result.badgeResponse.joinToString { it.name }}")
                }
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            missionViewModel.clearClaimResult()
        }

        missionViewModel.error.observe(viewLifecycleOwner) { err ->
            err ?: return@observe
            Toast.makeText(requireContext(), "Lỗi: $err", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenNotifications() {
        WebSocketManager.onNotificationReceived = { payload ->
            activity?.runOnUiThread {
                NotificationRepository.add(payload)
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

    private fun updateBadge() {
        if (unreadCount > 0) {
            tvNotificationBadge?.visibility = View.VISIBLE
            tvNotificationBadge?.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            tvNotificationBadge?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebSocketManager.onNotificationReceived = null
    }
}