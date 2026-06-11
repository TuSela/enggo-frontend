package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel
import android.widget.Toast
class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var pbDailyMission: ProgressBar? = null
    private var btnLearnVocabulary: View? = null

    private var btn_battle: View? = null

    private var currentUserId: Int = -1
    private var currentToken: String = ""

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
    }

    private fun initViews(view: View) {
        tvStreak = view.findViewById(R.id.tv_streak)
        tvLevel = view.findViewById(R.id.tv_level)
        tvProgress = view.findViewById(R.id.tv_progress)
        pbDailyMission = view.findViewById(R.id.pb_daily_mission)
        btnLearnVocabulary = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle = view.findViewById(R.id.btn_battle)
    }

    private fun setupClickListeners() {
        btnLearnVocabulary?.setOnClickListener {
            val intent = Intent(requireContext(), VocabularyActivity::class.java)
            startActivity(intent)
        }

        btn_battle?.setOnClickListener {
            if (currentUserId == -1 || currentToken.isEmpty()) {
                Toast.makeText(requireContext(), "Đang tải thông tin tài khoản, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🎯 TRUYỀN THẲNG SANG PVPACTIVITY
            val intent = Intent(requireContext(), PvpActivity::class.java).apply {
                putExtra("USER_ID", currentUserId)
                putExtra("JWT_TOKEN", currentToken)
            }
            startActivity(intent)
        }
    }
    private fun observeViewModel() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->

            // 🎯 LẤY ID VÀ TOKEN ĐÃ LƯU TRONG SHAREDPREFERENCES LÚC ĐĂNG NHẬP THÀNH CÔNG
            val sharedPref = requireActivity().getSharedPreferences(
                "app_prefs",
                android.content.Context.MODE_PRIVATE
            )
            currentUserId = sharedPref.getInt("USER_ID", -1)
            currentToken = sharedPref.getString("TOKEN", "") ?: ""

            // Log thử ra Logcat để bạn tiện theo dõi xem máy đã bóc đúng dữ liệu chưa
            android.util.Log.d(
                "PVP_WS",
                "HomeFragment bốc được từ bộ nhớ máy - UID: $currentUserId | Token trống?: ${currentToken.isEmpty()}"
            )

            // Hiển thị các thông số tiến trình học lên giao diện
            tvStreak?.text = "🔥 ${stats.streak}"
            tvLevel?.text = "LV. ${stats.level}"
            tvProgress?.text = "${stats.currentProgress}/${stats.totalProgress}"

            if (stats.totalProgress > 0) {
                val progressPercent =
                    (stats.currentProgress.toFloat() / stats.totalProgress * 100).toInt()
                pbDailyMission?.progress = progressPercent
            }
        }
    }
}


