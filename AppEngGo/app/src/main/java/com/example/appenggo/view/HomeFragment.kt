package com.example.appenggo.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.adapter.MissionAdapter
import com.example.appenggo.viewmodel.MainViewModel
import com.example.appenggo.viewmodel.MissionViewModel
import com.example.appenggo.websocket.WebSocketManager
import com.example.appenggo.model.NotificationRepository

class HomeFragment : BaseFragment() {

    // ── Bảng mốc XP theo level (index 0 = level 1) ───────────────────────────
    private val levelTable = listOf(
        0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700,
        3300, 4000, 4800, 5700, 6700, 7900, 9300, 10900, 12700, 14700,
        17000, 19600, 22500, 25700, 29300, 33300, 37800, 42800, 48400, 55000
    )

    /** Trả về % (0–100) XP tiến độ trong level hiện tại */
    private fun calcXpPercent(totalExp: Int, level: Int): Int {
        val idx = (level - 1).coerceIn(0, levelTable.lastIndex)
        if (idx + 1 > levelTable.lastIndex) return 100          // max level
        val currentReq = levelTable[idx]
        val nextReq    = levelTable[idx + 1]
        val inLevel    = (totalExp - currentReq).coerceAtLeast(0)
        val needed     = nextReq - currentReq
        return (inLevel * 100 / needed).coerceIn(0, 100)
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private lateinit var viewModel: MainViewModel
    private lateinit var missionViewModel: MissionViewModel

    private var tvStreak: TextView? = null
    private var tvGreeting: TextView? = null
    private var tvLevel: TextView? = null
    private var pbLevel: ProgressBar? = null
    private var tvProgress: TextView? = null
    private var btnLearnVocabulary: View? = null
    private var btn_battle: View? = null
    private var btnBell: View? = null
    private var tvNotificationBadge: TextView? = null
    private var rvMissions: RecyclerView? = null
    private var layoutMissionHeader: View? = null
    private var layoutMissionContent: View? = null
    private var ivMissionArrow: ImageView? = null
    private var isMissionExpanded = true

    private var unreadCount = 0
    private var isFirstLoad = true
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
    }

    private fun initViews(view: View) {
        tvStreak             = view.findViewById(R.id.tv_streak)
        tvGreeting           = view.findViewById(R.id.tv_greeting)
        tvLevel              = view.findViewById(R.id.tv_level)
        pbLevel              = view.findViewById(R.id.pb_level)
        tvProgress           = view.findViewById(R.id.tv_progress)
        btnLearnVocabulary   = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle           = view.findViewById(R.id.btn_battle)
        btnBell              = view.findViewById(R.id.btn_bell)
        tvNotificationBadge  = view.findViewById(R.id.tv_notification_badge)
        rvMissions           = view.findViewById(R.id.rv_missions)
        layoutMissionHeader  = view.findViewById(R.id.layout_mission_header)
        layoutMissionContent = view.findViewById(R.id.layout_mission_content)
        ivMissionArrow       = view.findViewById(R.id.iv_mission_arrow)
    }

    private fun setupMissionRecyclerView() {
        missionAdapter = MissionAdapter { missionId ->
            missionViewModel.claimReward(missionId)
        }
        rvMissions?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = missionAdapter
            isNestedScrollingEnabled = false
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
        layoutMissionHeader?.setOnClickListener {
            toggleMissionSection()
        }
    }

    private fun toggleMissionSection() {
        isMissionExpanded = !isMissionExpanded
        val content = layoutMissionContent ?: return
        val arrow   = ivMissionArrow ?: return

        if (isMissionExpanded) {
            content.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(arrow, "rotation", 180f, 0f).apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        } else {
            content.visibility = View.GONE
            ObjectAnimator.ofFloat(arrow, "rotation", 0f, 180f).apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh XP bar + missions mỗi khi fragment hiển thị lại
        if (::viewModel.isInitialized) {
            viewModel.loadMyInfo()
        }
        if (::missionViewModel.isInitialized) {
            missionViewModel.loadTodayMissions()
        }
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            tvStreak?.text = " ${user.streakDays}"
            tvGreeting?.text = "Xin chào, ${user.fullName?.takeIf { it.isNotBlank() } ?: user.username}"
            tvLevel?.text  = "LV ${user.level}"

            // ── XP bar theo bảng level ────────────────────────────────────────
            val percent = calcXpPercent(user.exp, user.level)
            pbLevel?.progress = percent
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (isFirstLoad) {
                if (loading) showLoading("Đang tải thông tin...") else {
                    hideLoading()
                    isFirstLoad = false
                }
            }
        }
    }

    private fun observeMissions() {
        missionViewModel.missions.observe(viewLifecycleOwner) { missions ->
            missionAdapter.submitList(missions)
            val completed = missions.count { it.status == "COMPLETED" || it.status == "CLAIMED" }
            val total     = missions.size
            tvProgress?.text = "$completed/$total"
            val pbDaily = view?.findViewById<ProgressBar>(R.id.pb_daily_mission)
            if (total > 0) {
                pbDaily?.max      = total
                pbDaily?.progress = completed
            }
        }

        missionViewModel.claimResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            val msg = buildString {
                append("Nhận thưởng thành công! +${result.expAwarded} XP")
                if (!result.badgeResponse.isNullOrEmpty()) {
                    append("\n🏅 Badge mới: ${result.badgeResponse.joinToString { it.name }}")
                }
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            // Cập nhật lại EXP/Level ngay sau khi nhận thưởng, không cần chờ onResume
            viewModel.loadMyInfo()
            missionViewModel.clearClaimResult()
        }

        missionViewModel.error.observe(viewLifecycleOwner) { err ->
            err ?: return@observe
            Toast.makeText(requireContext(), "Lỗi: $err", Toast.LENGTH_SHORT).show()
        }

        missionViewModel.isLoadingClaim.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading("Đang xử lý nhiệm vụ...") else hideLoading()
        }
    }

    private fun listenNotifications() {
        WebSocketManager.onNotificationReceived = { payload ->
            activity?.runOnUiThread {
                NotificationRepository.add(payload)
                unreadCount++
                updateBadge()
                val msg = when (payload.type) {
                    "FRIEND_REQUEST"  -> "🔔 ${payload.fromUsername} gửi lời mời kết bạn"
                    "FRIEND_ACCEPTED" -> "✅ ${payload.fromUsername} đã chấp nhận kết bạn"
                    else              -> payload.message
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