package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Hoặc Coil tùy thuộc project của bạn
import com.example.appenggo.R
import com.example.appenggo.model.TopEloResponse // Thay bằng đường dẫn Model thực tế của nhóm

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private var rankingList: List<TopEloResponse> = emptyList()

    fun submitList(list: List<TopEloResponse>) {
        this.rankingList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val user = rankingList[position]
        val rankNumber = position + 1 // Vị trí trong list bắt đầu từ 0 -> Rank bắt đầu từ 1

        holder.tvUsername.text = user.topUsers[position].username

        // Đổ dữ liệu chuỗi danh hiệu từ trường "description" của API
        holder.tvDescription.text = user.topUsers[position].badgeRank?.description ?: "Chưa xếp hạng"

        // Đổ thông số điểm số Elo
        holder.tvEloPoints.text = String.format("%,d", user.topUsers[position].elo)

        // Tải ảnh đại diện/Rank từ url "iconUrl" bằng thư viện Glide
        if (!user.topUsers[position].badgeRank?.iconUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.topUsers[position].badgeRank?.iconUrl)
                .placeholder(R.drawable.ic_person) // Ảnh mặc định khi chờ load
                .error(R.drawable.ic_person)       // Ảnh khi xảy ra lỗi
                .circleCrop()                      // Bo tròn ảnh giống thiết kế ban đầu
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person)
        }

        // --- XỬ LÝ THEO THỨ HẠNG (RANK) ĐỂ ĐỔI GIAO DIỆN ---
        when (rankNumber) {
            1 -> {
                // Rank 1: Hiện icon sấm sét, ẩn text số
                holder.ivRankIcon.visibility = View.VISIBLE
                holder.tvRankNumber.visibility = View.GONE

                // Cập nhật lại vị trí hiển thị của Avatar sang bên phải của Icon thay vì Số
                val params = holder.ivAvatar.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.RIGHT_OF, R.id.iv_rank_icon)
                holder.ivAvatar.layoutParams = params
            }
            2 -> {
                // Rank 2: Hiện số, ẩn icon sấm sét, đổi màu chữ thành Xanh lam
                holder.ivRankIcon.visibility = View.GONE
                holder.tvRankNumber.visibility = View.VISIBLE
                holder.tvRankNumber.text = "2"
                holder.tvRankNumber.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primary_blue))

                val params = holder.ivAvatar.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_rank_number)
                holder.ivAvatar.layoutParams = params
            }
            3 -> {
                // Rank 3: Đổi màu chữ số sang màu Cam đậm
                holder.ivRankIcon.visibility = View.GONE
                holder.tvRankNumber.visibility = View.VISIBLE
                holder.tvRankNumber.text = "3"
                holder.tvRankNumber.setTextColor(android.graphics.Color.parseColor("#D35400"))

                val params = holder.ivAvatar.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_rank_number)
                holder.ivAvatar.layoutParams = params
            }
            else -> {
                // Các Rank còn lại (Từ hạng 4 trở đi): Giữ nguyên màu xám mặc định
                holder.ivRankIcon.visibility = View.GONE
                holder.tvRankNumber.visibility = View.VISIBLE
                holder.tvRankNumber.text = rankNumber.toString()
                holder.tvRankNumber.setTextColor(android.graphics.Color.parseColor("#AFAFAF"))

                val params = holder.ivAvatar.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_rank_number)
                holder.ivAvatar.layoutParams = params
            }
        }
    }

    override fun getItemCount(): Int = rankingList.size

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRankIcon: ImageView = itemView.findViewById(R.id.iv_rank_icon)
        val tvRankNumber: TextView = itemView.findViewById(R.id.tv_rank_number)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_rank_avatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tv_rank_username)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_rank_description)
        val tvEloPoints: TextView = itemView.findViewById(R.id.tv_rank_elo_points)
    }
}