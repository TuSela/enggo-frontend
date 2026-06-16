package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.UserSearchResponse

class SearchUserAdapter(
    private val sentRequestIds: MutableSet<Int> = mutableSetOf(), // ← nhận từ ngoài
    private val items: MutableList<UserSearchResponse> = mutableListOf(),
    private val onAddFriend: (UserSearchResponse) -> Unit
) : RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvDisplayName: TextView = view.findViewById(R.id.tv_display_name)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val btnAddFriend: TextView = view.findViewById(R.id.btn_add_friend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]

        holder.tvDisplayName.text = user.username
        holder.tvUsername.text = "@${user.username}"

        if (!user.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        // Kiểm tra đã gửi chưa — kể cả sau khi thoát vào lại
        if (sentRequestIds.contains(user.id)) {
            setButtonSent(holder.btnAddFriend)
        } else {
            setButtonAdd(holder.btnAddFriend)
            holder.btnAddFriend.setOnClickListener {
                sentRequestIds.add(user.id)
                setButtonSent(holder.btnAddFriend)
                onAddFriend(user)
            }
        }
    }

    private fun setButtonAdd(btn: TextView) {
        btn.text = "KẾT BẠN"
        btn.setTextColor(btn.context.getColor(android.R.color.white))
        btn.setBackgroundResource(R.drawable.bg_button_blue_fancy)
        btn.isClickable = true
        btn.alpha = 1f
    }

    private fun setButtonSent(btn: TextView) {
        btn.text = "Đã gửi"
        btn.setTextColor(btn.context.getColor(android.R.color.darker_gray))
        btn.setBackgroundResource(R.drawable.bg_button_gray)
        btn.isClickable = false
        btn.alpha = 0.7f
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<UserSearchResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}