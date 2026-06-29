package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.BadgeAdapter
import com.example.appenggo.model.UserBadge
import com.example.appenggo.model.UserResponse
import com.example.appenggo.repository.UserRepository
import com.example.appenggo.viewmodel.ProfileResult
import com.example.appenggo.viewmodel.ProfileViewModel
import com.example.appenggo.viewmodel.ProfileViewModelFactory

class ProfileFragment : BaseFragment() {

    // ── Bảng mốc XP theo level ───────────────────────────────────────────────
    private val levelTable = listOf(
        0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700,
        3300, 4000, 4800, 5700, 6700, 7900, 9300, 10900, 12700, 14700,
        17000, 19600, 22500, 25700, 29300, 33300, 37800, 42800, 48400, 55000
    )
    // levelTable[0] = reqExp của level 1, levelTable[1] = reqExp của level 2, ...

    /**
     * Tính XP hiện tại trong level và XP cần để lên level tiếp theo.
     * Trả về Pair(currentInLevel, neededForLevel)
     */
    private fun getXpProgress(totalExp: Int, level: Int): Pair<Int, Int> {
        val idx = (level - 1).coerceIn(0, levelTable.lastIndex)
        val currentLevelReq = levelTable[idx]
        val nextLevelReq = if (idx + 1 <= levelTable.lastIndex) {
            levelTable[idx + 1]
        } else {
            // Đã max level – thanh đầy
            return Pair(1, 1)
        }
        val inLevel   = (totalExp - currentLevelReq).coerceAtLeast(0)
        val needed    = nextLevelReq - currentLevelReq
        return Pair(inLevel, needed)
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private lateinit var viewModel: ProfileViewModel
    private lateinit var badgeAdapter: BadgeAdapter
    private val badgeList = mutableListOf<UserBadge>()

    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvXPStatus: TextView
    private lateinit var pbXP: ProgressBar
    private lateinit var tvStreak: TextView
    private lateinit var tvRanking: TextView
    private lateinit var tvRankName: TextView
    private lateinit var tvRankPoint: TextView
    private lateinit var pbRank: ProgressBar
    private lateinit var imgAvatar: ImageView
    private lateinit var imgRankIcon: ImageView
    private lateinit var rvBadges: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews(view)
        setupViewModel()
        observeViewModel()

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("TOKEN", "") ?: ""
        if (token.isNotEmpty()) {
            viewModel.fetchProfileData(token)
        }

        return view
    }

    private fun initViews(view: View) {
        tvUsername = view.findViewById(R.id.tv_profile_username)
        tvBio      = view.findViewById(R.id.tv_profile_bio)
        tvLevel    = view.findViewById(R.id.tv_profile_level)
        tvXPStatus = view.findViewById(R.id.tv_xp_status)
        pbXP       = view.findViewById(R.id.pb_xp)
        tvStreak   = view.findViewById(R.id.tv_stats_streak)
        tvRanking  = view.findViewById(R.id.tv_stats_ranking)
        tvRankName = view.findViewById(R.id.tv_rank_name)
        tvRankPoint = view.findViewById(R.id.tv_rank_point)
        pbRank     = view.findViewById(R.id.pb_rank)
        imgAvatar  = view.findViewById(R.id.img_profile_avatar)
        imgRankIcon = view.findViewById(R.id.img_rank_icon)
        rvBadges   = view.findViewById(R.id.rv_badges)

        badgeAdapter = BadgeAdapter(badgeList)
        rvBadges.adapter = badgeAdapter

        view.findViewById<View>(R.id.btn_edit_profile).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileInfoActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
            bottomNav?.selectedItemId = R.id.nav_home
        }

        view.findViewById<View>(R.id.btn_see_all_badges).setOnClickListener {
            startActivity(Intent(requireContext(), AllBadgesActivity::class.java))
        }
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.api)
        val factory    = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { result ->
            if (result is ProfileResult.Success) {
                result.data?.let { updateUI(it) }
            } else if (result is ProfileResult.Error) {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userBadges.observe(viewLifecycleOwner) { result ->
            if (result is ProfileResult.Success) {
                badgeList.clear()
                result.data?.let { badgeList.addAll(it.take(3)) }
                badgeAdapter.notifyDataSetChanged()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading("Đang tải thông tin cá nhân...") else hideLoading()
        }
    }

    private fun updateUI(user: UserResponse) {
        tvUsername.text = user.fullName
        tvBio.text      = user.bio ?: "Chưa có giới thiệu"
        tvLevel.text    = "LV ${user.level}"

        // ── XP bar theo bảng level ────────────────────────────────────────────
        val (inLevel, needed) = getXpProgress(user.exp, user.level)
        val isMaxLevel = user.level >= levelTable.size

        if (isMaxLevel) {
            tvXPStatus.text = "MAX LEVEL"
            pbXP.progress   = 100
        } else {
            tvXPStatus.text = "$inLevel / $needed XP"
            pbXP.progress   = (inLevel * 100 / needed).coerceIn(0, 100)
        }

        tvStreak.text  = user.streakDays.toString()
        tvRanking.text = "#${user.leaderboardRank}"

        user.badgeRank?.let {
            tvRankName.text = it.description
            Glide.with(this).load(it.iconUrl).into(imgRankIcon)

            // Giả sử thang điểm Rank là 2000
            tvRankPoint.text = "${user.elo} / 2000"
            pbRank.progress  = (user.elo * 100 / 2000).coerceIn(0, 100)
        }

        user.avatarUrl?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(imgAvatar)
        }
    }
}