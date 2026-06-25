package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.UserResponse

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private var rankingList: List<UserResponse> = emptyList()
    private var currentPage: Int = 1
    private var pageSize: Int = 10

    fun submitList(list: List<UserResponse>, page: Int, size: Int) {
        this.rankingList = list
        this.currentPage = page
        this.pageSize = size
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val user = rankingList[position]
        val rankNumber = (currentPage - 1) * pageSize + position + 1

        holder.tvUsername.text = user.username
        holder.tvDescription.text = user.badgeRank?.description ?: "Chưa xếp hạng"
        holder.tvEloPoints.text = String.format("%,d", user.elo)

        // 1. Load Avatar chính của người dùng
        Glide.with(holder.itemView.context)
            .load(user.avatarUrl)
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar)
            .circleCrop()
            .into(holder.ivAvatar)

        // 2. Load Icon Huy hiệu nhỏ ở dòng dưới bên cạnh chữ danh hiệu (Thách Đấu, Vàng II...)
        if (!user.badgeRank?.iconUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.badgeRank.iconUrl)
                .placeholder(R.drawable.ic_rank)
                .error(R.drawable.ic_rank)
                .into(holder.ivBadgeIcon)
        } else {
            holder.ivBadgeIcon.setImageResource(R.drawable.ic_rank)
        }

        // 3. Xử lý hiển thị Thứ hạng bên trái ngoài cùng (Hạng 1 sử dụng vương miện ic_toprank cứng)
        if (rankNumber == 1) {
            // Hạng 1: Hiển thị đúng vương miện vàng ic_toprank cứng, ẩn Số thứ tự
            holder.ivRankIcon.visibility = View.VISIBLE
            holder.tvRankNumber.visibility = View.GONE
            holder.ivRankIcon.setImageResource(R.drawable.ic_toprank)
        } else {
            // Hạng 2 trở đi: Ẩn icon vương miện lớn, chỉ hiện Số thứ tự để tránh bị đè nhau
            holder.ivRankIcon.visibility = View.GONE
            holder.tvRankNumber.visibility = View.VISIBLE
            holder.tvRankNumber.text = rankNumber.toString()
            
            // Đổi màu số thứ tự theo đúng ảnh mẫu
            val color = when(rankNumber) {
                2 -> ContextCompat.getColor(holder.itemView.context, R.color.primary_blue) // Số 2 màu xanh
                3 -> android.graphics.Color.parseColor("#D35400") // Số 3 màu cam đậm
                else -> android.graphics.Color.parseColor("#AFAFAF") // Các số còn lại màu xám
            }
            holder.tvRankNumber.setTextColor(color)
        }

        // Đã xóa phần cập nhật LayoutParams bằng code vì iv_rank_avatar đã luôn bám theo layout_rank_container cố định (32dp) trong XML.
        // Điều này giúp tất cả các hàng đều thẳng hàng tắp 100%.
    }

    override fun getItemCount(): Int = rankingList.size

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRankIcon: ImageView = itemView.findViewById(R.id.iv_rank_icon)
        val tvRankNumber: TextView = itemView.findViewById(R.id.tv_rank_number)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_rank_avatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tv_rank_username)
        val ivBadgeIcon: ImageView = itemView.findViewById(R.id.iv_badge_icon)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_rank_description)
        val tvEloPoints: TextView = itemView.findViewById(R.id.tv_rank_elo_points)
    }
}