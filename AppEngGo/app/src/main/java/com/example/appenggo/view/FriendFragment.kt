package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.FriendAdapter
import com.example.appenggo.adapter.OnlineFriendAdapter
import com.example.appenggo.model.FriendResponse
import com.example.appenggo.websocket.WebSocketManager
import kotlinx.coroutines.launch

class FriendFragment : Fragment() {

    private var rvOnlineFriends: RecyclerView? = null
    private var rvAllFriends: RecyclerView? = null
    private var etSearch: EditText? = null

    private lateinit var onlineAdapter: OnlineFriendAdapter
    private lateinit var allFriendAdapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupAdapters()
        setupSearch()
        loadFriends()
        listenOnlineStatus()
    }

    private fun initViews(view: View) {
        rvOnlineFriends = view.findViewById(R.id.rv_online_friends)
        rvAllFriends = view.findViewById(R.id.rv_all_friends)
        etSearch = view.findViewById(R.id.et_search)

        view.findViewById<View>(R.id.btn_find_friend)?.setOnClickListener {
            startActivity(Intent(requireContext(), FindFriendActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupAdapters() {
        // Click avatar người online → mở chat
        onlineAdapter = OnlineFriendAdapter { friend ->
            openChat(friend)
        }
        rvOnlineFriends?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvOnlineFriends?.adapter = onlineAdapter

        // Click nút chat → mở ChatActivity
        allFriendAdapter = FriendAdapter { friend ->
            openChat(friend)
        }
        rvAllFriends?.layoutManager = LinearLayoutManager(requireContext())
        rvAllFriends?.adapter = allFriendAdapter
    }

    private fun openChat(friend: FriendResponse) {
        val token = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("TOKEN", null) ?: return

        // Tạo/lấy conversation rồi mở ChatActivity
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.openPrivateChat("Bearer $token", friend.userId)
                if (res.code == 1000 && res.result != null) {
                    val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CONVERSATION_ID, res.result.id)
                        putExtra(ChatActivity.EXTRA_FRIEND_NAME, friend.username)
                        putExtra(ChatActivity.EXTRA_FRIEND_AVATAR, friend.avatarUrl)
                        putExtra(ChatActivity.EXTRA_FRIEND_ONLINE, friend.online)
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Không thể mở chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFriends() {
        val token = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("TOKEN", null) ?: return

        lifecycleScope.launch {
            try {
                val allRes = RetrofitClient.api.getAllFriends("Bearer $token")
                if (allRes.code == 1000) {
                    allFriendAdapter.submitList(allRes.result ?: emptyList())
                }

                val onlineRes = RetrofitClient.api.getOnlineFriends("Bearer $token")
                if (onlineRes.code == 1000) {
                    onlineAdapter.submitList(onlineRes.result ?: emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi tải danh sách bạn bè", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        val token = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("TOKEN", null) ?: return

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                lifecycleScope.launch {
                    try {
                        if (keyword.isEmpty()) {
                            val res = RetrofitClient.api.getAllFriends("Bearer $token")
                            if (res.code == 1000) allFriendAdapter.submitList(res.result ?: emptyList())
                        } else {
                            val res = RetrofitClient.api.searchFriends("Bearer $token", keyword)
                            if (res.code == 1000) allFriendAdapter.submitList(res.result ?: emptyList())
                        }
                    } catch (e: Exception) { }
                }
            }
        })
    }

    private fun listenOnlineStatus() {
        WebSocketManager.onStatusChanged = { userId, status ->
            activity?.runOnUiThread {
                val isOnline = status == "ONLINE"
                allFriendAdapter.updateOnlineStatus(userId, isOnline)
                loadFriends()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebSocketManager.onStatusChanged = null
    }
}