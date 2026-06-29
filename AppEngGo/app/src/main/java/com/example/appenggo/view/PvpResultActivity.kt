package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.MatchResultResponse
import com.google.gson.Gson

class PvpResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_result)

        val resultJson = intent.getStringExtra("RESULT_JSON") ?: return finish()
        val myUserId   = intent.getIntExtra("MY_USER_ID", -1)

        val result = Gson().fromJson(resultJson, MatchResultResponse::class.java)

        // Xác định mình là player1 hay player2 dựa vào userId thực từ server
        val iAmPlayer1 = result.player1Id == myUserId
        val myResult   = if (iAmPlayer1) result.player1 else result.player2
        val oppResult  = if (iAmPlayer1) result.player2 else result.player1

        // winnerId == null → hòa, == myUserId → thắng, còn lại → thua
        val iWon = result.winnerId != null && result.winnerId == myUserId

        // ── Outcome banner ───────────────────────────────────────────────────
        val tvOutcome    = findViewById<TextView>(R.id.tv_outcome)
        val tvOutcomeSub = findViewById<TextView>(R.id.tv_outcome_sub)

        when {
            result.winnerId == null -> {
                tvOutcome.text    = "🤝 HÒA"
                tvOutcomeSub.text = "Trận đấu quá cân sức!"
                tvOutcome.setTextColor(getColor(R.color.gray_text))
            }
            iWon -> {
                tvOutcome.text    = "🏆 CHIẾN THẮNG!"
                tvOutcomeSub.text = "Xuất sắc! Bạn đã thắng trận này."
                tvOutcome.setTextColor(getColor(R.color.primary_blue))
            }
            else -> {
                tvOutcome.text    = "💔 THUA CUỘC"
                tvOutcomeSub.text = "Cố gắng hơn lần sau nhé!"
                tvOutcome.setTextColor(getColor(android.R.color.holo_red_light))
            }
        }

        // ── My stats ─────────────────────────────────────────────────────────
        findViewById<TextView>(R.id.tv_my_name).text = myResult.userName ?: "Bạn"
        findViewById<TextView>(R.id.tv_my_level).text = "LV ${myResult.level ?: 1}"
        findViewById<TextView>(R.id.tv_my_score).text = "${myResult.playerScore}"
        findViewById<TextView>(R.id.tv_my_correct).text =
            "${myResult.correctAnswersCount ?: 0}/${myResult.totalQuestions ?: 0}"
        findViewById<TextView>(R.id.tv_my_time).text = formatDuration(myResult.duration)

        val ivMyAvatar = findViewById<ImageView>(R.id.iv_my_avatar)
        Glide.with(this)
            .load(myResult.avatarUrl)
            .placeholder(R.drawable.ic_default_avatar)
            .circleCrop()
            .into(ivMyAvatar)

        val eloChange = myResult.eloChange ?: 0
        val tvMyElo   = findViewById<TextView>(R.id.tv_my_elo)
        tvMyElo.text  = if (eloChange >= 0) "+$eloChange ELO" else "$eloChange ELO"
        tvMyElo.setTextColor(
            if (eloChange >= 0) getColor(R.color.green)
            else getColor(android.R.color.holo_red_light)
        )

        // ── Opponent stats ────────────────────────────────────────────────────
        findViewById<TextView>(R.id.tv_opp_name).text = oppResult.userName ?: "Đối thủ"
        findViewById<TextView>(R.id.tv_opp_level).text = "LV ${oppResult.level ?: 1}"
        findViewById<TextView>(R.id.tv_opp_score).text = "${oppResult.playerScore}"
        findViewById<TextView>(R.id.tv_opp_correct).text =
            "${oppResult.correctAnswersCount ?: 0}/${oppResult.totalQuestions ?: 0}"
        findViewById<TextView>(R.id.tv_opp_time).text = formatDuration(oppResult.duration)

        val ivOppAvatar = findViewById<ImageView>(R.id.iv_opp_avatar)
        Glide.with(this)
            .load(oppResult.avatarUrl)
            .placeholder(R.drawable.ic_default_avatar)
            .circleCrop()
            .into(ivOppAvatar)

        val oppEloChange = oppResult.eloChange ?: 0
        val tvOppElo     = findViewById<TextView>(R.id.tv_opp_elo)
        tvOppElo.text    = if (oppEloChange >= 0) "+$oppEloChange ELO" else "$oppEloChange ELO"
        tvOppElo.setTextColor(
            if (oppEloChange >= 0) getColor(R.color.green)
            else getColor(android.R.color.holo_red_light)
        )

        // ── Buttons ───────────────────────────────────────────────────────────
        findViewById<Button>(R.id.btn_back_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("RELOAD_HOME", true)
            }
            startActivity(intent)
            finish()
        }
        // ── Rank section ──────────────────────────────────────────────────────
        val isRanked = intent.getBooleanExtra("IS_RANKED_MATCH", false)
        val layoutRankSection = findViewById<View>(R.id.layout_rank_section)

        if (isRanked) {
            layoutRankSection.visibility = View.VISIBLE

            // Badge icon
            val ivBadge = findViewById<ImageView>(R.id.iv_my_badge_rank)
            val badge = myResult.badgeRank
            if (badge?.iconUrl != null) {
                val badgeIconUrl = if (badge.iconUrl.contains(".svg"))
                    badge.iconUrl.replace(".svg", ".png") else badge.iconUrl
                Glide.with(this)
                    .load(badgeIconUrl)
                    .placeholder(R.drawable.ic_themes)
                    .error(R.drawable.ic_themes)
                    .into(ivBadge)
            }

            // ELO progress bar (dùng elo hiện tại % 100 để giả lập thanh tiến trình)
            val currentElo = myResult.elo ?: 0
            val pbElo = findViewById<ProgressBar>(R.id.pb_elo)
            pbElo.progress = currentElo % 100

            findViewById<TextView>(R.id.tv_elo_value).text = "${currentElo % 100}/100"

            // ELO change
            val eloChg = myResult.eloChange ?: 0
            val tvEloChange = findViewById<TextView>(R.id.tv_elo_change)
            tvEloChange.text = if (eloChg >= 0) "+$eloChg RP" else "$eloChg RP"
            tvEloChange.setTextColor(
                if (eloChg >= 0) getColor(R.color.green)
                else getColor(android.R.color.holo_red_light)
            )
        } else {
            layoutRankSection.visibility = View.GONE
        }
    }

    private fun formatDuration(durationStr: String?): String {
        if (durationStr.isNullOrBlank()) return "00:00"
        return try {
            val totalSeconds = durationStr.toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            durationStr ?: "00:00"
        }
    }
}