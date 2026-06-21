package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.NotificationPayload
import android.widget.Button

class NotificationAdapter(
    private val items: MutableList<NotificationPayload>,
    private val onAccept: ((NotificationPayload) -> Unit)? = null,
    private val onDecline: ((NotificationPayload) -> Unit)? = null
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val btnAccept: Button = view.findViewById(R.id.btn_accept)
        val btnDecline: Button = view.findViewById(R.id.btn_decline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = items[position]

        holder.tvTitle.text = when (n.type) {
            "FRIEND_REQUEST"  -> "🔔 ${n.fromUsername} gửi lời mời kết bạn"
            "FRIEND_ACCEPTED" -> "✅ ${n.fromUsername} đã chấp nhận kết bạn"
            "PVP_INVITE"      -> "⚔️ ${n.fromUsername} mời bạn thách đấu"
            else -> "Thông báo"
        }
        holder.tvMessage.text = n.message
        holder.ivIcon.setImageResource(R.drawable.ic_bell)

        // Chỉ hiện nút với FRIEND_REQUEST và PVP_INVITE
        val showButtons = n.type == "FRIEND_REQUEST" || n.type == "PVP_INVITE"
        holder.btnAccept.visibility = if (showButtons) View.VISIBLE else View.GONE
        holder.btnDecline.visibility = if (showButtons) View.VISIBLE else View.GONE

        holder.btnAccept.setOnClickListener {
            onAccept?.invoke(n)
            // Xóa item khỏi danh sách sau khi xử lý
            items.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }

        holder.btnDecline.setOnClickListener {
            onDecline?.invoke(n)
            items.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = items.size
}