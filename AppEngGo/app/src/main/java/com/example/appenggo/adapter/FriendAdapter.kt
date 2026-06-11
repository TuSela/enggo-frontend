package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.Response.UserResponse

class FriendAdapter(
    private var friends: List<UserResponse>,
    private val onInviteClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_friend_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_friend_status)
        val btnInvite: Button = view.findViewById(R.id.btn_invite_friend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_pvp, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.username
        holder.tvStatus.text = if (friend.status == "ONLINE") "● Online" else "● Offline"
        holder.btnInvite.setOnClickListener { onInviteClick(friend) }
    }

    override fun getItemCount() = friends.size

    fun updateData(newFriends: List<UserResponse>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
