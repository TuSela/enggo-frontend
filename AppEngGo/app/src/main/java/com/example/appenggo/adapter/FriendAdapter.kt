package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.FriendResponse

class FriendAdapter(
    private val items: MutableList<FriendResponse> = mutableListOf(),
    private val onChatClick: (FriendResponse) -> Unit
) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val dotOnline: View = view.findViewById(R.id.dot_online)
        val btnChat: ImageView = view.findViewById(R.id.btn_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = items[position]
        holder.tvUsername.text = friend.username

        // Chấm xanh nếu online
        holder.dotOnline.visibility = if (friend.online) View.VISIBLE else View.GONE

        if (!friend.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(friend.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        holder.btnChat.setOnClickListener { onChatClick(friend) }
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<FriendResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // Cập nhật trạng thái online realtime từ WebSocket
    fun updateOnlineStatus(userId: Int, isOnline: Boolean) {
        val index = items.indexOfFirst { it.userId == userId }
        if (index != -1) {
            items[index] = items[index].copy(online = isOnline)
            notifyItemChanged(index)
        }
    }
}