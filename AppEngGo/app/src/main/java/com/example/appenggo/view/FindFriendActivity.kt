package com.example.appenggo.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.SearchUserAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FindFriendActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var searchAdapter: SearchUserAdapter
    private lateinit var token: String
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)

        token = "Bearer ${
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("TOKEN", "")
        }"

        initViews()
        loadSentRequestsThenSetupAdapter()
        setupSearch()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        etSearch = findViewById(R.id.et_search)
        rvResults = findViewById(R.id.rv_search_results)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadSentRequestsThenSetupAdapter() {
        lifecycleScope.launch {
            // Load danh sách đã gửi từ backend trước
            val sentIds = try {
                val res = RetrofitClient.api.getSentRequestIds(token)
                if (res.code == 1000) res.result ?: emptyList()
                else emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            // Khởi tạo adapter với danh sách đã gửi sẵn
            searchAdapter = SearchUserAdapter(
                sentRequestIds = sentIds.toMutableSet()
            ) { user ->
                lifecycleScope.launch {
                    try {
                        val res = RetrofitClient.api.sendFriendRequest(token, user.id)
                        if (res.code == 1000) {
                            Toast.makeText(
                                this@FindFriendActivity,
                                "Đã gửi lời mời tới ${user.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@FindFriendActivity,
                                "Không thể gửi lời mời",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@FindFriendActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            rvResults.layoutManager = LinearLayoutManager(this@FindFriendActivity)
            rvResults.adapter = searchAdapter
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(400)
                    if (keyword.length >= 2) searchUsers(keyword)
                    else if (::searchAdapter.isInitialized) searchAdapter.submitList(emptyList())
                }
            }
        })
    }

    private fun searchUsers(keyword: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.searchUsers(token, keyword)
                if (res.code == 1000) {
                    searchAdapter.submitList(res.result ?: emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@FindFriendActivity, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        }
    }
}