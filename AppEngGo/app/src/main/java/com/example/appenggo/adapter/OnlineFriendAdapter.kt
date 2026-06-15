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

class OnlineFriendAdapter(
    private val items: MutableList<FriendResponse> = mutableListOf(),
    private val onClick: (FriendResponse) -> Unit
) : RecyclerView.Adapter<OnlineFriendAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_online_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = items[position]
        holder.tvUsername.text = friend.username

        if (!friend.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(friend.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        holder.itemView.setOnClickListener { onClick(friend) }
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<FriendResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}