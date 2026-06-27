package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.UserBadge

class BadgeAdapter(
    private val badges: List<UserBadge>,
    private val layoutResId: Int = R.layout.item_badge,
    private val onItemClick: ((UserBadge) -> Unit)? = null
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBadge: ImageView = view.findViewById(R.id.img_badge_icon)
        val tvBadgeName: TextView? = view.findViewById(R.id.tv_badge_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        
        holder.tvBadgeName?.text = badge.badgeName

        // Fix: Chuyển đổi URL SVG của Cloudinary sang PNG để Glide có thể hiển thị
        val displayUrl = if (badge.iconUrl.contains(".svg")) {
            badge.iconUrl.replace(".svg", ".png")
        } else {
            badge.iconUrl
        }

        Glide.with(holder.itemView.context)
            .load(displayUrl)
            .placeholder(R.drawable.ic_themes)
            .error(R.drawable.ic_themes)
            .into(holder.imgBadge)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(badge)
        }
    }

    override fun getItemCount() = badges.size
}
