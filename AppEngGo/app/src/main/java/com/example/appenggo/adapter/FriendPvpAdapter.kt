package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.FriendResponse

class FriendPvpAdapter(
    private val items: MutableList<FriendResponse> = mutableListOf(),
    private val onInvite: (FriendResponse) -> Unit
) : RecyclerView.Adapter<FriendPvpAdapter.ViewHolder>() {

    // userId đã gửi lời mời — disable nút
    private val invitedIds = mutableSetOf<Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnInvite: Button = view.findViewById(R.id.btn_invite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_pvp, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = items[position]

        holder.tvUsername.text = friend.username
        holder.tvStatus.text = if (friend.online) "Đang hoạt động" else "Offline"
        holder.tvStatus.setTextColor(
            holder.itemView.context.getColor(
                if (friend.online) android.R.color.holo_green_dark else android.R.color.darker_gray
            )
        )

        // Show green dot only when the friend is online
        val dotOnline = holder.itemView.findViewById<View>(R.id.dot_online)
        dotOnline.visibility = if (friend.online) View.VISIBLE else View.GONE

        if (!friend.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(friend.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        if (invitedIds.contains(friend.userId)) {
            holder.btnInvite.text = "Đã mời"
            holder.btnInvite.isEnabled = false
            holder.btnInvite.alpha = 0.5f
        } else if (!friend.online) {
            // Bạn đang offline → không cho mời, tránh tạo match "treo"
            // vì backend không có cơ chế timeout cho luồng mời trực tiếp này.
            holder.btnInvite.text = "Offline"
            holder.btnInvite.isEnabled = false
            holder.btnInvite.alpha = 0.5f
        } else {
            holder.btnInvite.text = "MỜI"
            holder.btnInvite.isEnabled = true
            holder.btnInvite.alpha = 1f
            holder.btnInvite.setOnClickListener {
                invitedIds.add(friend.userId)
                holder.btnInvite.text = "Đã mời"
                holder.btnInvite.isEnabled = false
                holder.btnInvite.alpha = 0.5f
                onInvite(friend)
            }
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<FriendResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // Cập nhật trạng thái online/offline realtime từ WebSocket
    fun updateOnlineStatus(userId: Int, isOnline: Boolean) {
        val index = items.indexOfFirst { it.userId == userId }
        if (index != -1) {
            val current = items[index]
            items[index] = current.copy(online = isOnline)
            notifyItemChanged(index)
        }
    }

    // Mở lại nút mời khi: request bị backend từ chối (vd bạn vừa offline),
    // hoặc khi nhận PVP_DECLINED từ đúng người đó.
    fun resetInviteState(userId: Int) {
        invitedIds.remove(userId)
        val index = items.indexOfFirst { it.userId == userId }
        if (index != -1) notifyItemChanged(index)
    }
}