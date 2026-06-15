package com.example.appenggo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.MessageResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter(
    private val currentUsername: String,
    private val items: MutableList<MessageResponse> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        Log.d("MessageAdapter", "sender='${msg.senderUsername}' current='$currentUsername' match=${msg.senderUsername == currentUsername}")
        return if (msg.senderUsername == currentUsername)
            VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
            )
        } else {
            ReceivedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        val time = formatTime(msg.createdAt)

        when (holder) {
            is SentViewHolder -> {
                holder.tvMessage.text = msg.content
                holder.tvTime.text = time
            }
            is ReceivedViewHolder -> {
                holder.tvMessage.text = msg.content
                holder.tvTime.text = time
            }
        }
    }

    override fun getItemCount() = items.size

    // Load lịch sử tin nhắn
    fun submitList(newList: List<MessageResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // Thêm 1 tin nhắn mới realtime
    fun addMessage(message: MessageResponse) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }

    private fun formatTime(dateTime: String?): String {
        if (dateTime == null) return ""
        return try {
            val dt = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            dt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            ""
        }
    }
}