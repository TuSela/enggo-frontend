package com.example.appenggo.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.RankingAdapter
import kotlinx.coroutines.launch

class LeaderboardFragment : BaseFragment() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var containerPages: LinearLayout

    private var currentPage = 1
    private val pageSize = 10
    private var totalPages = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        loadLeaderboard(currentPage)
    }

    private fun initViews(view: View) {
        rvLeaderboard = view.findViewById(R.id.rv_leaderboard)
        btnPrev = view.findViewById(R.id.btn_prev)
        btnNext = view.findViewById(R.id.btn_next)
        containerPages = view.findViewById(R.id.container_pages)

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadLeaderboard(currentPage)
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                loadLeaderboard(currentPage)
            }
        }
    }

    private fun setupRecyclerView() {
        rankingAdapter = RankingAdapter()
        rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.adapter = rankingAdapter
    }

    private fun loadLeaderboard(page: Int) {
        val token = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("TOKEN", null) ?: return

        showLoading("Đang tải bảng xếp hạng...")
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLeaderBoard("Bearer $token", page, pageSize)
                if (response.code == 1000 && response.result != null) {
                    val pageData = response.result
                    rankingAdapter.submitList(pageData.content, pageData.page, pageData.size)
                    totalPages = pageData.totalPages
                    if (totalPages < 1) totalPages = 1
                    updatePaginationUI()
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LeaderboardError", "Chi tiết lỗi kết nối server: ", e)
                Toast.makeText(requireContext(), "Lỗi kết nối server: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                hideLoading()
            }
        }
    }

    private fun updatePaginationUI() {
        containerPages.removeAllViews()

        for (i in 1..totalPages) {
            val pageView = LayoutInflater.from(requireContext()).inflate(R.layout.item_page_number, containerPages, false)
            val tvPage = pageView.findViewById<TextView>(R.id.tv_page_number)
            tvPage.text = i.toString()

            if (i == currentPage) {
                tvPage.setBackgroundResource(R.drawable.bg_page_selected)
                tvPage.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                tvPage.setBackgroundResource(0)
                tvPage.setTextColor(android.graphics.Color.parseColor("#AFAFAF"))
                tvPage.setOnClickListener {
                    currentPage = i
                    loadLeaderboard(currentPage)
                }
            }
            containerPages.addView(pageView)
        }

        btnPrev.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < totalPages

        btnPrev.imageTintList = ContextCompat.getColorStateList(requireContext(), if (currentPage > 1) R.color.primary_blue else R.color.gray)
        btnNext.imageTintList = ContextCompat.getColorStateList(requireContext(), if (currentPage < totalPages) R.color.primary_blue else R.color.gray)
    }
}