package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.MissionProgressResponse

class MissionAdapter(
    private val onClaimClick: (missionId: Int) -> Unit
) : RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

    private var missions: List<MissionProgressResponse> = emptyList()

    fun submitList(list: List<MissionProgressResponse>) {
        missions = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(missions[position])
    }

    override fun getItemCount(): Int = missions.size

    inner class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_mission_name)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_mission_desc)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_mission_progress)
        private val tvExp: TextView = itemView.findViewById(R.id.tv_mission_exp)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.pb_mission)
        private val btnClaim: Button = itemView.findViewById(R.id.btn_claim)

        fun bind(mission: MissionProgressResponse) {
            val info = mission.missionResponse
            tvName.text = info.name
            tvDesc.text = info.description
            tvExp.text = "+${info.expReward} XP"

            val current = mission.currentValue
            val target = info.targetValue
            tvProgress.text = "$current/$target"
            progressBar.max = target
            progressBar.progress = current.coerceAtMost(target)

            when (mission.status) {
                "CLAIMED" -> {
                    btnClaim.visibility = View.VISIBLE
                    btnClaim.text = "Đã nhận"
                    btnClaim.isEnabled = false
                    btnClaim.alpha = 0.5f
                }
                "COMPLETED" -> {
                    btnClaim.visibility = View.VISIBLE
                    btnClaim.text = "Nhận thưởng"
                    btnClaim.isEnabled = true
                    btnClaim.alpha = 1f
                    btnClaim.setOnClickListener { onClaimClick(info.id) }
                }
                else -> { // IN_PROGRESS
                    btnClaim.visibility = View.GONE
                }
            }
        }
    }
}